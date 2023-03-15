package no.nav.helse.arbeidsgiveropplysninger

import arbeidsgiveropplysninger.inntektsmeldingaktivitet.InntektsmeldingAktivitetDto
import java.time.LocalDateTime
import java.util.UUID

internal fun mockInntektsmelingAktiviteter(inntektsmeldingId: UUID = UUID.randomUUID()) = listOf(
    InntektsmeldingAktivitetDto(
        id = UUID.randomUUID(),
        inntektsmeldingId = inntektsmeldingId,
        varselkode = "RV_HE_1",
        nivå = "VARSEL",
        melding = "Dette er en melding",
        tidsstempel = LocalDateTime.now()
    ),
    InntektsmeldingAktivitetDto(
        id = UUID.randomUUID(),
        inntektsmeldingId = inntektsmeldingId,
        varselkode = "RV_HE_2",
        nivå = "VARSEL",
        melding = "Dette er en melding",
        tidsstempel = LocalDateTime.now()
    )
)
