DELETE FROM inntektsmelding_aktivitet AS a1 USING inntektsmelding_aktivitet AS a2 WHERE (
    a1.inntektsmelding_id = a2.inntektsmelding_id AND a1.varselkode = a2.varselkode AND a1.tidsstempel > a2.tidsstempel
);