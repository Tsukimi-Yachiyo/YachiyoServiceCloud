import os
from db import db


async def init_db():
    pool = await db.get_pool()
    sql_path = os.path.join(os.path.dirname(__file__), 'init.sql')

    with open(sql_path, 'r', encoding='utf-8') as f:
        sql = f.read()

    async with pool.acquire() as conn:
        await conn.execute(sql)
        print("Database tables initialized")
