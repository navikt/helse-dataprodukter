DO
$$
    BEGIN
        if not exists
            (select 1 from pg_publication where pubname = 'dataprodukt_forstegangsbehandling_publication')
        then
            CREATE PUBLICATION dataprodukt_forstegangsbehandling_publication for ALL TABLES;
        end if;
    end;
$$;
