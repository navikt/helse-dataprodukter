package arbeidsgiveropplysninger.inntektsmeldingaktivitet

import arbeidsgiveropplysninger.inntektsmeldinghåndtert.InntektsmeldingHåndtertDao
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
import java.util.*

private const val avvikVarselkode = "RV_IV_2"   // Har mer enn 25 % avvik. Dette støttes foreløpig ikke i Speil. Du må derfor annullere periodene.

private val relevanteNivåer = listOf(
    "VARSEL",
    "FUNKSJONELL_FEIL"
)

internal class SykepengegrunnlagAvvikAktivitetRiver(
    rapidsConnection: RapidsConnection,
    private val inntektsmeldingAktivitetDao: InntektsmeldingAktivitetDao,
    private val inntektsmeldingHåndtertDao: InntektsmeldingHåndtertDao
) : River.PacketListener {

    private val logg: Logger = LoggerFactory.getLogger(SykepengegrunnlagAvvikAktivitetRiver::class.java)

    init {
        River(rapidsConnection).apply {
            validate {
                it.demandValue("@event_name", "aktivitetslogg_ny_aktivitet")
                it.demand("aktiviteter", ::minstÉnAktivitetMedRelevantVarsel)
                it.requireKey("@id")
                it.requireArray("aktiviteter") {
                    requireKey("nivå", "melding")
                    require("id") { id -> UUID.fromString(id.asText()) }
                    require("tidsstempel", JsonNode::asLocalDateTime)
                    interestedIn("varselkode", "kontekster")
                }
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext, metadata: MessageMetadata, meterRegistry: MeterRegistry) {
        logg.info("Mottok melding som inneholder relevant varselkode")
        val aktiviteter = packet["aktiviteter"]
            .beholdRelevanteAktiviteter()

        if (aktiviteter.size > 1) logg.info("Et vilkårsgrunnlag har trigget flere varsler om 25 prosent avvik. Dette er sprøtt(?)")

        val aktivitet = aktiviteter.firstOrNull()
        val vedtaksperiodeId = aktivitet?.finnVedtaksperiodeId()
        if (vedtaksperiodeId == null ) {
            logg.error("Finner ikke vedtaksperiodeId for 25 prosent varselkode på aktivitet ${packet["@id"].asText()}")
            return
        }

        val inntektsmeldingId = inntektsmeldingHåndtertDao.finnHendelseId(vedtaksperiodeId)
        if (inntektsmeldingId == null ) {
            logg.error("Finner ikke inntektsmeldingId som er blitt håndtert av vedtaksperiode $vedtaksperiodeId")
            return
        }

        inntektsmeldingAktivitetDao.lagre(aktivitet.asAktivitet(inntektsmeldingId))

        logg.info("Lagret aktivitet på inntektsmelding med relevant varselkode")
    }

    private fun minstÉnAktivitetMedRelevantVarsel(aktiviteter: JsonNode) {
        aktiviteter
            .beholdRelevanteAktiviteter()
            .ifEmpty { throw IllegalArgumentException("Ingen varsler eller feil med relevant varselkode (25 prosent avvik)") }
    }
}

private fun JsonNode.beholdRelevanteAktiviteter() =
    filter { it["nivå"].asText() in relevanteNivåer && it["varselkode"].asText() == avvikVarselkode }

private fun JsonNode.asAktivitet(inntektsmeldingId: UUID): InntektsmeldingAktivitetDto =
    InntektsmeldingAktivitetDto(
        id = UUID.fromString(this["id"].asText()),
        hendelseId = inntektsmeldingId,
        varselkode = this["varselkode"].asText(),
        nivå = this["nivå"].asText(),
        melding = this["melding"].asText(),
        tidsstempel = this["tidsstempel"].asLocalDateTime()
    )


private fun JsonNode.finnVedtaksperiodeId() =
    get("kontekster")
        ?.firstOrNull { it["konteksttype"].asText() == "Vedtaksperiode" }
        ?.get("kontekstmap")
        ?.get("vedtaksperiodeId")
        ?.let { UUID.fromString(it.asText()) }