
DO $$ BEGIN
    IF EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'cloudsqliamuser')
    THEN
        ALTER DEFAULT PRIVILEGES FOR USER "dataprodukt-iverksettelser" IN SCHEMA public GRANT ALL PRIVILEGES ON SEQUENCES TO cloudsqliamuser;
        ALTER DEFAULT PRIVILEGES FOR USER "dataprodukt-iverksettelser" IN SCHEMA public GRANT ALL PRIVILEGES ON TABLES TO cloudsqliamuser;
    END IF;
END $$;
