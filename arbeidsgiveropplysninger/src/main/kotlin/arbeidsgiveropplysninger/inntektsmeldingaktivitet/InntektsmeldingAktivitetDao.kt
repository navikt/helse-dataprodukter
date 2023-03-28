package arbeidsgiveropplysninger.inntektsmeldingaktivitet

import kotliquery.queryOf
import kotliquery.sessionOf
import org.intellij.lang.annotations.Language
import javax.sql.DataSource

class InntektsmeldingAktivitetDao(
    private val dataSource: DataSource
) {

    fun lagre(aktivitet: InntektsmeldingAktivitetDto): Boolean =
        sessionOf(dataSource).use { session ->
            @Language("PostgreSQL")
            val statement =
                "INSERT INTO inntektsmelding_aktivitet(id, hendelse_id, varselkode, nivaa, melding, tidsstempel) VALUES(?, ?, ?, ?, ?, ?) ON CONFLICT DO NOTHING"
            return session.run(
                queryOf(
                    statement,
                    aktivitet.id,
                    aktivitet.hendelseId,
                    aktivitet.varselkode,
                    aktivitet.nivå,
                    aktivitet.melding,
                    aktivitet.tidsstempel,
                ).asExecute
            )
        }
}
