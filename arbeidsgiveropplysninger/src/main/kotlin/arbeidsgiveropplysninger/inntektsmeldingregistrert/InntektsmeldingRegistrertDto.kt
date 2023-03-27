package arbeidsgiveropplysninger.inntektsmeldingregistrert

import java.time.LocalDateTime
import java.util.UUID

data class InntektsmeldingRegistrertDto(
    internal val id: UUID,
    internal val hendelseId: UUID,
    internal val dokumentId: UUID,
    internal val opprettet: LocalDateTime
)