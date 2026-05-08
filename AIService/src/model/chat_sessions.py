from sqlmodel import Field, SQLModel
from datetime import datetime

class ChatSession(SQLModel, table=True):

    __tablename__ = "chat_sessions"

    id: int = Field( primary_key=True)
    user_id: int = Field(index=True)
    create_time: datetime = Field(default_factory=datetime.now)

