CREATE TABLE arbeidsgiveropplysninger_korrigert
(
    id                                      UUID PRIMARY KEY,
    korrigert_inntektsmelding_id            UUID      NOT NULL,
    korrigerende_inntektsopplysning_id      UUID      NOT NULL,
    korrigerende_inntektektsopplysningstype VARCHAR   NOT NULL,
    opprettet                               TIMESTAMP NOT NULL
);
