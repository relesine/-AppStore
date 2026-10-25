# 应用商店后端 — 多阶段构建（v1.3 企业级）
# 构建镜像：docker build -t appstore-backend:latest .
FROM maven:3.8.6-jdk-17-slim AS builder
WORKDIR /build
COPY pom.xml .
# 先单独下依赖，利用 Docker 层缓存加速
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
RUN groupadd -r appstore && useradd -r -g appstore appstore \
    && mkdir -p /data/uploads /app/logs && chown -R appstore:appstore /data /app

# 时区与中文支持
ENV TZ=Asia/Shanghai \
    JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/app/logs"

COPY --from=builder /build/target/*.jar app.jar

USER appstore
VOLUME ["/data/uploads", "/app/logs"]
EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD curl -fs http://localhost:8080/api/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar \
  --file.upload-path=/data/uploads/ \
  --springdoc.api-docs.enabled=${SPRINGDOC_ENABLED:-false} \
  --spring.datasource.url='jdbc:mysql://${DB_HOST:-mysql}:${DB_PORT:-3306}/appstore?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true' \
  --spring.datasource.username=${DB_USERNAME:-root} \
  --spring.datasource.password=${DB_PASSWORD:-123456} \
  --spring.redis.host=${REDIS_HOST:-redis}"]
