package no.nav.helse

import io.prometheus.client.Counter
import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.helse.rapids_rivers.*
import java.time.Duration
import java.util.*

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
        val start = System.nanoTime()
        messageCounter.inc()
        val søknadId = packet["id"].asText().toUUID()
        if (søknadId in ugyldigeSøknader) return
        val søknad = Søknad(
            id = packet["@id"].asText().toUUID(),
            søknadId = søknadId,
            sykmeldingId = packet["sykmeldingId"].asText().toUUID(),
            fnr = packet["fnr"].asText(),
            orgnummer = packet["arbeidsgiver.orgnummer"].asText(),
            fom = packet["fom"].asLocalDate(),
            tom = packet["tom"].asLocalDate(),
            arbeidGjenopptatt = packet["arbeidGjenopptatt"].asOptionalLocalDate(),
            opprettet = packet["@opprettet"].asLocalDateTime(),
        )
        logger.info(
            "Mottok søknad med {}, {}",
            kv("id", søknad.søknadId),
            kv("opprettet", søknad.opprettet)
        )
        mediator.håndter(søknad)
        logger.info("Behandlet søknad med {} på ${forbruktTid(start)} ms", kv("id", søknad.søknadId))
    }

    private val ugyldigeSøknader = listOf(
        UUID.fromString("b1ae4996-45ec-4427-9ab8-b7b3bbbf2e44")
    )

    private fun forbruktTid(start: Long) = Duration.ofNanos(System.nanoTime() - start).toMillis()
}
