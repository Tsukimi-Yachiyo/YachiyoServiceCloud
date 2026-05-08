from pydantic import BaseModel

class LLMModel(BaseModel):
    url : str
    model_name : str
    api_key : str
