FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copiar apenas o pom.xml primeiro para aproveitar o cache do Docker
COPY pom.xml .
RUN mvn dependency:go-offline

# Agora copiar o código-fonte
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine

# Criar um usuário não-root para segurança
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copiar o JAR e renomear
COPY --from=builder /app/target/pet-crud-api-0.0.1-SNAPSHOT.jar app.jar

# Mudar proprietário para o usuário não-root
RUN chown appuser:appgroup app.jar

# Mudar para usuário não-root
USER appuser

EXPOSE 8080

# Usar execução otimizada com opções de JVM
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]