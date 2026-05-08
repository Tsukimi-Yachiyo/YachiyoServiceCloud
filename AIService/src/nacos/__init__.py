from contextlib import asynccontextmanager
from v2.nacos import NacosNamingService, ClientConfigBuilder, GRPCConfig, RegisterInstanceParam
from fastapi import FastAPI
import os
from src.nacos.nacos_client import init_naming_service

class Config:
    NACOS_SERVER_ADDR = os.getenv("NACOS_SERVER_ADDR", "127.0.0.1:8848")
    NAMESPACE = os.getenv("NAMESPACE", "public")
    GROUP_NAME = os.getenv("GROUP_NAME", "DEFAULT_GROUP")
    SERVICE_NAME = os.getenv("SERVICE_NAME", "ai-service")
    NACOS_USERNAME = os.getenv("NACOS_USERNAME", "nacos")
    NACOS_PASSWORD = os.getenv("NACOS_PASSWORD", "KaguyaIrohaForever")

    IP = os.getenv("IP", "127.0.0.1")
    PORT = int(os.getenv("PORT", 8893))

@asynccontextmanager
async def lifespan(app: FastAPI):
    client_config = (ClientConfigBuilder()
                     .username(Config.NACOS_USERNAME)
                     .password(Config.NACOS_PASSWORD)
                     .server_address(Config.NACOS_SERVER_ADDR)
                     .namespace_id(Config.NAMESPACE)
                     .log_level('INFO')
                     .grpc_config(GRPCConfig(grpc_timeout=5000))
                     .build())
    naming_service = await NacosNamingService.create_naming_service(client_config)

    register_param = RegisterInstanceParam(
        service_name=Config.SERVICE_NAME,
        ip=Config.IP,
        port=Config.PORT,
        metadata={
            "version": "1.0.0", "env": os.getenv("ENV", "dev")}
    )

    success = await naming_service.register_instance(register_param)
    if success:
        print("Nacos instance registered successfully")
    else:
        print("Nacos instance failed to register")

    app.state.naming_service = naming_service

    # 初始化全局 NamingService 供 Feign 使用
    init_naming_service(naming_service)

    yield

    if naming_service:
        await naming_service.deregister_instance(register_param)
        print("✅ 服务实例已从 Nacos 注销")

    if naming_service:
        await naming_service.shutdown()

    print("Nacos 连接已关闭，应用停止")
