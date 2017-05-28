#!/usr/bin/env bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
    CREATE USER ipc_user WITH SUPERUSER;
    GRANT ALL PRIVILEGES ON DATABASE $POSTGRES_DB TO ipc_user;
    ALTER USER ipc_user WITH PASSWORD 'ipc_user';

    CREATE USER developer WITH SUPERUSER;
    GRANT ALL PRIVILEGES ON DATABASE $POSTGRES_DB TO developer;
    ALTER USER developer WITH PASSWORD 'developer';
    ALTER USER developer set default_transaction_read_only = on;
EOSQL