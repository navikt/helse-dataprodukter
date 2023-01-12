package no.nav.helse

import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.helse.rapids_rivers.RapidsConnection
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.util.*

internal interface IMediator {
    fun håndter(vedtaksperiodeId: UUID, vedtak: Vedtak)
    fun håndter(korrelasjonsId: UUID, utbetaling: Utbetaling, versjon: Utbetaling.Versjon)
    fun håndterAnnullering(korrelasjonsId: UUID, utbetalingIder: List<UUID>)
    fun nyUtbetaling(
        korrelasjonsId: UUID,
        arbeidsgiverFagsystemId: String,
        personFagsystemId: String,
        opprettet: LocalDateTime
    )

    fun nyVersjon(
        korrelasjonsId: UUID,
        utbetalingId: UUID,
        utbetalingstype: Utbetalingstype,
        opprettet: LocalDateTime
    )
}

internal class Mediator(
    rapidsConnection: RapidsConnection,
    private val vedtakFattetDao: VedtakFattetDao,
    private val utbetalingEndretDao: UtbetalingEndretDao
): IMediator {

    init {
        VedtakFattetRiver(rapidsConnection, this)
        UtbetalingEndretRiver(rapidsConnection, this)
    }

    private companion object {
        private val logg = LoggerFactory.getLogger(Mediator::class.java)
    }

    override fun håndter(vedtaksperiodeId: UUID, vedtak: Vedtak) {
        vedtakFattetDao.finnVedtakFor(vedtaksperiodeId)
            ?.håndterNytt(vedtak)
            ?: vedtak.lagre(vedtakFattetDao)
    }

    override fun håndter(korrelasjonsId: UUID, utbetaling: Utbetaling, versjon: Utbetaling.Versjon) {
        utbetalingEndretDao.finnUtbetalingFor(korrelasjonsId)
            ?.håndter(this, versjon)
            ?: utbetaling.lagre(this, versjon)
    }

    override fun håndterAnnullering(korrelasjonsId: UUID, utbetalingIder: List<UUID>) {
        logg.info(
            "Markerer utbetaling med {}, {} som annullert",
            kv("korrelasjonsId", korrelasjonsId),
            kv("utbetalingsider", utbetalingIder.joinToString())
        )
        utbetalingEndretDao.markerAnnullertFor(korrelasjonsId)
        utbetalingIder.forEach { utbetalingId ->
            vedtakFattetDao.markerAnnullertFor(utbetalingId)
        }
    }

    override fun nyUtbetaling(
        korrelasjonsId: UUID,
        arbeidsgiverFagsystemId: String,
        personFagsystemId: String,
        opprettet: LocalDateTime
    ) {
        utbetalingEndretDao.nyUtbetalingFor(
            korrelasjonsId,
            personFagsystemId,
            arbeidsgiverFagsystemId,
            opprettet
        )
    }

    override fun nyVersjon(korrelasjonsId: UUID, utbetalingId: UUID, utbetalingstype: Utbetalingstype, opprettet: LocalDateTime) {
        utbetalingEndretDao.nyVersjonFor(korrelasjonsId, utbetalingId, utbetalingstype, opprettet)
    }
}
