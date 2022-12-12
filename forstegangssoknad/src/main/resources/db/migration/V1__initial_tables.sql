CREATE TABLE person
(
    id         SERIAL,
    fnr    CHAR(11) NOT NULL,
    organisasjonsnummer VARCHAR(11) NOT NULL ,
    opprettet  TIMESTAMP NOT NULL,
    PRIMARY KEY (fnr, organisasjonsnummer)
);