package arbeidsgiveropplysninger.inntektsmeldingaktivitet

import kotliquery.queryOf
import kotliquery.sessionOf
import org.intellij.lang.annotations.Language
import javax.sql.DataSource

class InntektsmeldingAktivitetDao(
    private val dataSource: DataSource
) {

    // TODO: skal vi lagre et innslag hvis inntektsmeldingId OG varselkode er lik noe i databasen fra før av?
    fun lagre(aktivitet: InntektsmeldingAktivitetDto): Boolean = sessionOf(dataSource).use { session ->
        @Language("PostgreSQL")
        val statement =
            "INSERT INTO inntektsmelding_aktivitet(id, inntektsmelding_id, varselkode, nivaa, melding, tidsstempel) VALUES(?, ?, ?, ?, ?, ?)"
        session.run(
            queryOf(
                statement,
                aktivitet.id,
                aktivitet.inntektsmeldingId,
                aktivitet.varselkode,
                aktivitet.nivå,
                aktivitet.melding,
                aktivitet.tidsstempel,
            ).asExecute
        )
    }
}