package no.nav.helse

import no.nav.helse.rapids_rivers.RapidsConnection
import java.util.*

internal interface IVedtakFattetMediator {
    fun håndter(vedtaksperiodeId: UUID, vedtak: Vedtak)
}

internal class VedtakFattetMediator(rapidsConnection: RapidsConnection, private val dao: VedtakFattetDao): IVedtakFattetMediator {

    init {
        VedtakFattetRiver(rapidsConnection, this)
    }

    override fun håndter(vedtaksperiodeId: UUID, vedtak: Vedtak) {
        dao.finnVedtakFor(vedtaksperiodeId)
            ?.håndterNytt(vedtak, dao)
            ?: vedtak.lagre(dao)
    }
}
