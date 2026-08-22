#!/bin/bash
set -e

PGDATA="/var/lib/postgresql/data"
PG_BIN="/usr/lib/postgresql/${PG_VERSION}/bin"

mkdir -p "${PGDATA}"
chown -R postgres:postgres /var/lib/postgresql /var/run/postgresql

# Inicializacao do PostgreSQL (apenas na primeira execucao do container,
# ou seja, quando o diretorio de dados (PGDATA) ainda esta vazio - o que
# tambem acontece sempre que um volume novo/vazio e montado nesse caminho)
if [ ! -f "${PGDATA}/PG_VERSION" ]; then
  echo ">> [entrypoint] Primeira execucao: inicializando o PostgreSQL..."

  su postgres -c "${PG_BIN}/initdb -D ${PGDATA} --auth=trust --no-locale --encoding=UTF8"

  su postgres -c "${PG_BIN}/pg_ctl -D ${PGDATA} -l /var/log/postgresql/startup.log start"

  # Aguarda o Postgres aceitar conexoes
  until su postgres -c "${PG_BIN}/pg_isready -q"; do
    sleep 1
  done

  su postgres -c "psql --command \"CREATE USER ${POSTGRES_USER} WITH SUPERUSER PASSWORD '${POSTGRES_PASSWORD}';\""
  su postgres -c "createdb -O ${POSTGRES_USER} ${POSTGRES_DB}"
  su postgres -c "psql -U ${POSTGRES_USER} -d ${POSTGRES_DB} -f /docker-entrypoint-initdb.d/init.sql"

  su postgres -c "${PG_BIN}/pg_ctl -D ${PGDATA} stop"

  echo ">> [entrypoint] PostgreSQL inicializado com sucesso."
else
  echo ">> [entrypoint] Dados do PostgreSQL ja existentes, pulando inicializacao."
fi

# A partir daqui, o supervisord assume o controle dos 3 processos:
# postgres, backend (java) e nginx (frontend)
exec /usr/bin/supervisord -n -c /etc/supervisor/conf.d/supervisord.conf
