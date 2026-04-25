import httpx
from typing import List, Optional
from fastapi import HTTPException
from config import config
from v2.nacos import ListInstanceParam


class UserService:
    @staticmethod
    async def get_user_service_url(app) -> Optional[str]:
        naming_service = app.state.naming_service
        list_param = ListInstanceParam(
            service_name=config.USER_SERVICE_NAME,
            group_name=config.GROUP_NAME,
            namespace_id=config.NAMESPACE
        )
        instances = await naming_service.list_instance(list_param)
        if instances and len(instances.hosts) > 0:
            host = instances.hosts[0]
            return f"http://{host.ip}:{host.port}"
        return None

    @staticmethod
    async def get_friends(app, current_user_id: int) -> List[int]:
        try:
            url = await UserService.get_user_service_url(app)
            if not url:
                raise HTTPException(status_code=500, detail="用户服务不可用")

            async with httpx.AsyncClient(timeout=config.USER_SERVICE_TIMEOUT) as client:
                response = await client.post(
                    f"{url}/internal/user/follow/friends",
                    params={"currentUserId": current_user_id}
                )
                response.raise_for_status()
                result = response.json()
                if result.get("code") == 200:
                    return result.get("data", [])
                else:
                    raise HTTPException(status_code=1004, detail=result.get("msg", "用户服务调用失败"))
        except httpx.TimeoutException:
            raise HTTPException(status_code=1004, detail="服务暂时不可用，请稍后再试")
        except Exception as e:
            raise HTTPException(status_code=1004, detail=f"用户服务调用失败: {str(e)}")

    @staticmethod
    async def is_friend(app, current_user_id: int, target_user_id: int) -> bool:
        try:
            url = await UserService.get_user_service_url(app)
            if not url:
                return False

            async with httpx.AsyncClient(timeout=config.USER_SERVICE_TIMEOUT) as client:
                response = await client.get(
                    f"{url}/internal/user/follow/isFriend",
                    params={
                        "currentUserId": current_user_id,
                        "followeeId": target_user_id
                    }
                )
                response.raise_for_status()
                result = response.json()
                if result.get("code") == 200:
                    return result.get("data", False)
                return False
        except Exception:
            return False
