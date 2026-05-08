from pydantic import BaseModel
from typing import Optional


class AIChatRequest(BaseModel):

    prompt: str
    session_id: Optional[int]