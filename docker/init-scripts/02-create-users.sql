-- Create separate users for Command and Query sides
-- This script runs on the PRIMARY database during initialization

-- Create command user (read/write access)
CREATE USER command_user WITH ENCRYPTED PASSWORD 'command_password';
GRANT CONNECT ON DATABASE orders_db TO command_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO command_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO command_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO command_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO command_user;

-- Create query user (read-only access)
CREATE USER query_user WITH ENCRYPTED PASSWORD 'query_password';
GRANT CONNECT ON DATABASE orders_db TO query_user;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO query_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO query_user;
