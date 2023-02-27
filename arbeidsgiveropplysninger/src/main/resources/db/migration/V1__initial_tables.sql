CREATE TABLE haandtert_inntektsmelding(
    id UUID PRIMARY KEY,
    vedtaksperiode_id UUID NOT NULL,
    inntektsmelding_id UUID NOT NULL,
    opprettet TIMESTAMP NOT NULL
)