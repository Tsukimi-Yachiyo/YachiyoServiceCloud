"""
Nacos 服务发现客户端模块
"""
import os
from typing import Optional
from v2.nacos import NacosNamingService, ClientConfigBuilder, GRPCConfig

# 全局 NamingService 实例
_naming_service: Optional[NacosNamingService] = None


def init_naming_service(naming_service: NacosNamingService):
    """
    初始化全局 NamingService 实例（从 FastAPI app 传入）
    """
    global _naming_service
    _naming_service = naming_service


def get_naming_service() -> Optional[NacosNamingService]:
    """
    获取全局 NamingService 实例
    """
    return _naming_service
