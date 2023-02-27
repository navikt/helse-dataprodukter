package arbeidsgiveropplysninger

import java.time.LocalDateTime
import java.util.UUID

data class HåndtertInntektsmeldingDto(
    internal val id: UUID,
    internal val vedtaksperiodeId: UUID,
    internal val inntektsmeldingId: UUID,
    internal val opprettet: LocalDateTime
)