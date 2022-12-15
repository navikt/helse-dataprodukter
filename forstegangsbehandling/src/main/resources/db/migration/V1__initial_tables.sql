CREATE TABLE person
(
    id                  SERIAL UNIQUE,
    fnr                 CHAR(11)    NOT NULL,
    organisasjonsnummer VARCHAR(11) NOT NULL,
    opprettet           TIMESTAMP   NOT NULL,
    PRIMARY KEY (fnr, organisasjonsnummer)
);


CREATE TABLE søknad
(
    id                    SERIAL,
    person_ref            SERIAL references person (id),
    hendelse_id           UUID      NOT NULL,
    soknad_id             UUID      NOT NULL,
    sykmelding_id         UUID      NOT NULL,
    opprettet             TIMESTAMP NOT NULL,
    fom                   TIMESTAMP NOT NULL,
    tom                   TIMESTAMP NOT NULL,
    arbeid_gjenopptatt    TIMESTAMP,
    forstegangsbehandling BOOLEAN   NOT NULL,
    PRIMARY KEY (hendelse_id)
);