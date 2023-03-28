package arbeidsgiveropplysninger.inntektsmeldingregistrert

import arbeidsgiveropplysninger.inntektsmeldinghåndtert.InntektsmeldingHåndtertRiver
import net.logstash.logback.argument.StructuredArguments
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.rapids_rivers.asLocalDateTime
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.UUID

class InntektsmeldingRegistrertRiver(rapidsConnection: RapidsConnection, private val inntektsmeldingRegistrertDao: InntektsmeldingRegistrertDao) : River.PacketListener {

    private companion object {
        private val logg: Logger = LoggerFactory.getLogger(InntektsmeldingHåndtertRiver::class.java)
    }

    init {
        River(rapidsConnection).apply {
            validate { it.demandValue("@event_name", "inntektsmelding") }
            validate { it.requireKey("@id", "inntektsmeldingId", "@opprettet") }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {

        val hendelseId = UUID.fromString(packet["@id"].asText())
        val dokumentId = UUID.fromString(packet["inntektsmeldingId"].asText())
        val opprettet = packet["@opprettet"].asLocalDateTime()

        val inntektsmeldingRegistrertDto = InntektsmeldingRegistrertDto(
            id = UUID.randomUUID(),
            hendelseId = hendelseId,
            dokumentId = dokumentId,
            opprettet = opprettet
        )

        inntektsmeldingRegistrertDao.lagre(inntektsmeldingRegistrertDto)
        logg.info("Lagret kobling mellom hendelseId og dokumentId til inntektsmelding: hendelseId={}, dokumentId={}, opprettet={}",
            StructuredArguments.keyValue("hendelseId", hendelseId),
            StructuredArguments.keyValue("dokumentId", dokumentId),
            StructuredArguments.keyValue("opprettet", opprettet)
        )
    }
}