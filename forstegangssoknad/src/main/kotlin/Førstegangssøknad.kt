package no.nav.helse

import no.nav.helse.rapids_rivers.*


internal class Førstegangssøknad(
    rapidsConnection: RapidsConnection
) : River.PacketListener {

    init {
        River(rapidsConnection).apply {
            validate { it.demandValue("@event_name", "my_event") }
            validate { it.requireKey("a_required_key") }
            // nested objects can be chained using "."
            validate { it.requireValue("nested.key", "works_as_well") }
        }.register(this)
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        /* fordi vi bruker demandValue() på event_name kan vi trygt anta at meldingen
           er "my_event", og at det er minst én av de ulike require*() som har feilet */
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
    }
}