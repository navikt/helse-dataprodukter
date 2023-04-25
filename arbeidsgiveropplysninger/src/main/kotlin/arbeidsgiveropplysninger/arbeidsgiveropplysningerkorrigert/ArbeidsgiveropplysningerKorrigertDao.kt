package arbeidsgiveropplysninger.arbeidsgiveropplysningerkorrigert

import kotliquery.queryOf
import kotliquery.sessionOf
import org.intellij.lang.annotations.Language
import javax.sql.DataSource

class ArbeidsgiveropplysningerKorrigertDao(
    private val dataSource: DataSource
) {
    fun lagre(arbeidsgiveropplysningerKorrigertDto: ArbeidsgiveropplysningerKorrigertDto) =
        sessionOf(dataSource).use { session ->
            @Language("PostgreSQL")
            val statement =
                "INSERT INTO arbeidsgiveropplysninger_korrigert(id, korrigert_inntektsmelding_id, korrigerende_inntektsopplysning_id, korrigerende_inntektektsopplysningstype, opprettet) VALUES(?, ?, ?, ?, ?)"
            session.run(
                queryOf(
                    statement,
                    arbeidsgiveropplysningerKorrigertDto.id,
                    arbeidsgiveropplysningerKorrigertDto.korrigertInntektsmeldingId,
                    arbeidsgiveropplysningerKorrigertDto.korrigerendeInntektsopplysningId,
                    arbeidsgiveropplysningerKorrigertDto.korrigerendeInntektektsopplysningstype.name,
                    arbeidsgiveropplysningerKorrigertDto.opprettet
                ).asExecute
            )
        }
}
