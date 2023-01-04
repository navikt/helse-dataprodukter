CREATE TABLE hendelse(
    hendelse_id UUID NOT NULL,
    vedtaksperiode_id UUID NOT NULL,
    PRIMARY KEY (hendelse_id, vedtaksperiode_id)
);