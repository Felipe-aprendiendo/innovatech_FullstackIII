# ── Stage 1: Build ────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app
COPY pom.xml .
COPY .mvn/ .mvn/
COPY mvnw .
RUN chmod +x mvnw && ./mvnw dependency:go-offline -q

COPY src ./src
RUN ./mvnw package -DskipTests -q

# ── Stage 2: Runtime ───────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine AS runtime

LABEL maintainer="InnovaTech <dev@innovatech.cl>"
LABEL service="auth-service"
LABEL version="1.0.0"

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app

COPY --from=builder /app/target/auth-service-*.jar app.jar
RUN chown appuser:appgroup app.jar
RUN mkdir -p /app/logs && chown -R appuser:appgroup /app/logs

USER appuser
EXPOSE 8001

ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"
ENV DB_HOST=mysql-auth
ENV DB_PORT=3306
ENV DB_NAME=innovatech_auth
ENV DB_USERNAME=innovatech
ENV DB_PASSWORD=changeme
ENV REDIS_HOST=redis
ENV REDIS_PORT=6379
ENV USERS_SERVICE_URL=http://users-service:8002
ENV JWT_SECRET=changeme-replace-in-production

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget -qO- http://localhost:8001/api/v1/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
