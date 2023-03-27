package arbeidsgiveropplysninger.inntektsmeldingregistrert

import kotliquery.queryOf
import kotliquery.sessionOf
import org.intellij.lang.annotations.Language
import javax.sql.DataSource

class InntektsmeldingRegistrertDao(
    private val dataSource: DataSource
) {
    fun lagre(inntektsmeldingRegistrertDto: InntektsmeldingRegistrertDto) =
        sessionOf(dataSource).use { session ->
            @Language("PostgreSQL")
            val statement =
                "INSERT INTO inntektsmelding_registrert(id, hendelse_id, dokument_id, opprettet) VALUES(?, ?, ?, ?) ON CONFLICT DO NOTHING"
            session.run(
                queryOf(
                    statement,
                    inntektsmeldingRegistrertDto.id,
                    inntektsmeldingRegistrertDto.hendelseId,
                    inntektsmeldingRegistrertDto.dokumentId,
                    inntektsmeldingRegistrertDto.opprettet
                ).asExecute
            )
        }
}
