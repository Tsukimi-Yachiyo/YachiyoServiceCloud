from src.agent.state import State
from src.utils.model_util import ModelUtil

async def compress(state: State):

    """
        压缩
    """
    if len(state["output"]) > 150:
        print(len(state["output"]))
        return True

    return False

