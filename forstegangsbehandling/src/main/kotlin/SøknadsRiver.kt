package no.nav.helse

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers.asLocalDate
import com.github.navikt.tbd_libs.rapids_and_rivers.asLocalDateTime
import com.github.navikt.tbd_libs.rapids_and_rivers.asOptionalLocalDate
import com.github.navikt.tbd_libs.rapids_and_rivers.toUUID
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageMetadata
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageProblems
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import net.logstash.logback.argument.StructuredArguments.kv
import java.time.Duration
import java.util.*

internal class SøknadsRiver(
    rapidsConnection: RapidsConnection,
    private val mediator: SøknadMediator
) : River.PacketListener {

    init {
        River(rapidsConnection).apply {
            precondition { it.requireValue("@event_name", "sendt_søknad_nav") }
            validate {
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

    override fun onError(problems: MessageProblems, context: MessageContext, metadata: MessageMetadata) {
        // Burde ikke være problem med søknadsmeldingen
        throw Exception("Problem med meding: $problems")
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext, metadata: MessageMetadata, meterRegistry: MeterRegistry) {
        val start = System.nanoTime()
        Counter.builder("soknader_lest").description("Antall førstegangssøknader lest").register(meterRegistry).increment()
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
