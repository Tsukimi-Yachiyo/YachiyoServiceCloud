from sqlmodel.ext.asyncio.session import AsyncSession
from sqlalchemy.ext.asyncio import create_async_engine
import importlib
import pkgutil


DATABASE_URL = "postgresql+asyncpg://postgres:yuanshen123@localhost:5432/yachiyo"
engine = create_async_engine(DATABASE_URL, echo=True)

# 当前包下的所有模块
for _, module_name, _ in pkgutil.iter_modules(__path__):
    module = importlib.import_module(f".{module_name}", __name__)

    for attr_name in dir(module):
        if not attr_name.startswith('_'):
            attr = getattr(module, attr_name)
            globals()[f"{module_name}.{attr_name}"] = attr

async def get_session():
    async with AsyncSession(engine) as session:
        yield session