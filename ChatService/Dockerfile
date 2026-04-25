FROM python:3.11-slim

# 设置工作目录
WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends \
    gcc \
    libpq-dev \
    && rm -rf /var/lib/apt/lists/*

# 安装 Poetry
RUN pip install --no-cache-dir -i https://mirrors.aliyun.com/pypi/simple/ poetry==2.3.2

COPY pyproject.toml poetry.lock* ./

RUN poetry config virtualenvs.create false

RUN poetry install --no-root

# 复制项目源代码
COPY . .

EXPOSE 8892

ENV PYTHONUNBUFFERED=1

# 启动 Uvicorn 服务器
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8892"]
