package no.nav.helse

import no.nav.helse.rapids_rivers.RapidsConnection
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

internal interface IVedtakFattetMediator {
    fun håndter(vedtaksperiodeId: UUID, vedtak: Vedtak)
}

internal class VedtakFattetMediator(rapidsConnection: RapidsConnection, private val dao: VedtakFattetDao):
    IVedtakFattetMediator {
    private companion object {
        val logg: Logger = LoggerFactory.getLogger(VedtakFattetMediator::class.java)
    }

    init {
        VedtakFattetRiver(rapidsConnection, this)
    }

    override fun håndter(vedtaksperiodeId: UUID, vedtak: Vedtak) {
//        dao.finnVedtakFor(vedtaksperiodeId)
//            ?.håndterNyttVedtak(vedtak)
//            ?: vedtak.lagre(dao)
    }
}
