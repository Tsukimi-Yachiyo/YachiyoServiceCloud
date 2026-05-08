from fastapi import Request, HTTPException, status
from pydantic import BaseModel


class CurrentUser(BaseModel):
    user_id: int
    user_name: str
    user_role: str
    auth_token: str

class User:
    @staticmethod
    async def get_current_user(request: Request) -> CurrentUser:
        user_id = request.headers.get("X-User-Id")
        user_name = request.headers.get("X-User-Name")
        user_role = request.headers.get("X-User-Role")
        auth_token = request.headers.get("X-Auth-Token")

        if not all([user_id, user_name, user_role, auth_token]):
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="未登录，缺少认证信息"
            )

        try:
            user_id = int(user_id)
        except ValueError:
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="用户ID格式错误"
            )

        return CurrentUser(
            user_id=user_id,
            user_name=user_name,
            user_role=user_role,
            auth_token=auth_token
        )
