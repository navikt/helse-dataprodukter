CREATE TABLE inntektsmelding_aktivitet
(
    id                 UUID      PRIMARY KEY,
    inntektsmelding_id UUID      NOT NULL,
    varselkode         VARCHAR   NOT NULL,
    nivaa              VARCHAR   NOT NULL,
    melding            VARCHAR   NOT NULL,
    tidsstempel        TIMESTAMP NOT NULL
);
