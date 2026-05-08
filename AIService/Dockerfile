FROM python:3.11-slim

# 设置工作目录
WORKDIR /app

# 安装系统依赖
RUN apt-get update && apt-get install -y --no-install-recommends \
    gcc \
    libpq-dev \
    curl \
    && rm -rf /var/lib/apt/lists/*

# 安装 Poetry
RUN pip install --no-cache-dir -i https://mirrors.aliyun.com/pypi/simple/ poetry==2.3.2

# 配置 Poetry
RUN poetry config virtualenvs.create false \
    && poetry config cache-dir /tmp/poetry_cache

# 先复制依赖文件，利用Docker缓存
COPY pyproject.toml poetry.lock* ./

# 安装项目依赖
RUN poetry install --no-root --only main

# 复制项目源代码
COPY . .

# 暴露端口
EXPOSE 8893

# 设置环境变量
ENV PYTHONUNBUFFERED=1

# 健康检查
HEALTHCHECK --interval=30s --timeout=30s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:8893/health || exit 1

# 启动 Uvicorn 服务器
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8893", "--workers", "1"]
