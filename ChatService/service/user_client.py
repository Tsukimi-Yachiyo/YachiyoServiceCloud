"""
User Service Feign 接口
对应 Spring @FeignClient("user-service")
"""
from typing import List, Optional
from config.feign_decorator import FeignClient, Api


@FeignClient(service_name="user-service")
class UserClient:
    """
    User Service Feign 客户端
    """

    @Api(method="POST", path="/internal/user/follow/friends")
    async def get_friends(self, currentUserId: int) -> Optional[dict]:
        """
        获取好友列表
        对应: POST /internal/user/follow/friends
        """
        pass

    @Api(method="GET", path="/internal/user/follow/isFriend")
    async def is_friend(self, currentUserId: int, followeeId: int) -> Optional[dict]:
        """
        检查是否为好友
        对应: GET /internal/user/follow/isFriend
        """
        pass
