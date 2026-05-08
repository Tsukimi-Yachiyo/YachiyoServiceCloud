from sqlmodel import Field, SQLModel
from datetime import datetime

class ApiDepository(SQLModel, table=True):

    __tablename__ = "api_depository"

    id: int = Field(primary_key=True)
    user_id: int
    base_url: str
    model: str
    api_key: str
    remaining_tokens: int
    create_time: datetime = Field(default_factory=datetime.now)
    update_time: datetime = Field(
        default_factory=datetime.now,
        sa_column_kwargs={"onupdate": datetime.now}
    )
