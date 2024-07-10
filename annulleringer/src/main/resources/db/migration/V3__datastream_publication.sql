DO
$$
    BEGIN
        if not exists
            (select 1 from pg_publication where pubname = 'dataprodukt_annulleringer_publication')
        then
            CREATE PUBLICATION dataprodukt_annulleringer_publication for ALL TABLES;
        end if;
    end;
$$;
