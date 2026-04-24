from fastapi import FastAPI, Request, WebSocket, WebSocketDisconnect, Query
from fastapi.responses import JSONResponse
from contextlib import asynccontextmanager
from dotenv import load_dotenv
import os
import json

from config import config
from config.nacos import lifespan as nacos_lifespan
from db import db
from db.init import init_db
from redis import redis_client
from api import router as api_router
from service.websocket_service import websocket_manager
from service.chat_service import ChatService
from dto import ApiResponse

load_dotenv()


@asynccontextmanager
async def lifespan(app: FastAPI):
    await db.init_pool()
    await redis_client.init_client()
    await init_db()
    async with nacos_lifespan(app):
        yield
    await db.close_pool()
    await redis_client.close_client()


app = FastAPI(lifespan=lifespan)

app.include_router(api_router)


@app.get("/health")
async def health_check():
    return ApiResponse(msg="ChatService is running")


@app.websocket("/ws/chat/{connection_id}")
async def websocket_endpoint(
    websocket: WebSocket,
    connection_id: int,
    user_id: int = Query(...),
    user_name: str = Query(None)
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

if __name__ == "__main__":
    uvicorn.run(
        "main:app",
        host=config.IP,
        port=config.PORT,  # 这里指定端口
        reload=True
    )
