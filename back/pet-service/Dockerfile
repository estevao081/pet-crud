FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copiar apenas o pom.xml primeiro para aproveitar o cache do Docker
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine

# Usuário não-root
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app
COPY --from=builder /app/target/pet-service.jar app.jar
RUN chown appuser:appgroup app.jar
USER appuser

EXPOSE 8083

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
