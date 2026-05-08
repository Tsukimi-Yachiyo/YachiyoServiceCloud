from pydantic import BaseModel
from typing import Optional, Any

class Result(BaseModel):
    # 类属性：定义返回格式的字段
    code: str    # 状态码
    data: Optional[Any] = None    # 返回数据
    detail: str = ""  # 详细信息
    message: str = ""  # 提示信息


    # 类方法：成功返回（直接返回 Result 对象）
    @classmethod
    def success(cls, data: Optional[Any] = None, message: str = "操作成功", detail: str = ""):
        return cls(
            code="200",
            data=data,
            detail=detail,
            message=message
        )

    # 类方法：失败返回（直接返回 Result 对象）
    @classmethod
    def error(cls, code: str = "500", message: str = "操作失败", detail: str = ""):
        return cls(
            code=code,
            data=None,
            detail=detail,
            message=message
        )