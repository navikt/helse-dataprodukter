package no.nav.helse

import net.logstash.logback.argument.StructuredArguments.kv
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID

internal class Vedtak(
    private val vedtaksperiodeId: UUID,
    private val hendelseId: UUID,
    private val utbetalingId: UUID,
    private val fattetTidspunkt: LocalDateTime,
) {
    private companion object {
        private val logg = LoggerFactory.getLogger(Vedtak::class.java)
    }
    internal fun håndterNytt(vedtak: Vedtak) {
        logg.info(
            "Ignorerer vedtak med {} da det allerede finnes et vedtak for {}",
            kv("hendelseId", vedtak.hendelseId),
            kv("vedtaksperiodeId", vedtaksperiodeId)
        )
        return
    }

    internal fun lagre(vedtakFattetDao: VedtakFattetDao) {
        vedtakFattetDao.lagre(hendelseId, vedtaksperiodeId, utbetalingId, fattetTidspunkt)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Vedtak

        if (hendelseId != other.hendelseId) return false
        if (vedtaksperiodeId != other.vedtaksperiodeId) return false
        if (utbetalingId != other.utbetalingId) return false
        if (fattetTidspunkt.truncatedTo(ChronoUnit.MILLIS) != other.fattetTidspunkt.truncatedTo(ChronoUnit.MILLIS)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = hendelseId.hashCode()
        result = 31 * result + vedtaksperiodeId.hashCode()
        result = 31 * result + utbetalingId.hashCode()
        result = 31 * result + fattetTidspunkt.hashCode()
        return result
    }
}