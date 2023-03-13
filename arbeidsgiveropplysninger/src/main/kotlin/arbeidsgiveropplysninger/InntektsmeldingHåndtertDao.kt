package no.nav.helse.arbeidsgiveropplysninger

import kotliquery.queryOf
import kotliquery.sessionOf
import org.intellij.lang.annotations.Language
import javax.sql.DataSource

class InntektsmeldingHåndtertDao(
    private val dataSource: DataSource
) {
    fun lagre(inntektsmeldingHåndtertDto: InntektsmeldingHåndtertDto) =
        sessionOf(dataSource).use { session ->
            @Language("PostgreSQL")
            val statement =
                "INSERT INTO inntektsmelding_haandtert(id, vedtaksperiode_id, inntektsmelding_id, opprettet) VALUES(?, ?, ?, ?)"
            session.run(
                queryOf(
                    statement,
                    inntektsmeldingHåndtertDto.id,
                    inntektsmeldingHåndtertDto.vedtaksperiodeId,
                    inntektsmeldingHåndtertDto.inntektsmeldingId,
                    inntektsmeldingHåndtertDto.opprettet
                ).asExecute
            )
        }


}