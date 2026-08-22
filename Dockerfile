# Pet Crud - imagem unica (frontend + backend + PostgreSQL). Permite rodar toda a aplicacao com um unico `docker run`.

FROM maven:3.9.9-eclipse-temurin-21 AS backend-builder
WORKDIR /app
COPY back/pom.xml .
RUN mvn dependency:go-offline
COPY back/src ./src
RUN mvn clean package -DskipTests

FROM node:18-alpine AS frontend-builder
WORKDIR /app
COPY front/package*.json ./
RUN npm install
COPY front/ .
# API_BASE vazio -> chamadas relativas (ex: /pets), proxiadas pelo Nginx
ENV VITE_API_URL=""
ENV VITE_APP_NAME="AdotaPet"
RUN npm run build

FROM eclipse-temurin:21-jre-jammy

ENV DEBIAN_FRONTEND=noninteractive \
    POSTGRES_DB=pet_crud \
    POSTGRES_USER=meuuser \
    POSTGRES_PASSWORD=minhasenha \
    POSTGRES_URL=jdbc:postgresql://127.0.0.1:5432/pet_crud \
    SPRING_JPA_HIBERNATE_DDL_AUTO=update \
    SECRET_KEY=mySecretKey123 \
    PG_VERSION=15

RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        curl gnupg2 lsb-release ca-certificates && \
    echo "deb https://apt.postgresql.org/pub/repos/apt $(lsb_release -cs)-pgdg main" \
        > /etc/apt/sources.list.d/pgdg.list && \
    curl -fsSL https://www.postgresql.org/media/keys/ACCC4CF8.asc \
        | gpg --dearmor -o /usr/share/keyrings/postgresql.gpg && \
    sed -i "s#deb https#deb [signed-by=/usr/share/keyrings/postgresql.gpg] https#" /etc/apt/sources.list.d/pgdg.list && \
    apt-get update && \
    apt-get install -y --no-install-recommends \
        postgresql-${PG_VERSION} nginx supervisor && \
    rm -rf /var/lib/apt/lists/* && \
    # Remove o cluster "main" criado automaticamente pelo pacote apt: a
    # inicializacao real do banco (initdb) e feita pelo entrypoint.sh em
    # /var/lib/postgresql/data, permitindo o uso de volume nesse caminho.
    pg_dropcluster --stop ${PG_VERSION} main || true

COPY --from=backend-builder /app/target/pet-crud-api-0.0.1-SNAPSHOT.jar /app/backend.jar

RUN rm -f /etc/nginx/sites-enabled/default
COPY --from=frontend-builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

COPY init.sql /docker-entrypoint-initdb.d/init.sql

COPY supervisord.conf /etc/supervisor/conf.d/supervisord.conf
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh && \
    mkdir -p /var/log/supervisor /var/run/postgresql /var/log/postgresql /var/lib/postgresql/data && \
    chown -R postgres:postgres /var/run/postgresql /var/log/postgresql /var/lib/postgresql

EXPOSE 80

# Volume opcional para persistir os dados do Postgres entre execucoes do mesmo nome de volume/container (veja instrucoes de uso no README).
VOLUME ["/var/lib/postgresql/data"]

ENTRYPOINT ["/entrypoint.sh"]
