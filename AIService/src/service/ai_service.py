from src.utils.session_util import SessionUtil
from sqlalchemy.ext.asyncio import AsyncSession
from src.model.result import Result
from fastapi import Depends

async def add_session(user_id: int, session: AsyncSession = None) -> Result:
    """
        添加会话
    """
    await SessionUtil.add_session(user_id=user_id, session=session)
    return Result.success(message="添加会话成功")

async def get_session(user_id: int, session: AsyncSession = None) -> Result:
    """
        获取会话
    """
    session = await SessionUtil.get_session(user_id=user_id, session=session)
    return Result.success(message="获取会话成功", data=session)

async def remove_session(user_id: int, session_id: int, session: AsyncSession = None) -> Result:
    """
        删除会话
    """
    await SessionUtil.remove_session(user_id=user_id, session_id=session_id, session=session)
    return Result.success(message="删除会话成功")
