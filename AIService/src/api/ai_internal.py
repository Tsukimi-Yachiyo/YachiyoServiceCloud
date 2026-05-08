"""
    此接口用于给其他微服务通过 feign 服务
"""

from fastapi import APIRouter
from src.model.result import Result
from fastapi import UploadFile

from src.service import rag_documents

router = APIRouter(prefix="/internal/ai")

@router.post("/documents/add")
async def documents_add(request: list[UploadFile]) -> Result:
    return Result.success(data=await rag_documents.add_documents(request))
