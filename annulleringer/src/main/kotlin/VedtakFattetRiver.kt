package no.nav.helse

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers.toUUID
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageMetadata
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import net.logstash.logback.argument.StructuredArguments.kv
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.LocalDateTime

internal class VedtakFattetRiver(
    rapidsConnection: RapidsConnection,
    private val mediator: IMediator
) : River.PacketListener {

    private companion object {
        private val logg: Logger = LoggerFactory.getLogger(VedtakFattetRiver::class.java)
    }

    init {
        River(rapidsConnection).apply {
            precondition {
                it.requireValue("@event_name", "vedtak_fattet")
                it.requireKey("utbetalingId")
            }
            validate {
                it.requireKey("@id", "vedtaksperiodeId", "vedtakFattetTidspunkt")
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext, metadata: MessageMetadata, meterRegistry: MeterRegistry) {
        val start = System.nanoTime()

        Counter.builder("vedtak_fattet_lest")
            .description("Antall vedtak fattet lest")
            .register(meterRegistry)
            .increment()

        val vedtaksperiodeId = packet["vedtaksperiodeId"].asText().toUUID()
        val fattetTidspunkt = LocalDateTime.parse(packet["vedtakFattetTidspunkt"].asText())

        val vedtak = Vedtak(
            vedtaksperiodeId = vedtaksperiodeId,
            hendelseId = packet["@id"].asText().toUUID(),
            utbetalingId = packet["utbetalingId"].asText().toUUID(),
            fattetTidspunkt = fattetTidspunkt,
        )
        logg.info(
            "Mottok vedtak med {}, {}",
            kv("vedtaksperiodeId", vedtaksperiodeId),
            kv("fattetTidspunkt", fattetTidspunkt)
        )
        mediator.håndter(vedtaksperiodeId, vedtak)
        logg.info("Behandlet vedtak fattet med {} på ${forbruktTid(start)} ms", kv("vedtaksperiodeId", vedtaksperiodeId))
    }

    private fun forbruktTid(start: Long) = Duration.ofNanos(System.nanoTime() - start).toMillis()
}
