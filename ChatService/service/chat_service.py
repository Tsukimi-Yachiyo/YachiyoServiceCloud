import json
from typing import List, Optional, Dict
from datetime import datetime
from db import db
from redis import redis_client
from dto import MessageItem, ConnectionData


class ChatService:
    CONNECTION_CACHE_TTL = 86400

    @staticmethod
    def get_connection_cache_key(connection_id: int) -> str:
        return f"chat:connection:{connection_id}"

    @staticmethod
    async def create_or_get_connection(from_user_id: int, to_user_id: int) -> ConnectionData:
        if from_user_id == to_user_id:
            raise ValueError("不能和自己创建连接")

        first_user_id = min(from_user_id, to_user_id)
        second_user_id = max(from_user_id, to_user_id)

        pool = await db.get_pool()
        async with pool.acquire() as conn:
            async with conn.transaction():
                row = await conn.fetchrow(
                    """
                    SELECT id, first_user_id, second_user_id, create_time, update_time
                    FROM chat_connection
                    WHERE first_user_id = $1 AND second_user_id = $2 AND is_deleted = FALSE
                    """,
                    first_user_id, second_user_id
                )

                if row:
                    connection_id = row['id']
                    await conn.execute(
                        "UPDATE chat_connection SET update_time = CURRENT_TIMESTAMP WHERE id = $1",
                        connection_id
                    )
                else:
                    connection_id = await conn.fetchval(
                        """
                        INSERT INTO chat_connection (first_user_id, second_user_id)
                        VALUES ($1, $2)
                        RETURNING id
                        """,
                        first_user_id, second_user_id
                    )

        messages = await ChatService.get_latest_messages(connection_id, limit=10)
        connection_data = ConnectionData(
            connection_id=connection_id,
            first_user_id=first_user_id,
            second_user_id=second_user_id,
            message_list=messages
        )

        await ChatService.cache_connection(connection_data)

        return connection_data

    @staticmethod
    async def get_connection_by_id(connection_id: int, current_user_id: int) -> Optional[ConnectionData]:
        cache_key = ChatService.get_connection_cache_key(connection_id)
        r = redis_client.get_client()

        cached_data = await r.get(cache_key)
        if cached_data:
            data = json.loads(cached_data)
            if current_user_id in [data['first_user_id'], data['second_user_id']]:
                return ConnectionData(**data)
            else:
                return None

        pool = await db.get_pool()
        row = await pool.fetchrow(
            """
            SELECT id, first_user_id, second_user_id, create_time, update_time
            FROM chat_connection
            WHERE id = $1 AND is_deleted = FALSE
            """,
            connection_id
        )

        if not row:
            return None

        if current_user_id not in [row['first_user_id'], row['second_user_id']]:
            return None

        messages = await ChatService.get_latest_messages(connection_id, limit=10)
        connection_data = ConnectionData(
            connection_id=row['id'],
            first_user_id=row['first_user_id'],
            second_user_id=row['second_user_id'],
            message_list=messages
        )

        await ChatService.cache_connection(connection_data)
        return connection_data

    @staticmethod
    async def get_user_connections(current_user_id: int) -> List[ConnectionData]:
        pool = await db.get_pool()
        rows = await pool.fetch(
            """
            SELECT id, first_user_id, second_user_id, create_time, update_time
            FROM chat_connection
            WHERE (first_user_id = $1 OR second_user_id = $1) AND is_deleted = FALSE
            ORDER BY update_time DESC
            """,
            current_user_id
        )

        connections = []
        for row in rows:
            messages = await ChatService.get_latest_messages(row['id'], limit=10)
            connection_data = ConnectionData(
                connection_id=row['id'],
                first_user_id=row['first_user_id'],
                second_user_id=row['second_user_id'],
                message_list=messages
            )
            connections.append(connection_data)
            await ChatService.cache_connection(connection_data)

        return connections

    @staticmethod
    async def get_latest_messages(connection_id: int, limit: int = 10) -> List[MessageItem]:
        pool = await db.get_pool()
        rows = await pool.fetch(
            """
            SELECT id, connection_id, user_id, message, create_time
            FROM chat_message
            WHERE connection_id = $1 AND is_deleted = FALSE
            ORDER BY create_time DESC
            LIMIT $2
            """,
            connection_id, limit
        )

        messages = []
        for row in reversed(rows):
            messages.append(MessageItem(
                id=row['id'],
                connection_id=row['connection_id'],
                user_id=row['user_id'],
                message=row['message'],
                create_time=row['create_time']
            ))

        return messages

    @staticmethod
    async def send_message(
        connection_id: int,
        user_id: int,
        user_name: str,
        message: str
    ) -> MessageItem:
        pool = await db.get_pool()
        async with pool.acquire() as conn:
            async with conn.transaction():
                message_id = await conn.fetchval(
                    """
                    INSERT INTO chat_message (connection_id, user_id, message)
                    VALUES ($1, $2, $3)
                    RETURNING id
                    """,
                    connection_id, user_id, message
                )

                await conn.execute(
                    "UPDATE chat_connection SET update_time = CURRENT_TIMESTAMP WHERE id = $1",
                    connection_id
                )

                row = await conn.fetchrow(
                    "SELECT create_time FROM chat_message WHERE id = $1",
                    message_id
                )

        message_item = MessageItem(
            id=message_id,
            connection_id=connection_id,
            user_id=user_id,
            user_name=user_name,
            message=message,
            create_time=row['create_time']
        )

        await ChatService.add_message_to_cache(connection_id, message_item)

        return message_item

    @staticmethod
    async def get_history_messages(
        connection_id: int,
        page: int = 1,
        limit: int = 10
    ) -> List[MessageItem]:
        offset = (page - 1) * limit
        pool = await db.get_pool()
        rows = await pool.fetch(
            """
            SELECT id, connection_id, user_id, message, create_time
            FROM chat_message
            WHERE connection_id = $1 AND is_deleted = FALSE
            ORDER BY create_time DESC
            LIMIT $2 OFFSET $3
            """,
            connection_id, limit, offset
        )

        messages = []
        for row in reversed(rows):
            messages.append(MessageItem(
                id=row['id'],
                connection_id=row['connection_id'],
                user_id=row['user_id'],
                message=row['message'],
                create_time=row['create_time']
            ))

        return messages

    @staticmethod
    async def get_new_messages(
        connection_id: int,
        last_timestamp: datetime
    ) -> List[MessageItem]:
        pool = await db.get_pool()
        rows = await pool.fetch(
            """
            SELECT id, connection_id, user_id, message, create_time
            FROM chat_message
            WHERE connection_id = $1 AND create_time > $2 AND is_deleted = FALSE
            ORDER BY create_time ASC
            """,
            connection_id, last_timestamp
        )

        messages = []
        for row in rows:
            messages.append(MessageItem(
                id=row['id'],
                connection_id=row['connection_id'],
                user_id=row['user_id'],
                message=row['message'],
                create_time=row['create_time']
            ))

        return messages

    @staticmethod
    async def cache_connection(connection_data: ConnectionData):
        cache_key = ChatService.get_connection_cache_key(connection_data.connection_id)
        r = redis_client.get_client()

        data = {
            "connection_id": connection_data.connection_id,
            "first_user_id": connection_data.first_user_id,
            "second_user_id": connection_data.second_user_id,
            "message_list": [
                m.model_dump(mode='json') for m in connection_data.message_list
            ]
        }

        await r.setex(
            cache_key,
            ChatService.CONNECTION_CACHE_TTL,
            json.dumps(data, ensure_ascii=False)
        )

    @staticmethod
    async def add_message_to_cache(connection_id: int, message: MessageItem):
        cache_key = ChatService.get_connection_cache_key(connection_id)
        r = redis_client.get_client()

        cached_data = await r.get(cache_key)
        if cached_data:
            data = json.loads(cached_data)
            data['message_list'].append(message.model_dump(mode='json'))
            if len(data['message_list']) > 50:
                data['message_list'] = data['message_list'][-50:]
            await r.setex(
                cache_key,
                ChatService.CONNECTION_CACHE_TTL,
                json.dumps(data, ensure_ascii=False)
            )

    @staticmethod
    async def get_other_user_id(connection_id: int, current_user_id: int) -> Optional[int]:
        connection = await ChatService.get_connection_by_id(connection_id, current_user_id)
        if not connection:
            return None
        if connection.first_user_id == current_user_id:
            return connection.second_user_id
        return connection.first_user_id
