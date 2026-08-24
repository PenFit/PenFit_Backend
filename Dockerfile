FROM eclipse-temurin:17-jdk-jammy AS builder
WORKDIR /workspace

COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle
RUN chmod +x gradlew && ./gradlew --no-daemon dependencies > /dev/null 2>&1 || true

COPY src src
RUN ./gradlew --no-daemon clean bootJar -x test

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

RUN apt-get update \
 && apt-get install -y --no-install-recommends curl tzdata \
 && rm -rf /var/lib/apt/lists/* \
 && groupadd --system penfit \
 && useradd --system --gid penfit --home /app penfit

COPY --from=builder --chown=penfit:penfit /workspace/build/libs/*.jar /app/app.jar

USER penfit
EXPOSE 8080

ENV TZ=Asia/Seoul \
    SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS="-XX:MaxRAMPercentage=70 -Duser.timezone=Asia/Seoul"

HEALTHCHECK --interval=15s --timeout=5s --start-period=60s --retries=5 \
  CMD curl -fsS http://localhost:8080/actuator/health/readiness || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
