"""应用配置 — 从环境变量加载，支持 .env 文件"""

from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    # SiliconFlow LLM
    siliconflow_api_key: str = ""
    siliconflow_base_url: str = "https://api.siliconflow.cn/v1"
    siliconflow_model: str = "deepseek-ai/DeepSeek-V4-Flash"
    siliconflow_temperature: float = 0.7
    siliconflow_max_tokens: int = 4096

    # Embedding
    embedding_model: str = "BAAI/bge-m3"
    embedding_dim: int = 1024

    # Java 后端 CRM API
    crm_api_base_url: str = "http://localhost:8080/api/v1"

    # 数据库
    database_url: str = "sqlite+aiosqlite:///./crm_ai.db"

    # ChromaDB
    chroma_persist_dir: str = "./chroma_db"

    # 服务
    ai_service_port: int = 8001

    model_config = {"env_file": ".env", "env_file_encoding": "utf-8"}


settings = Settings()
