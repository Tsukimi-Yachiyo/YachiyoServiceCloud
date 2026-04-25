# 基础 Python 镜像
FROM python:3.11-slim

# 设置工作目录
WORKDIR /app

# 把打包好的 whl 包复制到镜像里
COPY dist/*.whl .

# 安装你的项目包
RUN pip install --no-cache-dir *.whl

# 暴露端口（根据你项目实际端口改）
EXPOSE 8892

# 启动命令（你项目怎么跑就怎么写）
# 例如：uvicorn main:app --host 0.0.0.0 --port 8000
CMD ["uvicorn", "ChatService:app", "--host", "0.0.0.0", "--port", "8892"]
