#!/bin/bash
# Executado pelo container do Postgres apenas na PRIMEIRA inicialização (volume vazio).
# Um banco por microserviço: nenhum serviço enxerga as tabelas do outro.
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname postgres <<-EOSQL
    CREATE DATABASE ${USER_DB:-user_db} OWNER "$POSTGRES_USER";
    CREATE DATABASE ${PET_DB:-pet_db} OWNER "$POSTGRES_USER";
EOSQL
