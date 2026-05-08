from sqlmodel import Field, SQLModel
from datetime import datetime

class History(SQLModel, table=True):

    id: int = Field(primary_key=True)
    session_id: int = Field(nullable=False)
    human_input: str = Field(nullable=False)
    ai_result: str = Field(nullable=False)
    spend_tokens: int = Field(nullable=False)
    create_time: datetime = Field(default_factory=datetime.now)