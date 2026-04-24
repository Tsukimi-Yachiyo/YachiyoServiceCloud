from fastapi import APIRouter, WebSocket, WebSocketDisconnect, Depends, HTTPException
from typing import Optional
import json

from service.websocket_service import websocket_manager
from service.chat_service import ChatService
from utils.auth import CurrentUser

router = APIRouter()


@router.websocket("/ws/chat/{connection_id}")
async def websocket_endpoint(
    websocket: WebSocket,
    connection_id: int,
    user_id: int,
    user_name: Optional[str] = None
):
    try:
        await websocket.accept()
        await websocket_manager.connect(user_id, websocket)

        connection = await ChatService.get_connection_by_id(connection_id, user_id)
        if not connection:
            await websocket.send_json({
                "type": "error",
                "msg": "聊天连接不存在"
            })
            await websocket.close()
            return

        try:
            while True:
                data = await websocket.receive_json()
                msg_type = data.get("type")

                if msg_type == "heartbeat":
                    await websocket.send_json({
                        "type": "heartbeat_ack",
                        "msg": "连接正常"
                    })
                elif msg_type == "message":
                    message_content = data.get("message", "")
                    if message_content:
                        message = await ChatService.send_message(
                            connection_id,
                            user_id,
                            user_name or str(user_id),
                            message_content
                        )
                        message_dict = message.model_dump(mode='json')
                        message_dict['type'] = 'message'
                        await websocket.send_json(message_dict)

                        other_user_id = await ChatService.get_other_user_id(connection_id, user_id)
                        if other_user_id and websocket_manager.is_user_online(other_user_id):
                            await websocket_manager.send_personal_message(message_dict, other_user_id)

        except WebSocketDisconnect:
            await websocket_manager.disconnect(user_id, websocket)
        except Exception as e:
            await websocket_manager.disconnect(user_id, websocket)
    except Exception as e:
        try:
            await websocket.close()
        except:
            pass
