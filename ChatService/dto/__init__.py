from pydantic import BaseModel, Field
from typing import List, Optional, TypeVar, Generic
from datetime import datetime
import logging

log = logging.getLogger(__name__)

T = TypeVar('T')


class Result(BaseModel, Generic[T]):
    code: str = Field("200", description="响应状态码")
    message: str = Field("success", description="响应消息")
    data: Optional[T] = Field(None, description="响应数据")
    detail: Optional[str] = Field(None, description="详情")

    @classmethod
    def success(cls, data: Optional[T] = None, message: str = "success", detail: Optional[str] = None) -> 'Result[T]':
        result = cls(code="200", message=message, data=data, detail=detail)
        cls._log(True, result)
        return result

    @classmethod
    def error(cls, code: str = "500", message: str = "error", detail: Optional[str] = None) -> 'Result[T]':
        result = cls(code=code, message=message, data=None, detail=detail)
        cls._log(False, result)
        return result

    @staticmethod
    def _log(is_success: bool, result: 'Result') -> None:
        log.info("%s: code=%s, message=%s, data=%s, detail=%s",
                 "success" if is_success else "error",
                 result.code, result.message, result.data, result.detail)





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
