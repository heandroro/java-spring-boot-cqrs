-- Create replication user for PostgreSQL streaming replication
-- This script runs on the PRIMARY database during initialization

-- Create replication user
CREATE USER replicator WITH REPLICATION ENCRYPTED PASSWORD 'replicator_password';

-- Create replication slot for the replica
SELECT * FROM pg_create_physical_replication_slot('replica_slot');

-- Grant necessary permissions
GRANT CONNECT ON DATABASE orders_db TO replicator;

-- Create archive directory (if needed)
\! mkdir -p /var/lib/postgresql/archive
