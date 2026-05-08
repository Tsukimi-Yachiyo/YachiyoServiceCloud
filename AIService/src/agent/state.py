from typing import TypedDict, List, Optional, Annotated
from langchain_core.messages import BaseMessage
from langgraph.graph import add_messages

class State(TypedDict):
    messages: Annotated[List[BaseMessage], add_messages]

    prompt: str # 输入提示

    # 用户信息
    user_id: int # 用户ID
    session_id: Optional[str] # 会话ID

    analyse_result: str # 分析结果

    context: List[str]

    # 对话信息
    emotion: str # 对话情感
    response_emotion: str # 回答情感
    next_branch: str # 下一步分支
    is_allow: bool # 是否允许继续

    output: str # 输出
