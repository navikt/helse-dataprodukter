package no.nav.helse.arbeidsgiveropplysninger

import net.logstash.logback.argument.StructuredArguments
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.rapids_rivers.asLocalDateTime
import no.nav.helse.rapids_rivers.toUUID
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.UUID

internal class InntektsmeldingHåndtertRiver(
    rapidsConnection: RapidsConnection,
    private val inntektsmeldingHåndtertDao: InntektsmeldingHåndtertDao,
) : River.PacketListener {

    private companion object {
        private val logg: Logger = LoggerFactory.getLogger(InntektsmeldingHåndtertRiver::class.java)
    }

    init {
        River(rapidsConnection).apply {
            validate {
                it.demandValue("@event_name", "inntektsmelding_håndtert")
                it.requireKey(
                    "vedtaksperiodeId",
                    "inntektsmeldingId",
                    "@opprettet"
                )
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val vedtaksperiodeId = packet["vedtaksperiodeId"].asText().toUUID()
        val inntektsmeldingId = packet["inntektsmeldingId"].asText().toUUID()
        val opprettet = packet["@opprettet"].asLocalDateTime()

        val inntektsmeldingHåndtert = InntektsmeldingHåndtertDto(
            id = UUID.randomUUID(),
            vedtaksperiodeId = vedtaksperiodeId,
            inntektsmeldingId = inntektsmeldingId,
            opprettet = opprettet
        )
        logg.info(
            "Mottok melding om at en vedtaksperiode har håndtert en inntektsmelding: {}, {}, {}",
            StructuredArguments.kv("vedtaksperiodeId", vedtaksperiodeId),
            StructuredArguments.kv("inntektsmeldingId", inntektsmeldingId),
            StructuredArguments.kv("opprettet", opprettet)
        )
        inntektsmeldingHåndtertDao.lagre(inntektsmeldingHåndtert)

        logg.info("Lagret kobling mellom vedtaksperiode og inntektsmelding: {}, {}",
            StructuredArguments.kv("vedtaksperiodeId", vedtaksperiodeId),
            StructuredArguments.kv("inntektsmeldingId", inntektsmeldingId),
        )
    }
}