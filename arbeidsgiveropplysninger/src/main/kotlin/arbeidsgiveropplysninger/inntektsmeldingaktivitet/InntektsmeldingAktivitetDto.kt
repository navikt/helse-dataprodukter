package arbeidsgiveropplysninger.inntektsmeldingaktivitet

import java.time.LocalDateTime
import java.util.UUID

data class InntektsmeldingAktivitetDto(
    internal val id: UUID,
    internal val inntektsmeldingId: UUID,
    internal val varselkode: String,
    internal val nivå: String,
    internal val melding: String,
    internal val tidsstempel: LocalDateTime
)