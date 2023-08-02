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
        "f33d6f16-1123-4aae-8b0f-0e8bb96d396c",
        "00bd7590-7411-4c35-8c11-2ac29108ff70",
        "de2fed38-fa77-48fd-9af5-3512edd69e97",
        "3093c86f-6101-4482-9faa-55aeaea82f42",
        "e2745000-0892-4549-80a9-ee8d7280b271",
        "6b247965-cbc3-4e22-8647-1ef4955fe8d7",
        "64fd36a7-80de-343d-b895-d54f1dde8df8",
        "2e405fce-662e-3677-9aad-935b6ea9846c"
    ).map { UUID.fromString(it) }

    private fun forbruktTid(start: Long) = Duration.ofNanos(System.nanoTime() - start).toMillis()
}
