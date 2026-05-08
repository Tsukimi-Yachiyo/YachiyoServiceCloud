from sqlmodel.ext.asyncio.session import AsyncSession
from src.model.llm_model import LLMModel
from src.model.result import Result
from src.model.api_depository import ApiDepository
from langchain_openai import ChatOpenAI
from langchain_community.embeddings import DashScopeEmbeddings
from fastapi import Depends
from sqlalchemy import select
from src.model import get_session

class ModelUtil:
    """
        此类用于添加AI模型
    """
    model: ChatOpenAI = None
    compress_model: ChatOpenAI = None
    embeddings_model: DashScopeEmbeddings = None

    @classmethod
    async def init(cls,session: AsyncSession):
        cls.session = session
        cls.model = None
        cls.compress_model = ChatOpenAI(
            model="deepseek-r1-distill-llama-8b",
            api_key="sk-e696424fcdf4453cb107da9d342f8542",
            base_url="https://dashscope.aliyuncs.com/compatible-mode/v1",
            temperature=0.1
        )
        cls.embeddings_model = DashScopeEmbeddings(
            model="text-embedding-v3",
            dashscope_api_key="sk-e696424fcdf4453cb107da9d342f8542",
        )
        await cls.get_new_model()

    @classmethod
    async def get_compress_model(cls):
        return cls.compress_model

    @classmethod
    async def get_embeddings_model(cls):
        return cls.embeddings_model

    @classmethod
    async def get_llm(cls):
        return cls.model

    @classmethod
    async def get_new_model(cls):
        # 获取 token 最多的模型
        stmt = select(ApiDepository).order_by(ApiDepository.remaining_tokens.desc())
        result = await cls.session.exec(stmt)
        api_depository: ApiDepository = result.scalars().first()
        cls.model = ChatOpenAI(
            model=api_depository.model,
            api_key=api_depository.api_key,
            base_url=api_depository.base_url,
            temperature=0.7
        )

    @classmethod
    async def add_new_model(cls,user_id: int, llm_model: LLMModel) -> Result:
        api_depository = ApiDepository(
            user_id=user_id,
            base_url=llm_model.url,
            model=llm_model.model_name,
            api_key=llm_model.api_key
        )
        try:
            cls.session.add(api_depository)
            await cls.session.commit()
            await cls.session.refresh(api_depository)
            return Result.success(message="添加成功")
        except Exception as e:
            return Result.error(message=str(e))

    @classmethod
    async def expend_token(cls,user_id: int,model_id: int,token: int) -> bool:
        try:
            api_depository: ApiDepository = await cls.session.exec(ApiDepository).filter( ApiDepository.id == model_id).first()
            if api_depository is None:
                return Result.error(message="模型不存在")
            api_depository.remaining_tokens -= token
            await cls.session.commit()
            return True
        except Exception as e:
            return False
