import TestDatasource.getDataSource
import TestDatasource.resetDatabase
import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.helse.Vedtak
import no.nav.helse.VedtakFattetDao
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.LocalDateTime
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class VedtakFattetDaoTest {

    private val dataSource = getDataSource()
    private val dao = VedtakFattetDao(dataSource)

    @BeforeEach
    fun reset() = resetDatabase()

    @Test
    fun `kan lagre vedtak`() {
        val hendelseId = UUID.randomUUID()
        val vedtaksperiodeId = UUID.randomUUID()
        val utbetalingId = UUID.randomUUID()
        val fattetTidspunkt = LocalDateTime.now()
        dao.lagre(hendelseId, vedtaksperiodeId, utbetalingId, fattetTidspunkt)
        assertVedtak(hendelseId, vedtaksperiodeId, utbetalingId, fattetTidspunkt)
    }
    @Test
    fun `kan lese vedtak`() {
        val vedtaksperiodeId = UUID.randomUUID()
        opprettVedtak(vedtaksperiodeId)
        val vedtak = dao.finnVedtakFor(vedtaksperiodeId)
        assertNotNull(vedtak)
    }

    @Test
    fun `kan lagre og lese vedtak`() {
        val hendelseId = UUID.randomUUID()
        val vedtaksperiodeId = UUID.randomUUID()
        val utbetalingId = UUID.randomUUID()
        val fattetTidspunkt = LocalDateTime.now()
        dao.lagre(hendelseId, vedtaksperiodeId, utbetalingId, fattetTidspunkt)

        val funnetVedtak = dao.finnVedtakFor(vedtaksperiodeId)
        assertEquals(Vedtak(vedtaksperiodeId, hendelseId, utbetalingId, fattetTidspunkt), funnetVedtak)
    }

    @Test
    fun `Markerer ikke vedtak som har annen utbetalingId som annullert`() {
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
    fun `Kan markere vedtak som annullert`() {
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

    private fun assertVedtak(
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

    private fun opprettVedtak(vedtaksperiodeId: UUID) {
        @Language("PostgreSQL")
        val query = "INSERT INTO vedtak_fattet(vedtaksperiode_id, hendelse_id, utbetaling_id, fattet_tidspunkt) VALUES (?, ?, ?, ?)"
        sessionOf(dataSource).use { session ->
            session.run(queryOf(query, vedtaksperiodeId, UUID.randomUUID(), UUID.randomUUID(), LocalDateTime.now()).asExecute)
        }
    }
}