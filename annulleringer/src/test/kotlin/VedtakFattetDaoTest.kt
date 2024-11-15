import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.helse.Vedtak
import no.nav.helse.VedtakFattetDao
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*
import javax.sql.DataSource

internal class VedtakFattetDaoTest {

    @Test
    fun `kan lagre vedtak`() = e2e {
        val hendelseId = UUID.randomUUID()
        val vedtaksperiodeId = UUID.randomUUID()
        val utbetalingId = UUID.randomUUID()
        val fattetTidspunkt = LocalDateTime.now()
        dao.lagre(hendelseId, vedtaksperiodeId, utbetalingId, fattetTidspunkt)
        assertVedtak(hendelseId, vedtaksperiodeId, utbetalingId, fattetTidspunkt)
    }
    @Test
    fun `kan lese vedtak`() = e2e {
        val vedtaksperiodeId = UUID.randomUUID()
        opprettVedtak(vedtaksperiodeId)
        val vedtak = dao.finnVedtakFor(vedtaksperiodeId)
        assertNotNull(vedtak)
    }

    @Test
    fun `kan lagre og lese vedtak`() = e2e {
        val hendelseId = UUID.randomUUID()
        val vedtaksperiodeId = UUID.randomUUID()
        val utbetalingId = UUID.randomUUID()
        val fattetTidspunkt = LocalDateTime.now()
        dao.lagre(hendelseId, vedtaksperiodeId, utbetalingId, fattetTidspunkt)

        val funnetVedtak = dao.finnVedtakFor(vedtaksperiodeId)
        assertEquals(Vedtak(vedtaksperiodeId, hendelseId, utbetalingId, fattetTidspunkt), funnetVedtak)
    }

    @Test
    fun `Markerer ikke vedtak som har annen utbetalingId som annullert`() = e2e {
        val hendelseId = UUID.randomUUID()
        val vedtaksperiodeId = UUID.randomUUID()
        val fattetTidspunkt = LocalDateTime.now()
        val utbetalingId = UUID.randomUUID()
        dao.lagre(hendelseId, vedtaksperiodeId, utbetalingId, fattetTidspunkt)
        assertVedtak(hendelseId, vedtaksperiodeId, utbetalingId, fattetTidspunkt, false)

        dao.markerAnnullertFor(UUID.randomUUID())

        assertVedtak(hendelseId, vedtaksperiodeId, utbetalingId, fattetTidspunkt, false)
    }

    @Test
    fun `Kan markere vedtak som annullert`() = e2e {
        val hendelseId1 = UUID.randomUUID()
        val hendelseId2 = UUID.randomUUID()
        val vedtaksperiodeId1 = UUID.randomUUID()
        val vedtaksperiodeId2 = UUID.randomUUID()
        val fattetTidspunkt1 = LocalDateTime.now()
        val fattetTidspunkt2 = LocalDateTime.now()
        val utbetalingId = UUID.randomUUID()
        dao.lagre(hendelseId1, vedtaksperiodeId1, utbetalingId, fattetTidspunkt1)
        dao.lagre(hendelseId2, vedtaksperiodeId2, utbetalingId, fattetTidspunkt2)
        assertVedtak(hendelseId1, vedtaksperiodeId1, utbetalingId, fattetTidspunkt1, false)
        assertVedtak(hendelseId2, vedtaksperiodeId2, utbetalingId, fattetTidspunkt2, false)

        dao.markerAnnullertFor(utbetalingId)

        assertVedtak(hendelseId1, vedtaksperiodeId1, utbetalingId, fattetTidspunkt1, true)
        assertVedtak(hendelseId2, vedtaksperiodeId2, utbetalingId, fattetTidspunkt2, true)
    }

    data class E2ETestContext(
        val dataSource: DataSource,
        val dao: VedtakFattetDao
    )
    private fun e2e(testblokk: E2ETestContext.() -> Unit) {
        databaseTest { ds ->
            val dao = VedtakFattetDao(ds)
            testblokk(E2ETestContext(ds, dao))
        }
    }

    private fun E2ETestContext.assertVedtak(
        hendelseId: UUID,
        vedtaksperiodeId: UUID,
        utbetalingId: UUID?,
        fattetTidspunkt: LocalDateTime,
        annullert: Boolean = false
    ) {
        @Language("PostgreSQL")
        val query = "SELECT COUNT(1) FROM vedtak_fattet WHERE vedtaksperiode_id = ? AND hendelse_id = ? AND utbetaling_id = ? AND fattet_tidspunkt = ? AND annullert = ?"
        val antall = sessionOf(dataSource).use { session ->
            session.run(queryOf(query, vedtaksperiodeId, hendelseId, utbetalingId, fattetTidspunkt, annullert).map { it.int(1) }.asSingle)
        }

        assertEquals(1, antall)
    }

    private fun E2ETestContext.opprettVedtak(vedtaksperiodeId: UUID) {
        @Language("PostgreSQL")
        val query = "INSERT INTO vedtak_fattet(vedtaksperiode_id, hendelse_id, utbetaling_id, fattet_tidspunkt) VALUES (?, ?, ?, ?)"
        sessionOf(dataSource).use { session ->
            session.run(queryOf(query, vedtaksperiodeId, UUID.randomUUID(), UUID.randomUUID(), LocalDateTime.now()).asExecute)
        }
    }
}