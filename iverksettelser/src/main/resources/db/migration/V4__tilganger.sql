
DO $$ BEGIN
    IF EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'cloudsqliamuser')
    THEN
        GRANT ALL ON ALL TABLES IN SCHEMA public TO cloudsqliamuser;
        GRANT ALL ON ALL SEQUENCES IN SCHEMA public TO cloudsqliamuser;
    END IF;
END $$;
