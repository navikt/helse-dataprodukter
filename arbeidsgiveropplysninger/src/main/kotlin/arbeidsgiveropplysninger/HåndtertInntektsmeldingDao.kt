package arbeidsgiveropplysninger

import kotliquery.queryOf
import kotliquery.sessionOf
import org.intellij.lang.annotations.Language
import javax.sql.DataSource

class HåndtertInntektsmeldingDao(
    private val dataSource: DataSource
) {
    fun lagre(håndtertInntektsmeldingDto: HåndtertInntektsmeldingDto) =
        sessionOf(dataSource).use { session ->
            @Language("PostgreSQL")
            val statement =
                "INSERT INTO haandtert_inntektsmelding(id, vedtaksperiode_id, inntektsmelding_id, opprettet) VALUES(?, ?, ?, ?)"
            session.run(
                queryOf(
                    statement,
                    håndtertInntektsmeldingDto.id,
                    håndtertInntektsmeldingDto.vedtaksperiodeId,
                    håndtertInntektsmeldingDto.inntektsmeldingId,
                    håndtertInntektsmeldingDto.opprettet
                ).asExecute
            )
        }


}