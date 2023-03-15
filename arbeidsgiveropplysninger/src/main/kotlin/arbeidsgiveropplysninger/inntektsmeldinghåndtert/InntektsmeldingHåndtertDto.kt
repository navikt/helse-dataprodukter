package arbeidsgiveropplysninger.inntektsmeldinghåndtert

import java.time.LocalDateTime
import java.util.UUID

data class InntektsmeldingHåndtertDto(
    internal val id: UUID,
    internal val vedtaksperiodeId: UUID,
    internal val inntektsmeldingId: UUID,
    internal val opprettet: LocalDateTime
)