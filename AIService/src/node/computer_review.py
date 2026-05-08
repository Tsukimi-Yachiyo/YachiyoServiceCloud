from src.agent.state import State
import json

def computer_review(state: State):
    """
        计算机审核
    """
    keyword = json.loads(open("SENSITIVE_WORDS.json", "r", encoding="utf-8").read())

    for key in keyword:
        if key in state["prompt"]:
            return {"is_allow": False}

    return {"is_allow": True}
