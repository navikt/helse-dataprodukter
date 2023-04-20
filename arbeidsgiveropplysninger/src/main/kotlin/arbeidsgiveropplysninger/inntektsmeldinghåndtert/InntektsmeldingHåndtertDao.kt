package arbeidsgiveropplysninger.inntektsmeldinghåndtert

import kotliquery.queryOf
import kotliquery.sessionOf
import org.intellij.lang.annotations.Language
import java.util.*
import javax.sql.DataSource

class InntektsmeldingHåndtertDao(
    private val dataSource: DataSource
) {
    fun lagre(inntektsmeldingHåndtertDto: InntektsmeldingHåndtertDto) =
        sessionOf(dataSource).use { session ->
            @Language("PostgreSQL")
            val statement =
                "INSERT INTO inntektsmelding_haandtert(id, vedtaksperiode_id, hendelse_id, opprettet) VALUES(?, ?, ?, ?)"
            session.run(
                queryOf(
                    statement,
                    inntektsmeldingHåndtertDto.id,
                    inntektsmeldingHåndtertDto.vedtaksperiodeId,
                    inntektsmeldingHåndtertDto.hendelseId,
                    inntektsmeldingHåndtertDto.opprettet
                ).asExecute
            )
        }

    fun finnHendelseId(vedtaksperiodeId: UUID) =
        sessionOf(dataSource).use { session ->
            @Language("PostgreSQL")
            val statement = "SELECT hendelse_id FROM inntektsmelding_haandtert WHERE vedtaksperiode_id=? ORDER BY opprettet DESC LIMIT 1"
            session.run(
                queryOf(
                    statement,
                    vedtaksperiodeId
                ).map { it.uuid("hendelse_id") }.asSingle
            )
        }

}