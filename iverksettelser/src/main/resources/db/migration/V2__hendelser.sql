CREATE TABLE hendelse(
    hendelse_id UUID NOT NULL,
    vedtaksperiode_id UUID NOT NULL,
    PRIMARY KEY (vedtaksperiode_id, hendelse_id)
);