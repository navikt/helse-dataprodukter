CREATE TABLE vedtak_fattet(
    vedtaksperiode_id UUID NOT NULL PRIMARY KEY,
    hendelse_id UUID NOT NULL,
    utbetaling_id UUID,
    korrelasjon_id UUID,
    fattet_tidspunkt TIMESTAMP NOT NULL
);

CREATE TABLE utbetaling(
    korrelasjon_id UUID NOT NULL PRIMARY KEY,
    arbeidsgiver_fagsystemid VARCHAR NOT NULL UNIQUE,
    person_fagsystemid VARCHAR NOT NULL UNIQUE,
    annullert BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE utbetalingsversjon(
    utbetaling_id UUID NOT NULL PRIMARY KEY,
    utbetaling_ref UUID REFERENCES utbetaling(korrelasjon_id)
);