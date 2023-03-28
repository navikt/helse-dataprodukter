package no.nav.helse.arbeidsgiveropplysninger

import arbeidsgiveropplysninger.inntektsmeldingaktivitet.InntektsmeldingAktivitetDto
import java.time.LocalDateTime
import java.util.UUID

internal fun mockInntektsmeldingAktivitet(hendelseId: UUID = UUID.randomUUID(), varselkode: String) =
    InntektsmeldingAktivitetDto(
        id = UUID.randomUUID(),
        hendelseId = hendelseId,
        varselkode = varselkode,
        nivå = "VARSEL",
        melding = "Dette er en melding",
        tidsstempel = LocalDateTime.now()
    )

internal fun mockInntektsmelingAktiviteter(hendelseId: UUID = UUID.randomUUID()) = listOf(
    mockInntektsmeldingAktivitet(hendelseId = hendelseId, varselkode = "RV_IM_1"),
    mockInntektsmeldingAktivitet(hendelseId = hendelseId, varselkode = "RV_IM_2"),
)
