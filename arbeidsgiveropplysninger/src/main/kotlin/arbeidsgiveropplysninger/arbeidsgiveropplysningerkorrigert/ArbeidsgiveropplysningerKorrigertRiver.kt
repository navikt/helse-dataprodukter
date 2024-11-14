package arbeidsgiveropplysninger.arbeidsgiveropplysningerkorrigert

import arbeidsgiveropplysninger.arbeidsgiveropplysningerkorrigert.ArbeidsgiveropplysningerKorrigertDto.KorrigerendeInntektektsopplysningstype
import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers.asLocalDateTime
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageMetadata
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.micrometer.core.instrument.MeterRegistry
import net.logstash.logback.argument.StructuredArguments.keyValue
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

class ArbeidsgiveropplysningerKorrigertRiver(
    rapidsConnection: RapidsConnection,
    private val arbeidsgiveropplysningerKorrigertDao: ArbeidsgiveropplysningerKorrigertDao
) : River.PacketListener {

    private companion object {
        private val logg: Logger = LoggerFactory.getLogger(ArbeidsgiveropplysningerKorrigertRiver::class.java)
    }

    init {
        River(rapidsConnection).apply {
            precondition { it.requireValue("@event_name", "arbeidsgiveropplysninger_korrigert") }
            validate {
                it.requireKey(
                    "@id",
                    "@opprettet",
                    "korrigertInntektsmeldingId",
                    "korrigerendeInntektsopplysningId",
                    "korrigerendeInntektektsopplysningstype"
                )
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext, metadata: MessageMetadata, meterRegistry: MeterRegistry) {
        val korrigertInntektsmeldingId = UUID.fromString(packet["korrigertInntektsmeldingId"].asText())
        val korrigerendeInntektsopplysningId = UUID.fromString(packet["korrigerendeInntektsopplysningId"].asText())
        val korrigerendeInntektektsopplysningstype =
            KorrigerendeInntektektsopplysningstype.valueOf(packet["korrigerendeInntektektsopplysningstype"].asText())
        val opprettet = packet["@opprettet"].asLocalDateTime()

        val arbeidsgiveropplysningerKorrigert = ArbeidsgiveropplysningerKorrigertDto(
            id = UUID.randomUUID(),
            korrigertInntektsmeldingId = korrigertInntektsmeldingId,
            korrigerendeInntektsopplysningId = korrigerendeInntektsopplysningId,
            korrigerendeInntektektsopplysningstype = korrigerendeInntektektsopplysningstype,
            opprettet = opprettet
        )

        arbeidsgiveropplysningerKorrigertDao.lagre(arbeidsgiveropplysningerKorrigert)
        logg.info("Lagret korrigering av arbeidsgiveropplysninger: {}, {}, {}, {}",
            keyValue("korrigertInntektsmeldingId", korrigertInntektsmeldingId),
            keyValue("korrigerendeInntektsopplysningId", korrigerendeInntektsopplysningId),
            keyValue("korrigerendeInntektektsopplysningstype", korrigerendeInntektektsopplysningstype),
            keyValue("opprettet", opprettet),
        )
    }
}
