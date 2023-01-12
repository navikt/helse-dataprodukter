package no.nav.helse

import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.helse.Utbetalingsstatus.Companion.gyldigeStatuser
import no.nav.helse.Utbetalingsstatus.Companion.values
import no.nav.helse.Utbetalingstype.Companion.gyldigeTyper
import no.nav.helse.Utbetalingstype.Companion.values
import no.nav.helse.rapids_rivers.*
import org.slf4j.LoggerFactory
import java.util.*

internal class UtbetalingEndretRiver(
    rapidsConnection: RapidsConnection,
    private val mediator: IMediator
): River.PacketListener {

    private companion object {
        private val logg = LoggerFactory.getLogger(UtbetalingEndretRiver::class.java)
    }

    init {
        River(rapidsConnection).apply {
            validate {
                it.demandValue("@event_name", "utbetaling_endret")
                it.requireKey("korrelasjonsId", "utbetalingId", "arbeidsgiverOppdrag.fagsystemId", "personOppdrag.fagsystemId")
                it.requireAny("gjeldendeStatus", gyldigeStatuser.values())
                it.requireAny("type", gyldigeTyper.values())
                it.requireKey("@opprettet")
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
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