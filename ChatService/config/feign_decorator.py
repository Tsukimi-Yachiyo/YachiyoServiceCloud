"""
Feign 风格装饰器 - 异步版
支持 Nacos 服务发现、随机负载均衡、像调本地方法一样调远程 HTTP 服务
"""
import aiohttp
import inspect
import random
from functools import wraps
from typing import Callable, Any, Optional, Dict

from config import config
from config.nacos_client import get_naming_service


async def select_instance(service_name: str) -> Optional[str]:
    """
    从 Nacos 选择一个可用服务实例（随机负载均衡）
    只使用 list_instances 方法（你提供的 NacosNamingService 支持该方法）
    """
    try:
        naming_service = get_naming_service()
        if not naming_service:
            print("Naming service not initialized, using fallback address")
            return get_fallback_address(service_name)

        # 使用 list_instances 获取健康实例
        from v2.nacos.naming.model.naming_param import ListInstanceParam
        request = ListInstanceParam(
            service_name=service_name,
            group_name=config.GROUP_NAME,
            subscribe=True,
            healthy_only=True
        )
        instances = await naming_service.list_instances(request)

        if not instances:
            print(f"No healthy instance found for {service_name}, using fallback address")
            return get_fallback_address(service_name)

        # 随机负载均衡
        ins = random.choice(instances)
        return f"http://{ins.ip}:{ins.port}"
    except Exception as e:
        print(f"Failed to select instance for {service_name}: {e}, using fallback address")
        return get_fallback_address(service_name)


def get_fallback_address(service_name: str) -> Optional[str]:
    """当 Nacos 不可用时的 fallback 地址（可根据实际环境配置）"""
    import os
    in_docker = os.path.exists("/.dockerenv")

    if service_name == "user-service":
        host = "host.docker.internal" if in_docker else "127.0.0.1"
        return f"http://{host}:9800"
    # 可扩展其他服务的 fallback
    return None


def FeignClient(service_name: str):
    """
    类装饰器：标记该类为 Feign 客户端，所有带 @Api 的方法会被转换为远程调用
    """
    def decorator(cls):
        # 在类上存储服务名（备用）
        cls._service_name = service_name

        # 为类添加一个 session 属性（在 __init__ 中初始化）
        original_init = cls.__init__

        def new_init(self, *args, **kwargs):
            original_init(self, *args, **kwargs)
            self._session = aiohttp.ClientSession()

        cls.__init__ = new_init

        # 添加关闭 session 的方法（可选）
        async def close_session(self):
            if hasattr(self, '_session') and not self._session.closed:
                await self._session.close()
        cls.close = close_session

        # 处理类中带有 _feign 标记的方法
        for name, method in cls.__dict__.items():
            if callable(method) and hasattr(method, "_feign"):
                # 获取元数据
                http_method = method._method.upper()
                path = method._path
                # 获取原始方法的签名（用于参数绑定）
                sig = inspect.signature(method)

                @wraps(method)
                async def wrapper(self, *args, **kwargs):
                    # 绑定位置参数和关键字参数
                    bound = sig.bind_partial(*args, **kwargs)
                    bound.apply_defaults()
                    params = {k: v for k, v in bound.arguments.items() if v is not None}

                    # 获取服务实例地址
                    base_url = await select_instance(service_name)
                    if not base_url:
                        print(f"No available instance for service: {service_name}")
                        return None

                    url = f"{base_url}{path}"
                    timeout = aiohttp.ClientTimeout(total=config.USER_SERVICE_TIMEOUT)

                    try:
                        if http_method == "GET":
                            async with self._session.get(url, params=params, timeout=timeout) as resp:
                                if resp.status == 200:
                                    return await resp.json()
                                print(f"GET {url} -> HTTP {resp.status}: {await resp.text()}")
                                return None
                        elif http_method == "POST":
                            async with self._session.post(url, json=params, timeout=timeout) as resp:
                                if resp.status == 200:
                                    return await resp.json()
                                print(f"POST {url} -> HTTP {resp.status}: {await resp.text()}")
                                return None
                        else:
                            raise ValueError(f"Unsupported HTTP method: {http_method}")
                    except Exception as e:
                        print(f"Feign call to {url} failed: {e}")
                        return None

                # 替换原方法
                setattr(cls, name, wrapper)
        return cls
    return decorator


def Api(method: str, path: str):
    """
    方法装饰器：标记该远程接口的方法，指定 HTTP 方法和路径
    要求被装饰的方法必须定义为 async def（因为会生成异步调用）
    """
    def decorator(func: Callable) -> Callable:
        func._feign = True
        func._method = method
        func._path = path
        return func
    return decorator