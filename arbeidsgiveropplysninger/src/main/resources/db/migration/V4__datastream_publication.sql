DO
$$
    BEGIN
        if not exists
            (select 1 from pg_publication where pubname = 'dataprodukter_arbeidsgiveropplysninger_publication')
        then
            CREATE PUBLICATION dataprodukter_arbeidsgiveropplysninger_publication for ALL TABLES;
        end if;
    end;
$$;

