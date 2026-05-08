from fastapi import FastAPI
from src.api import ai_chat, ai_internal, api
from src.agent.graph import AgentGraph
from contextlib import asynccontextmanager
from src.utils.model_util import ModelUtil
from src.model import get_session
from psycopg_pool import AsyncConnectionPool
from src.node import retrieve
from src.service import rag_documents
from langgraph.store.postgres import AsyncPostgresStore
from src import nacos


DB_URI =  "postgresql://postgres:yuanshen123@localhost:5432/yachiyo?sslmode=disable"

@asynccontextmanager
async def lifespan(app: FastAPI):
    pool = AsyncConnectionPool(
        conninfo=DB_URI,
        min_size=2,  # 最小保持连接数
        max_size=10,  # 最大连接数
        max_idle=300,  # 空闲超时(秒)
        max_lifetime=600  # 连接最大寿命(秒)
    )

    async for session in get_session():
        await ModelUtil.init(session)
        break

    embeddings_model = await ModelUtil.get_embeddings_model()
    store =  AsyncPostgresStore(
            pool,
            index={"dims": 1024, "embed": embeddings_model, "fields": ["text"]})
    retrieve.store = store
    rag_documents.store = store
    # 2. 初始化 AgentGraph，传入连接池
    agent_graph = AgentGraph(pool)
    await agent_graph.start()

    ai_chat.agent_graph = agent_graph
    async with nacos.lifespan(app):
        yield

    await agent_graph.checkpointer.close()
    await pool.close()

app = FastAPI(lifespan=lifespan)

app.include_router(ai_chat.router)
app.include_router(api.router)
app.include_router(ai_internal.router)


@app.get("/")
async def root():
    return {"message": "Hello World"}


@app.get("/hello/{name}")
async def say_hello(name: str):
    return {"message": f"Hello {name}"}

