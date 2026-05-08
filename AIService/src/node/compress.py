from src.agent.state import State
from src.utils.model_util import ModelUtil
from langchain_core.messages import SystemMessage, HumanMessage

async def compress(state: State):
    """
        压缩文本
    """
    llm = await ModelUtil.get_compress_model()
    with open("prompt\\summary_out_prompt","r",encoding="utf-8") as f:
        system_prompt = f.read()
        message = [
            SystemMessage(content=system_prompt),
            HumanMessage(content=state["output"])
        ]
    response = await llm.ainvoke(message)
    return {"output": response.content}
