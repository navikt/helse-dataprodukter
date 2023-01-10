package no.nav.helse

import no.nav.helse.Utbetalingsstatus.Companion.gyldigeStatuser
import no.nav.helse.Utbetalingsstatus.Companion.values
import no.nav.helse.Utbetalingstype.Companion.gyldigeTyper
import no.nav.helse.Utbetalingstype.Companion.values
import no.nav.helse.rapids_rivers.*
import java.util.*

internal class UtbetalingEndretRiver(rapidsConnection: RapidsConnection, private val mediator: IMediator): River.PacketListener {

    init {
        River(rapidsConnection).apply {
            validate {
                it.demandValue("@event_name", "utbetaling_endret")
                it.requireKey("korrelasjonsId", "utbetalingId")
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
        val opprettet = packet["@opprettet"].asLocalDateTime()
        val utbetaling = Utbetaling(korrelasjonsId, utbetalingId, utbetalingstype, opprettet)
        mediator.håndter(korrelasjonsId, utbetaling)
    }
}