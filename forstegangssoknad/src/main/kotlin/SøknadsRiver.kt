package no.nav.helse

import io.prometheus.client.Counter
import no.nav.helse.rapids_rivers.*

val messageCounter = Counter.build("soknader_lest", "Antall førstegangssøknader lest").register()

internal class SøknadsRiver(
    rapidsConnection: RapidsConnection
) : River.PacketListener {

    init {
        River(rapidsConnection).apply {
            validate { it.demandValue("@event_name", "sendt_søknad_nav") }
        }.register(this)
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        /* fordi vi bruker demandValue() på event_name kan vi trygt anta at meldingen
           er "my_event", og at det er minst én av de ulike require*() som har feilet */
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        messageCounter.inc()
    }
}