package no.nav.helse

import no.nav.helse.rapids_rivers.RapidsConnection
import java.time.LocalDateTime
import java.util.*

internal interface IMediator {
    fun håndter(vedtaksperiodeId: UUID, vedtak: Vedtak)
    fun håndter(korrelasjonsId: UUID, utbetaling: Utbetaling, versjon: Utbetaling.Versjon)
    fun håndterAnnullering(korrelasjonsId: UUID)
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

    override fun håndterAnnullering(korrelasjonsId: UUID) {
        vedtakFattetDao.markerAnnullertFor(korrelasjonsId)
        utbetalingEndretDao.markerAnnullertFor(korrelasjonsId)
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
