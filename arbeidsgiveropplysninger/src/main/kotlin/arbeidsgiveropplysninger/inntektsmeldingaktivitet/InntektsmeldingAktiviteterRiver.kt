package arbeidsgiveropplysninger.inntektsmeldingaktivitet

import arbeidsgiveropplysninger.inntektsmeldinghåndtert.InntektsmeldingHåndtertRiver
import com.fasterxml.jackson.databind.JsonNode
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.rapids_rivers.asLocalDateTime
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.UUID

private val relevanteVarselkoder = listOf(
    "RV_IM_2",  // Første fraværsdag i inntektsmeldingen er ulik skjæringstidspunktet. Kontrollér at inntektsmeldingen er knyttet til riktig periode.
    "RV_IM_3",  // Inntektsmeldingen og vedtaksløsningen er uenige om beregningen av arbeidsgiverperioden. Undersøk hva som er riktig arbeidsgiverperiode.
    "RV_IM_4",  // Det er mottatt flere inntektsmeldinger på samme skjæringstidspunkt. Undersøk at arbeidsgiverperioden, sykepengegrunnlaget og refusjonsopplysningene er riktige
    "RV_IM_5",  // Sykmeldte har oppgitt ferie første dag i arbeidsgiverperioden.
    "RV_IM_6",  // Inntektsmelding inneholder ikke beregnet inntekt
    "RV_IM_7",  // Brukeren har opphold i naturalytelser TODO: Skal vi ta med denne?
    "RV_IM_8",  // Arbeidsgiver har redusert utbetaling av arbeidsgiverperioden TODO: Skal vi ta med denne?
    "RV_IM_22"  // Det er mottatt flere inntektsmeldinger på kort tid for samme arbeidsgiver
)

private val relevanteNivåer = listOf(
    "VARSEL",
    "FUNKSJONELL_FEIL",
    "LOGISK_FEIL"
)

internal class InntektsmeldingAktiviteterRiver(
    rapidsConnection: RapidsConnection,
    private val inntektsmeldingAktivitetDao: InntektsmeldingAktivitetDao
) : River.PacketListener {

    private val logg: Logger = LoggerFactory.getLogger(InntektsmeldingHåndtertRiver::class.java)

    init {
        River(rapidsConnection).apply {
            validate {
                it.demandValue("@event_name", "aktivitetslogg_ny_aktivitet")
                it.demand("aktiviteter", ::minstÉnAktivitetMedRelevantVarsel)
                it.demand("@forårsaket_av", ::forårsaketAvInntektsmelding) // TODO: bestemme om vi skal filtrere på foråraketAv eller kontekster
                it.requireKey("@id", "@forårsaket_av.id")
                it.requireArray("aktiviteter") {
                    requireKey("nivå", "melding")
                    require("id") { id -> UUID.fromString(id.asText()) }
                    require("tidsstempel", JsonNode::asLocalDateTime)
                    interestedIn("varselkode")
                }
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        logg.info("Mottok melding som inneholder relevant varselkode")
        val inntektsmeldingId = UUID.fromString(packet["@forårsaket_av"]["id"].asText())
        val aktiviteter = packet["aktiviteter"]
            .beholdRelevanteAktiviteter()
            .asAktiviteter(inntektsmeldingId)

        aktiviteter.forEach {
            inntektsmeldingAktivitetDao.lagre(it)
        }

        logg.info("Lagret aktivteter på inntektsmelding med relevante varselkoder")
    }

    private fun minstÉnAktivitetMedRelevantVarsel(aktiviteter: JsonNode) {
        aktiviteter
            .beholdRelevanteAktiviteter()
            .ifEmpty { throw IllegalArgumentException("Ingen varsler eller feil knyttet til inntektsmelding") }
    }

    private fun forårsaketAvInntektsmelding(forårsaketAv: JsonNode) {
        val eventName = forårsaketAv["event_name"].asText()
        if(eventName != "inntektsmelding") {
            logg.info("Fant en aktivitet med relevant varsel som ikke er forårsaket av en inntektsmelding, men en $eventName")
            throw IllegalArgumentException("Kun interessert i aktiviteter på inntektsmelding")
        }
    }
}

private fun JsonNode.beholdRelevanteAktiviteter() = filter { it["nivå"].asText() in relevanteNivåer && it["varselkode"].asText() in relevanteVarselkoder }

private fun List<JsonNode>.asAktiviteter(inntektsmeldingId: UUID): List<InntektsmeldingAktivitetDto> = map {
    InntektsmeldingAktivitetDto(
        id = UUID.fromString(it["id"].asText()),
        inntektsmeldingId = inntektsmeldingId,
        varselkode = it["varselkode"]?.asText(),
        nivå = it["nivå"].asText(),
        melding = it["melding"].asText(),
        tidsstempel = it["tidsstempel"].asLocalDateTime()
    )
}