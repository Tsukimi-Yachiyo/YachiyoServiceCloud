from src.agent.state import State
from src.utils.model_util import ModelUtil
from pydantic import BaseModel, Field
from typing import Literal
from langchain_core.messages import SystemMessage, HumanMessage
from pydantic import ValidationError

class AnalysisResult(BaseModel):
    emotion: str = Field(description="用户输入内容的情感")
    response_emotion: str = Field(description="AI 回答的情感")
    next_branch: Literal["chat", "game", "plot"] = Field(description="下一步分支：chat(普通对话)/game(游戏)/plot(故事设定)")

async def analyse(state: State):
    """
        分析文本
    """
    llm = await ModelUtil.get_compress_model()
    llm = llm.with_structured_output(AnalysisResult)
    with  open("prompt\\analyse","r",encoding="utf-8") as f:
        messages = [
            SystemMessage(content=f.read()),
            HumanMessage(content=state["prompt"])
        ]
        time = 0
        while time < 5:
            try:
                response = await llm.ainvoke(
                    messages
                )
                return response.model_dump()
            except ValidationError:
                time += 1
        return {
            "emotion": "happy",
            "response_emotion": "happy",
            "next_branch": "chat",
        }
