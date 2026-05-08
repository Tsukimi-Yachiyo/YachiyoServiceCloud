from src.agent.state import State
async def plot(state: State):
    """
        故事板
    """
    llm = await ModelUtil.get_plot_model()
    response = await llm.ainvoke(state.prompt)
    state.plot_result = response
