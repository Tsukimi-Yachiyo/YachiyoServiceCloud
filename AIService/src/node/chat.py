from src.agent.state import State
from src.utils.model_util import ModelUtil
from langchain_core.messages import SystemMessage, HumanMessage
import asyncio

async def chat(state: State):
    """
        聊天
    """
    llm = await ModelUtil.get_llm()

    with open("prompt\\chat","r",encoding="utf-8") as f:
        system_prompt = f.read()
        context = []
        while not context:
            await asyncio.sleep(1)
            context = state.get("context", [])
        system_prompt += f"\n\n参考以下知识库内容：\n{chr(10).join(context)}"

        message = [
            SystemMessage(content=system_prompt),
            HumanMessage(content=state["prompt"])
        ]
    response = await llm.ainvoke(message)
    return {"output": response.content}
