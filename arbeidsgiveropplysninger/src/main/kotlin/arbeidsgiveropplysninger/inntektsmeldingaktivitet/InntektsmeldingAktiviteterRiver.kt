package arbeidsgiveropplysninger.inntektsmeldingaktivitet

import com.fasterxml.jackson.databind.JsonNode
import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers.asLocalDateTime
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageMetadata
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.micrometer.core.instrument.MeterRegistry
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
    "RV_IM_22" // Det er mottatt flere inntektsmeldinger på kort tid for samme arbeidsgiver
)

private val relevanteNivåer = listOf(
    "VARSEL",
    "FUNKSJONELL_FEIL"
)

private val relevantForårsaketAv = listOf(
    "inntektsmelding",
    "inntektsmelding_replay"
)

internal class InntektsmeldingAktiviteterRiver(
    rapidsConnection: RapidsConnection,
    private val inntektsmeldingAktivitetDao: InntektsmeldingAktivitetDao
) : River.PacketListener {

    private val logg: Logger = LoggerFactory.getLogger(InntektsmeldingAktiviteterRiver::class.java)

    init {
        River(rapidsConnection).apply {
            precondition {
                it.requireValue("@event_name", "aktivitetslogg_ny_aktivitet")
                it.require("aktiviteter", ::minstÉnAktivitetMedRelevantVarsel)
                it.require("@forårsaket_av", ::forårsaketAvInntektsmelding) // TODO: bestemme om vi skal filtrere på foråraketAv eller kontekster
            }
            validate {
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

    override fun onPacket(packet: JsonMessage, context: MessageContext, metadata: MessageMetadata, meterRegistry: MeterRegistry) {
        logg.info("Mottok melding som inneholder relevant varselkode")
        val hendelseId = UUID.fromString(packet["@forårsaket_av"]["id"].asText())
        val aktiviteter = packet["aktiviteter"]
            .beholdRelevanteAktiviteter()
            .asAktiviteter(hendelseId)

        aktiviteter.forEach {
            inntektsmeldingAktivitetDao.lagre(it)
        }

        logg.info("Lagret aktiviteter på inntektsmelding med relevante varselkoder")
    }

    private fun minstÉnAktivitetMedRelevantVarsel(aktiviteter: JsonNode) {
        aktiviteter
            .beholdRelevanteAktiviteter()
            .ifEmpty { throw IllegalArgumentException("Ingen varsler eller feil knyttet til inntektsmelding") }
    }

    private fun forårsaketAvInntektsmelding(forårsaketAv: JsonNode) {
        val eventName = forårsaketAv["event_name"].asText()
        if(eventName !in relevantForårsaketAv) {
            logg.info("Fant en aktivitet med relevant varsel som ikke er forårsaket av en inntektsmelding, men $eventName")
            throw IllegalArgumentException("Kun interessert i aktiviteter på inntektsmelding")
        }
    }
}

private fun JsonNode.beholdRelevanteAktiviteter() = filter { it["nivå"].asText() in relevanteNivåer && it["varselkode"].asText() in relevanteVarselkoder }

private fun List<JsonNode>.asAktiviteter(hendelseId: UUID): List<InntektsmeldingAktivitetDto> = map {
    InntektsmeldingAktivitetDto(
        id = UUID.fromString(it["id"].asText()),
        hendelseId = hendelseId,
        varselkode = it["varselkode"].asText(),
        nivå = it["nivå"].asText(),
        melding = it["melding"].asText(),
        tidsstempel = it["tidsstempel"].asLocalDateTime()
    )
}