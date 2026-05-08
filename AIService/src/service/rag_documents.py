from typing import List
from langchain_core.documents import Document
from langchain_text_splitters import RecursiveCharacterTextSplitter
from langchain_community.document_loaders import PyPDFLoader, TextLoader
from langchain_experimental.text_splitter import SemanticChunker
from fastapi import UploadFile
import tempfile
from src.utils.model_util import ModelUtil
import os

store = None

async def add_documents(files: List[UploadFile]) -> str:
    """
    从本地加载文档（PDF或TXT），处理后存入知识库。
    """

    all_documents = []

    with tempfile.TemporaryDirectory() as tmpdir:
        for file in files:
            # 生成安全的临时文件路径
            tmp_path = os.path.join(tmpdir, file.filename)
            content = await file.read()  # 读取文件内容
            with open(tmp_path, "wb") as f:
                f.write(content)

            # 根据扩展名选择加载器
            if file.filename.endswith(".pdf"):
                from langchain_community.document_loaders import PyPDFLoader
                loader = PyPDFLoader(tmp_path)
            elif file.filename.endswith(".txt"):
                loader = TextLoader(tmp_path, encoding="utf-8")
            else:
                continue  # 跳过不支持的类型
            docs = loader.load()
            all_documents.extend(docs)

    text_splitter = SemanticChunker(
        embeddings_model,
        breakpoint_threshold_type="percentile",  # 可选 "standard_deviation", "interquartile"
        breakpoint_threshold_amount=95.0  # 相似度低于95%分位数时切分
    )

    chunks = text_splitter.split_documents(all_documents)

    # 4. 为每个文本块生成嵌入并存入数据库
    for chunk in chunks:
        # 使用命名空间 "documents"，确保与RAG检索时的命名空间一致
        namespace = ("documents", "knowledge_base")
        doc_id = str(uuid.uuid4())  # 生成一个随机ID
        await store.aput(
            namespace,  # 命名空间，用于分组和搜索
            doc_id,  # 文档的唯一标识
            {
                "text": chunk.page_content,  # 需要被索引嵌入的文本内容
                "metadata": chunk.metadata  # 保留原始元数据，如页码、来源文件等
            }
        )
    return f"成功添加 {len(chunks)} 个文档块到知识库。"
