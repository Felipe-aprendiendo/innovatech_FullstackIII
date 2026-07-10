# ── Stage 1: Build ─────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17-alpine AS builder

WORKDIR /build

COPY pom.xml .
COPY projects-service/pom.xml projects-service/pom.xml

RUN mvn dependency:go-offline -pl projects-service -q --no-transfer-progress

COPY projects-service/src projects-service/src
RUN mvn package -pl projects-service -DskipTests -q --no-transfer-progress

# ── Stage 2: Runtime ────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine AS runtime

LABEL maintainer="InnovaTech <dev@innovatech.cl>"
LABEL service="projects-service"
LABEL version="1.0.0"

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app

COPY --from=builder /build/projects-service/target/projects-service-*.jar app.jar
RUN chown appuser:appgroup app.jar
RUN mkdir -p /app/logs && chown -R appuser:appgroup /app/logs

USER appuser
EXPOSE 8003

ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"

HEALTHCHECK --interval=30s --timeout=10s --start-period=90s --retries=3 \
  CMD wget -qO- http://localhost:8003/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
