# LettersInTime Dockerfile
# 用于代码挂载部署：代码在宿主机，容器内编译运行
# 
# 特点：
# - 宿主机通过 Git 管理代码
# - 容器提供编译和运行环境
# - Maven 依赖缓存加速构建

FROM maven:3.8-openjdk-11

# 设置工作目录
WORKDIR /app

# 安装 curl 用于健康检查
RUN apt-get update && \
    apt-get install -y curl && \
    rm -rf /var/lib/apt/lists/*

# 暴露应用端口
EXPOSE 8080

# 设置时区
ENV TZ=Asia/Shanghai

# 创建启动脚本
RUN echo '#!/bin/bash\n\
set -e\n\
echo "================================="\n\
echo "  LettersInTime Container Started"\n\
echo "================================="\n\
echo ""\n\
echo "📂 检查代码目录..."\n\
if [ ! -f "pom.xml" ]; then\n\
    echo "❌ 错误: 未找到 pom.xml 文件"\n\
    echo "请确保项目代码已挂载到 /app 目录"\n\
    exit 1\n\
fi\n\
echo "✅ 代码目录检查通过"\n\
echo ""\n\
echo "📦 开始 Maven 构建..."\n\
mvn clean package -DskipTests -B\n\
echo ""\n\
echo "✅ 构建完成"\n\
echo ""\n\
echo "🚀 启动应用..."\n\
java -Xms256m -Xmx512m -Djava.security.egd=file:/dev/./urandom -jar target/*.jar\n\
' > /entrypoint.sh && chmod +x /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]

