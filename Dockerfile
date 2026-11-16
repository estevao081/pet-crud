# Pet Crud - imagem unica (frontend + 4 microservicos + PostgreSQL). Permite rodar toda a aplicacao com um unico `docker run`.

FROM maven:3.9.9-eclipse-temurin-21 AS backend-builder
WORKDIR /services
# Um layer de dependências por serviço (cache), depois o código-fonte
COPY back/gateway/pom.xml gateway/pom.xml
COPY back/auth-service/pom.xml auth-service/pom.xml
COPY back/user-service/pom.xml user-service/pom.xml
COPY back/pet-service/pom.xml pet-service/pom.xml
RUN for s in gateway auth-service user-service pet-service; do (cd $s && mvn -B dependency:go-offline) || exit 1; done
COPY back/gateway/src gateway/src
COPY back/auth-service/src auth-service/src
COPY back/user-service/src user-service/src
COPY back/pet-service/src pet-service/src
RUN for s in gateway auth-service user-service pet-service; do (cd $s && mvn -B clean package -DskipTests) || exit 1; done

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
    POSTGRES_USER=meuuser \
    POSTGRES_PASSWORD=minhasenha \
    USER_DB=user_db \
    PET_DB=pet_db \
    SPRING_JPA_HIBERNATE_DDL_AUTO=update \
    SECRET_KEY=mySecretKey123 \
    PUBLIC_BASE_URL= \
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

COPY --from=backend-builder /services/gateway/target/gateway.jar /app/gateway.jar
COPY --from=backend-builder /services/auth-service/target/auth-service.jar /app/auth-service.jar
COPY --from=backend-builder /services/user-service/target/user-service.jar /app/user-service.jar
COPY --from=backend-builder /services/pet-service/target/pet-service.jar /app/pet-service.jar

RUN rm -f /etc/nginx/sites-enabled/default
COPY --from=frontend-builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

COPY supervisord.conf /etc/supervisor/conf.d/supervisord.conf
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh && \
    mkdir -p /var/log/supervisor /var/run/postgresql /var/log/postgresql /var/lib/postgresql/data && \
    chown -R postgres:postgres /var/run/postgresql /var/log/postgresql /var/lib/postgresql

EXPOSE 80

# Volume opcional para persistir os dados do Postgres entre execucoes do mesmo nome de volume/container (veja instrucoes de uso no README).
VOLUME ["/var/lib/postgresql/data"]

ENTRYPOINT ["/entrypoint.sh"]
