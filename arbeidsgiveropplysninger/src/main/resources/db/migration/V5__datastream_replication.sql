DO
$$
    BEGIN
        if not exists
            (select 1 from pg_replication_slots where slot_name = 'dataprodukter_arbeidsgiveropplysninger_replication')
        then
            PERFORM PG_CREATE_LOGICAL_REPLICATION_SLOT ('dataprodukter_arbeidsgiveropplysninger_replication', 'pgoutput');
        end if;
    end;
$$;