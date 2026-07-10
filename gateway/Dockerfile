# ── Stage 1: Build ─────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17-alpine AS builder

WORKDIR /build

# POMs primero para aprovechar cache de capas
COPY pom.xml .
COPY gateway/pom.xml gateway/pom.xml

RUN mvn dependency:go-offline -pl gateway -q --no-transfer-progress

COPY gateway/src gateway/src
RUN mvn package -pl gateway -DskipTests -q --no-transfer-progress

# ── Stage 2: Runtime ────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine AS runtime

LABEL maintainer="InnovaTech <dev@innovatech.cl>"
LABEL service="gateway"
LABEL version="1.0.0"

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app

COPY --from=builder /build/gateway/target/gateway-*.jar app.jar
RUN chown appuser:appgroup app.jar

USER appuser
EXPOSE 8000

ENV JAVA_OPTS="-Xms128m -Xmx256m -XX:+UseG1GC"

HEALTHCHECK --interval=30s --timeout=10s --start-period=90s --retries=3 \
  CMD wget -qO- http://localhost:8000/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
