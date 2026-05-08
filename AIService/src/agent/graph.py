from langgraph.checkpoint.postgres.aio import AsyncPostgresSaver
from langgraph.graph import StateGraph, END
from src.agent.state import State
from src.model.ai_chat import AIChatRequest
from src.node import computer_review, analyse, plot, persona, game, chat, compress, edge, retrieve
from src.model.history import History
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

DB_URI =  "postgresql://postgres:yuanshen123@localhost:5432/yachiyo?sslmode=disable"

class AgentGraph:
    def __init__(self, connection_pool):
        self.app: StateGraph[State] = None
        self.checkpointer: AsyncPostgresSaver = None
        self.pool = connection_pool
        self.graph = StateGraph(State)

        self.graph.add_node("computer_review", computer_review.computer_review)
        self.graph.add_node("retrieve", retrieve.retrieve)
        self.graph.add_node("llm_and_computer_analyse", analyse.analyse)
        self.graph.add_node("llm_plot", plot.plot)
        self.graph.add_node("llm_persona", persona.persona)
        self.graph.add_node("llm_game", game.game)
        self.graph.add_node("llm_chat", chat.chat)
        self.graph.add_node("llm_compress", compress.compress)

        self.graph.set_entry_point("computer_review")
        self.graph.set_entry_point("retrieve")
        self.graph.add_conditional_edges(
        "computer_review",
        lambda state: state["is_allow"],
        {
            True:"llm_and_computer_analyse",
            False:END,
        }
        )
        self.graph.add_conditional_edges(
            "llm_and_computer_analyse",
            lambda state: state["next_branch"],
            {
                "plot":"llm_plot",
                "persona":"llm_persona",
                "game":"llm_game",
                "chat":"llm_chat",
            },
        )
        for node in [ "llm_plot", "llm_persona", "llm_game", "llm_chat", "llm_compress" ]:
            self.graph.add_conditional_edges(
                node,
                edge.compress,
                {
                    True:"llm_compress",
                    False:END,
                }
            )

        self.graph.add_edge("llm_compress",END)
        
    async def start(self):
        self.checkpointer = AsyncPostgresSaver(self.pool)
        self.app = self.graph.compile(checkpointer=self.checkpointer)

    async def invoke(self, request: AIChatRequest, user_id: int, session: AsyncSession = None) -> State:
        config = {"configurable": {"thread_id": request.session_id}}

        result = await self.app.ainvoke({"prompt": request.prompt}, config = config)
        response = result.get("output")
        history = History(
            session_id=request.session_id,
            user_id=user_id,
            human_input = request.prompt,
            ai_output = response
        )
        await session.add(history)
        await session.commit()
        return response

    async def get_history(self, session_id: int, session: AsyncSession = None) -> list[History]:
        stmt = select(History).where(History.session_id == session_id)
        result = await session.exec(stmt)
        history_list = result.scalars().all()
        return history_list
