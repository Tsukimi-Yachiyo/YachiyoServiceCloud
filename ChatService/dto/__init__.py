from pydantic import BaseModel, Field
from typing import List, Optional
from datetime import datetime


class ApiResponse(BaseModel):
    code: int = Field(200, description="响应状态码")
    msg: str = Field("成功", description="响应消息")
    data: Optional[dict | list] = Field(None, description="响应数据")


class CreateConnectionRequest(BaseModel):
    to_user_id: int = Field(..., description="对方用户ID")


class MessageItem(BaseModel):
    id: int
    connection_id: int
    user_id: int
    user_name: Optional[str] = None
    message: str
    create_time: datetime


class ConnectionData(BaseModel):
    connection_id: int
    first_user_id: int
    second_user_id: int
    message_list: List[MessageItem] = Field(default_factory=list)


class SendMessageRequest(BaseModel):
    connection_id: int
    message: str = Field(..., min_length=1, max_length=1000)


class HistoryMessageRequest(BaseModel):
    connection_id: int
    page: int = Field(1, ge=1)
