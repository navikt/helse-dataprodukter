package arbeidsgiveropplysninger.inntektsmeldingaktivitet

import kotliquery.queryOf
import kotliquery.sessionOf
import org.intellij.lang.annotations.Language
import java.util.UUID
import javax.sql.DataSource

class InntektsmeldingAktivitetDao(
    private val dataSource: DataSource
) {

    fun lagre(aktivitet: InntektsmeldingAktivitetDto): Boolean {
        if (!erDuplikat(aktivitet.inntektsmeldingId, aktivitet.varselkode)) {
            sessionOf(dataSource).use { session ->
                @Language("PostgreSQL")
                val statement =
                    "INSERT INTO inntektsmelding_aktivitet(id, inntektsmelding_id, varselkode, nivaa, melding, tidsstempel) VALUES(?, ?, ?, ?, ?, ?)"
                return session.run(
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
        return false
    }

    private fun erDuplikat(inntektsmeldingId: UUID, varselkode: String): Boolean {
        val antallDuplikater = sessionOf(dataSource).use { session ->
            @Language("PostgreSQL")
            val statement =
                "SELECT COUNT(*) FROM inntektsmelding_aktivitet WHERE inntektsmelding_id = ? AND varselkode = ?"
            session.run(
                queryOf(statement, inntektsmeldingId, varselkode)
                    .map { it.int(1) }
                    .asSingle
            )
        }
        return antallDuplikater != null && antallDuplikater > 0
    }
}