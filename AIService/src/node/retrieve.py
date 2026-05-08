import os

from langchain_community.embeddings import DashScopeEmbeddings

from src.utils.model_util import ModelUtil
from langgraph.store.postgres import AsyncPostgresStore
from psycopg import Connection
from src.agent.state import State

store: AsyncPostgresStore = None

async def retrieve(state: State):
    question = state["prompt"]

    items = await store.asearch(
        ("documents", "knowledge_base"),  # 位置参数（namespace_prefix）
        query=question,  # 注意参数名是 query
        limit=5
    )
    context = [item.value.get("text", "") for item in items if item.value]

    return {"context": context}