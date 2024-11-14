package arbeidsgiveropplysninger.inntektsmeldinghåndtert

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers.asLocalDateTime
import com.github.navikt.tbd_libs.rapids_and_rivers.toUUID
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageMetadata
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.micrometer.core.instrument.MeterRegistry
import net.logstash.logback.argument.StructuredArguments
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
            precondition { it.requireValue("@event_name", "inntektsmelding_håndtert") }
            validate {
                it.requireKey(
                    "vedtaksperiodeId",
                    "inntektsmeldingId",
                    "@opprettet"
                )
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext, metadata: MessageMetadata, meterRegistry: MeterRegistry) {
        val vedtaksperiodeId = packet["vedtaksperiodeId"].asText().toUUID()
        val hendelseId = packet["inntektsmeldingId"].asText().toUUID()
        val opprettet = packet["@opprettet"].asLocalDateTime()

        val inntektsmeldingHåndtert = InntektsmeldingHåndtertDto(
            id = UUID.randomUUID(),
            vedtaksperiodeId = vedtaksperiodeId,
            hendelseId = hendelseId,
            opprettet = opprettet
        )
        logg.info(
            "Mottok melding om at en vedtaksperiode har håndtert en inntektsmelding: {}, {}, {}",
            StructuredArguments.kv("vedtaksperiodeId", vedtaksperiodeId),
            StructuredArguments.kv("hendelseId", hendelseId),
            StructuredArguments.kv("opprettet", opprettet)
        )
        inntektsmeldingHåndtertDao.lagre(inntektsmeldingHåndtert)

        logg.info("Lagret kobling mellom vedtaksperiode og inntektsmelding: {}, {}",
            StructuredArguments.kv("vedtaksperiodeId", vedtaksperiodeId),
            StructuredArguments.kv("hendelseId", hendelseId),
        )
    }
}