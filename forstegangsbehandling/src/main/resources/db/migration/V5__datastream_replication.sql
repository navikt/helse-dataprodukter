DO
$$
    BEGIN
        if not exists
            (select 1 from pg_replication_slots where slot_name = 'dataprodukt_forstegangsbehandling_replication')
        then
            PERFORM PG_CREATE_LOGICAL_REPLICATION_SLOT ('dataprodukt_forstegangsbehandling_replication', 'pgoutput');
        end if;
    end;
$$;
