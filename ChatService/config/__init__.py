from dotenv import load_dotenv
import os

load_dotenv()


class Config:
    NACOS_SERVER_ADDR = os.getenv("NACOS_SERVER_ADDR", "127.0.0.1:8848")
    NAMESPACE = os.getenv("NAMESPACE", "public")
    GROUP_NAME = os.getenv("GROUP_NAME", "DEFAULT_GROUP")
    SERVICE_NAME = os.getenv("SERVICE_NAME", "chat-service")
    NACOS_USERNAME = os.getenv("NACOS_USERNAME", "nacos")
    NACOS_PASSWORD = os.getenv("NACOS_PASSWORD", "")

    IP = os.getenv("IP", "127.0.0.1")
    PORT = int(os.getenv("PORT", 8892))

    POSTGRES_HOST = os.getenv("POSTGRES_HOST", "127.0.0.1")
    POSTGRES_PORT = int(os.getenv("POSTGRES_PORT", 5432))
    POSTGRES_DB = os.getenv("POSTGRES_DB", "chat_db")
    POSTGRES_USER = os.getenv("POSTGRES_USER", "postgres")
    POSTGRES_PASSWORD = os.getenv("POSTGRES_PASSWORD", "postgres")

    REDIS_HOST = os.getenv("REDIS_HOST", "127.0.0.1")
    REDIS_PORT = int(os.getenv("REDIS_PORT", 6379))
    REDIS_DB = int(os.getenv("REDIS_DB", 0))
    REDIS_PASSWORD = os.getenv("REDIS_PASSWORD", "")

    USER_SERVICE_NAME = os.getenv("USER_SERVICE_NAME", "user-service")
    USER_SERVICE_TIMEOUT = float(os.getenv("USER_SERVICE_TIMEOUT", 3.0))


config = Config()
