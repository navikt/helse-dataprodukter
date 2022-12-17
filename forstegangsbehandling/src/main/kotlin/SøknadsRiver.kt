package no.nav.helse

import io.prometheus.client.Counter
import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.helse.rapids_rivers.*

val messageCounter: Counter = Counter.build("soknader_lest", "Antall førstegangssøknader lest").register()

internal class SøknadsRiver(
    rapidsConnection: RapidsConnection,
    private val mediator: SøknadMediator
) : River.PacketListener {

    init {
        River(rapidsConnection).apply {
            validate {
                it.demandValue("@event_name", "sendt_søknad_nav")
                it.requireKey(
                    "@id",
                    "id",
                    "sykmeldingId",
                    "fnr",
                    "arbeidsgiver.orgnummer",
                    "fom",
                    "tom",
                    "@opprettet",
                )
                it.interestedIn("arbeidGjenopptatt")
            }
        }.register(this)
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        // Burde ikke være problem med søknadsmeldingen
        throw Exception("Problem med meding: $problems")
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        messageCounter.inc()
        val søknad = Søknad(
            packet["@id"].asText().toUUID(),
            packet["id"].asText().toUUID(),
            packet["sykmeldingId"].asText().toUUID(),
            packet["fnr"].asText(),
            packet["arbeidsgiver.orgnummer"].asText(),
            packet["fom"].asLocalDate(),
            packet["tom"].asLocalDate(),
            packet["arbeidGjenopptatt"].asOptionalLocalDate(),
            packet["@opprettet"].asLocalDateTime(),
        )
        logger.info(
            "Mottok søknad med {}, {}",
            kv("sykmeldingId", søknad.sykmeldingId),
            kv("opprettet", søknad.opprettet)
        )
        mediator.håndter(søknad)
    }
}
