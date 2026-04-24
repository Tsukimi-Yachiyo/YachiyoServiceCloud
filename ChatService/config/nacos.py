from contextlib import asynccontextmanager
from v2.nacos import NacosNamingService, ClientConfigBuilder, GRPCConfig, RegisterInstanceParam
from fastapi import FastAPI
import os
from config import config


@asynccontextmanager
async def lifespan(app: FastAPI):
    client_config = (ClientConfigBuilder()
                     .username(config.NACOS_USERNAME)
                     .password(config.NACOS_PASSWORD)
                     .server_address(config.NACOS_SERVER_ADDR)
                     .namespace_id(config.NAMESPACE)
                     .log_level('INFO')
                     .grpc_config(GRPCConfig(grpc_timeout=5000))
                     .build())
    naming_service = await NacosNamingService.create_naming_service(client_config)

    register_param = RegisterInstanceParam(
        service_name=config.SERVICE_NAME,
        ip=config.IP,
        port=config.PORT,
        metadata={
            "version": "1.0.0", "env": os.getenv("ENV", "dev")}
    )

    success = await naming_service.register_instance(register_param)
    if success:
        print("Nacos instance registered successfully")
    else:
        print("Nacos instance failed to register")

    app.state.naming_service = naming_service

    yield

    if naming_service:
        await naming_service.deregister_instance(register_param)
        print("✅ 服务实例已从 Nacos 注销")

    if naming_service:
        await naming_service.shutdown()

    print("Nacos 连接已关闭，应用停止")
