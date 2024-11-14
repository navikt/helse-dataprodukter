package no.nav.helse

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers.asLocalDateTime
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageMetadata
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.micrometer.core.instrument.MeterRegistry
import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.helse.Utbetalingstype.Companion.gyldigeTyper
import no.nav.helse.Utbetalingstype.Companion.values
import org.slf4j.LoggerFactory
import java.util.*

internal class UtbetalingEndretRiver(
    rapidsConnection: RapidsConnection,
    private val mediator: IMediator
): River.PacketListener {

    private companion object {
        private val logg = LoggerFactory.getLogger(UtbetalingEndretRiver::class.java)
        private val terminaltilstander = listOf("ANNULLERT", "UTBETALT", "GODKJENT_UTEN_UTBETALING")
    }

    init {
        River(rapidsConnection).apply {
            precondition { it.requireValue("@event_name", "utbetaling_endret") }
            validate {
                it.requireKey("korrelasjonsId", "utbetalingId", "arbeidsgiverOppdrag.fagsystemId", "personOppdrag.fagsystemId")
                it.requireAny("gjeldendeStatus", terminaltilstander)
                it.requireAny("type", gyldigeTyper.values())
                it.requireKey("@opprettet")
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext, metadata: MessageMetadata, meterRegistry: MeterRegistry) {
        val utbetalingId = UUID.fromString(packet["utbetalingId"].asText())
        val korrelasjonsId = UUID.fromString(packet["korrelasjonsId"].asText())
        val utbetalingstype = enumValueOf<Utbetalingstype>(packet["type"].asText())
        val arbeidsgiverFagsystemId = packet["arbeidsgiverOppdrag.fagsystemId"].asText()
        val personFagsystemId = packet["personOppdrag.fagsystemId"].asText()
        val opprettet = packet["@opprettet"].asLocalDateTime()
        val utbetaling = Utbetaling(
            korrelasjonsId = korrelasjonsId,
            arbeidsgiverFagsystemId = arbeidsgiverFagsystemId,
            personFagsystemId = personFagsystemId,
            opprettet = opprettet
        )
        val versjon = Utbetaling.Versjon(utbetalingId, utbetalingstype, opprettet)
        logg.info(
            "Mottok utbetaling med {}, {}, {}, {}",
            kv("utbetalingId", utbetalingId),
            kv("korrelasjonsId", korrelasjonsId),
            kv("utbetalingstype", utbetalingstype.name),
            kv("opprettet", opprettet)
        )
        mediator.håndter(korrelasjonsId, utbetaling, versjon)
    }
}