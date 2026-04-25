import redis.asyncio as redis
from typing import Optional
from config import config


class RedisClient:
    _client: Optional[redis.Redis] = None

    @classmethod
    async def init_client(cls):
        if cls._client is None:
            cls._client = redis.Redis(
                host=config.REDIS_HOST,
                port=config.REDIS_PORT,
                db=config.REDIS_DB,
                password=config.REDIS_PASSWORD if config.REDIS_PASSWORD else None,
                decode_responses=True
            )
            await cls._client.ping()
            print("Redis client initialized")

    @classmethod
    def get_client(cls) -> redis.Redis:
        return cls._client

    @classmethod
    async def close_client(cls):
        if cls._client:
            await cls._client.close()
            cls._client = None
            print("Redis client closed")


redis_client = RedisClient()
