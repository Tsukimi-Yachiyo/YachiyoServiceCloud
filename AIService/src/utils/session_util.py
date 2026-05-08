from sqlmodel.ext.asyncio.session import AsyncSession
from src.model.chat_sessions import ChatSession

class SessionUtil:

    @staticmethod
    async def get_session(user_id, session: AsyncSession) -> list[ChatSession]:
        return await session.exec(ChatSession).filter(ChatSession.user_id == user_id).all()

    @staticmethod
    async def add_session(user_id, session: AsyncSession):
        await session.add(ChatSession(user_id=user_id))
        await session.commit()

    @staticmethod
    async def remove_session(user_id: int, session_id: int, session: AsyncSession):
        await session.exec(ChatSession).filter(ChatSession.user_id == user_id, ChatSession.session_id == session_id).delete()
        await session.commit()