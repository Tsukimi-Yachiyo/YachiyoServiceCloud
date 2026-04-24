from fastapi import APIRouter, Request, Depends, HTTPException, status, Query
from typing import Optional
from datetime import datetime

from utils.auth import get_current_user, CurrentUser
from dto import (
    ApiResponse, CreateConnectionRequest, ConnectionData,
    SendMessageRequest, HistoryMessageRequest
)
from service.user_service import UserService
from service.chat_service import ChatService
from service.websocket_service import websocket_manager

router = APIRouter(prefix="/api/v2/chat")


@router.get("/friends", response_model=ApiResponse)
async def get_friends(
    request: Request,
    current_user: CurrentUser = Depends(get_current_user)
):
    friends = await UserService.get_friends(request.app, current_user.user_id)
    return ApiResponse(data=friends)


@router.post("/connection/create", response_model=ApiResponse)
async def create_connection(
    request: Request,
    body: CreateConnectionRequest,
    current_user: CurrentUser = Depends(get_current_user)
):
    if not await UserService.is_friend(
        request.app,
        current_user.user_id,
        body.to_user_id
    ):
        raise HTTPException(status_code=1002, detail="非好友不能创建连接")

    connection = await ChatService.create_or_get_connection(
        current_user.user_id,
        body.to_user_id
    )
    return ApiResponse(data=connection.model_dump())


@router.get("/connection/{connection_id}", response_model=ApiResponse)
async def get_connection(
    connection_id: int,
    current_user: CurrentUser = Depends(get_current_user)
):
    connection = await ChatService.get_connection_by_id(
        connection_id,
        current_user.user_id
    )
    if not connection:
        raise HTTPException(status_code=1001, detail="聊天连接不存在")
    return ApiResponse(data=connection.model_dump())


@router.get("/connection/list", response_model=ApiResponse)
async def get_connection_list(
    current_user: CurrentUser = Depends(get_current_user)
):
    connections = await ChatService.get_user_connections(current_user.user_id)
    return ApiResponse(data=[c.model_dump() for c in connections])


@router.post("/message/send", response_model=ApiResponse)
async def send_message(
    request: Request,
    body: SendMessageRequest,
    current_user: CurrentUser = Depends(get_current_user)
):
    connection = await ChatService.get_connection_by_id(
        body.connection_id,
        current_user.user_id
    )
    if not connection:
        raise HTTPException(status_code=1001, detail="聊天连接不存在")

    other_user_id = await ChatService.get_other_user_id(
        body.connection_id,
        current_user.user_id
    )
    if not other_user_id:
        raise HTTPException(status_code=403, detail="无权限")

    if not await UserService.is_friend(
        request.app,
        current_user.user_id,
        other_user_id
    ):
        raise HTTPException(status_code=1002, detail="非好友不能发送消息")

    message = await ChatService.send_message(
        body.connection_id,
        current_user.user_id,
        current_user.user_name,
        body.message
    )

    message_dict = message.model_dump(mode='json')
    message_dict['type'] = 'message'

    if websocket_manager.is_user_online(other_user_id):
        await websocket_manager.send_personal_message(message_dict, other_user_id)

    return ApiResponse(data=message_dict)


@router.get("/message/receive", response_model=ApiResponse)
async def receive_message(
    connection_id: int,
    last_timestamp: datetime,
    current_user: CurrentUser = Depends(get_current_user)
):
    connection = await ChatService.get_connection_by_id(
        connection_id,
        current_user.user_id
    )
    if not connection:
        raise HTTPException(status_code=1001, detail="聊天连接不存在")

    messages = await ChatService.get_new_messages(
        connection_id,
        last_timestamp
    )
    return ApiResponse(data=[m.model_dump(mode='json') for m in messages])


@router.get("/message/history", response_model=ApiResponse)
async def get_history_messages(
    connection_id: int,
    page: int = Query(1, ge=1),
    current_user: CurrentUser = Depends(get_current_user)
):
    connection = await ChatService.get_connection_by_id(
        connection_id,
        current_user.user_id
    )
    if not connection:
        raise HTTPException(status_code=1001, detail="聊天连接不存在")

    messages = await ChatService.get_history_messages(
        connection_id,
        page
    )
    return ApiResponse(data=[m.model_dump(mode='json') for m in messages])
