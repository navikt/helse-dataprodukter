package arbeidsgiveropplysninger.arbeidsgiveropplysningerkorrigert

import java.time.LocalDateTime
import java.util.UUID

data class ArbeidsgiveropplysningerKorrigertDto(
    internal val id: UUID,
    internal val korrigertInntektsmeldingId: UUID,
    internal val korrigerendeInntektsopplysningId: UUID,
    internal val korrigerendeInntektektsopplysningstype: KorrigerendeInntektektsopplysningstype,
    internal val opprettet: LocalDateTime
) {
    enum class KorrigerendeInntektektsopplysningstype {
        SAKSBEHANDLER,
        INNTEKTSMELDING
    }
}