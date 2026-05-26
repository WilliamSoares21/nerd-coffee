FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=builder /app/target/coffenerd-*.jar app.jar

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
    CMD java -cp app.jar -Dloader.main=com.example.HealthCheck org.springframework.boot.loader.PropertiesLauncher || exit 1

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
