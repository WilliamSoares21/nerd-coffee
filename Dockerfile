FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jdk
WORKDIR /app
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
COPY --from=builder /app/target/coffenerd-*.jar app.jar

# Health check
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health/liveness || exit 1

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
