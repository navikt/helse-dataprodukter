package no.nav.helse.arbeidsgiveropplysninger

import arbeidsgiveropplysninger.inntektsmeldingaktivitet.InntektsmeldingAktivitetDto
import java.time.LocalDateTime
import java.util.UUID

internal fun mockInntektsmeldingAktivitet(inntektsmeldingId: UUID = UUID.randomUUID(), varselkode: String) =
    InntektsmeldingAktivitetDto(
        id = UUID.randomUUID(),
        inntektsmeldingId = inntektsmeldingId,
        varselkode = varselkode,
        nivå = "VARSEL",
        melding = "Dette er en melding",
        tidsstempel = LocalDateTime.now()
    )

internal fun mockInntektsmelingAktiviteter(inntektsmeldingId: UUID = UUID.randomUUID()) = listOf(
    mockInntektsmeldingAktivitet(inntektsmeldingId = inntektsmeldingId, varselkode = "RV_IM_1"),
    mockInntektsmeldingAktivitet(inntektsmeldingId = inntektsmeldingId, varselkode = "RV_IM_2"),
)
