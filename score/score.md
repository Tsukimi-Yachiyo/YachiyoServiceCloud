#如果要修改数据库源和地址和端口,或者nacos的命名等在app.env配置文件中修改 

#主要的配置文件
DB_DRIVER //切换数据库
例子DB_SOURCE = "postgres://root:123456@localhost:5432/root?sslmode=disable"
1.postgres是数据库源，要和DB_DRIVER匹配
2.root是用户名 123456是密码 切换用户进行修改
3.local:5432是数据库地址与端口
4.地址与端口后的root是要连接的数据库名
5.?sslmode=disable 本地数据库关闭ssl连接

SERVER_ADDRESS = "0.0.0.0:8890"
开启监听的地址与端口

#api部分
均以post方式进行提交
接口1:http://127.0.0.1:8890/internal/change    修改指定用户的积分
请求格式为json 举例为 {
    "id": 1,
    "score": 88
   }  
成功返回200与字段"success"，失败返回400(比如用户不存在，积分修改不合法)

接口2:http://127.0.0.1:8890/api/v3/get          获取指定用户的积分
请求格式为json 举例为 {
    "id": 1,
}
成功返回200与用户积分，失败返回200

地址端口对应app.env中的SERVER_ADDRESS


#数据库表的建立和迁移
初始的表名默认为"YachiyoCup2026"，有id和score字段不能为空
如果要创建新表,
1.在db的migration中定义表的结构与回滚方式
2.执行makefile文件中的migrateup(命令行执行make migrateup),路径可以在文件中修改
