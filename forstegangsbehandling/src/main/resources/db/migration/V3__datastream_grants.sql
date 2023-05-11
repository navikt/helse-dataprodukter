DO
$$
    BEGIN
        IF EXISTS
            (SELECT 1 from pg_roles where rolname = 'dataprodukt-forstegangsbehandling')
        THEN
            ALTER USER "dataprodukt-forstegangsbehandling" WITH REPLICATION;
        END IF;
    END
$$;
DO
$$
    BEGIN
        IF EXISTS
            (SELECT 1 from pg_roles where rolname = 'bigquery_datastream')
        THEN
            ALTER USER "bigquery_datastream" WITH REPLICATION;
            ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO "bigquery_datastream";
            GRANT USAGE ON SCHEMA public TO "bigquery_datastream";
            GRANT SELECT ON ALL TABLES IN SCHEMA public TO "bigquery_datastream";
        END IF;
    END
$$;