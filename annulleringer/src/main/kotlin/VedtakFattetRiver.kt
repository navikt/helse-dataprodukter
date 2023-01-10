package no.nav.helse

import io.prometheus.client.Counter
import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.helse.rapids_rivers.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.LocalDateTime

private val messageCounter: Counter = Counter.build("vedtak_fattet_lest", "Antall vedtak fattet lest").register()

internal class VedtakFattetRiver(
    rapidsConnection: RapidsConnection,
    private val mediator: IVedtakFattetMediator
) : River.PacketListener {

    private companion object {
        private val logg: Logger = LoggerFactory.getLogger(VedtakFattetRiver::class.java)
    }

    init {
        River(rapidsConnection).apply {
            validate {
                it.demandValue("@event_name", "vedtak_fattet")
                it.demandKey("utbetalingId")
                it.requireKey(
                    "@id",
                    "vedtaksperiodeId",
                    "vedtakFattetTidspunkt",
                )
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val start = System.nanoTime()
        messageCounter.inc()
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
