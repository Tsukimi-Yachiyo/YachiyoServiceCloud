"""
    此接口用于添加AI模型
"""
from fastapi import APIRouter, Depends
from src.model.llm_model import LLMModel
from sqlmodel.ext.asyncio.session import AsyncSession
from src.model.api_depository import ApiDepository
from src.utils.model_util import ModelUtil
from src.model.result import Result
from src.model import get_session
from src.utils.auth_util import CurrentUser, User


router = APIRouter(prefix="/api/v2/ai")

@router.post("/model/add")
async def add_model(llm_model: LLMModel, current_user: CurrentUser = Depends(User.get_current_user)) -> Result:
    return await ModelUtil.add_new_model(current_user.user_id, llm_model)
