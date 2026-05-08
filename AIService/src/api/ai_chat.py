"""
    此接口用于与AI聊天
"""

from fastapi import APIRouter
from src.model.result import Result
from src.agent.graph import AgentGraph
from src.model.ai_chat import AIChatRequest
from src.utils.auth_util import User,CurrentUser
from fastapi import Depends
from sqlalchemy.ext.asyncio import AsyncSession
from src.service import ai_service
from src.model import get_session

router = APIRouter(prefix="/api/v2/ai")

agent_graph : AgentGraph = None

@router.post("/chat")
async def chat(request: AIChatRequest, current_user: CurrentUser = Depends(User.get_current_user), session: AsyncSession = Depends(get_session)):
    return Result.success(await agent_graph.invoke(request, current_user.user_id, session))

@router.post("/history")
async def history(session_id: int, session: AsyncSession = Depends(get_session)):
    return Result.success(await agent_graph.get_history(session_id, session))

@router.post("/session/add")
async def session_add(current_user: CurrentUser = Depends(User), session: AsyncSession = Depends(get_session)) -> Result:
    return await ai_service.add_session(current_user.user_id, session)

@router.post("/session/remove")
async def session_remove(session_id: int, current_user: CurrentUser = Depends(User), session: AsyncSession = Depends(get_session)) -> Result:
    return await ai_service.remove_session(current_user.user_id, session_id, session)

@router.post("/session/get")
async def session_get(current_user: CurrentUser = Depends(User), session: AsyncSession = Depends(get_session)) -> Result:
    return await ai_service.get_session(current_user.user_id, session)
