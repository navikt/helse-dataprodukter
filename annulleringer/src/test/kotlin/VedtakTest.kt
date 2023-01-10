import no.nav.helse.Vedtak
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

class VedtakTest {
    @Test
    fun `referential equals`() {
        val vedtak = Vedtak(
            vedtaksperiodeId = UUID.randomUUID(),
            hendelseId = UUID.randomUUID(),
            utbetalingId = UUID.randomUUID(),
            fattetTidspunkt = LocalDateTime.now(),
        )
        assertEquals(vedtak, vedtak)
    }

    @Test
    fun `structural equals`() {
        val hendelseId = UUID.randomUUID()
        val vedtaksperiodeId = UUID.randomUUID()
        val utbetalingId = UUID.randomUUID()
        val fattetTidspunkt = LocalDateTime.now()
        val vedtak1 = Vedtak(vedtaksperiodeId, hendelseId, utbetalingId, fattetTidspunkt)
        val vedtak2 = Vedtak(vedtaksperiodeId, hendelseId, utbetalingId, fattetTidspunkt)
        assertEquals(vedtak1, vedtak2)
    }

    @Test
    fun `not equals - hendelseId`() {
        val hendelseId = UUID.randomUUID()
        val vedtaksperiodeId = UUID.randomUUID()
        val utbetalingId = UUID.randomUUID()
        val fattetTidspunkt = LocalDateTime.now()
        val vedtak1 = Vedtak(vedtaksperiodeId, hendelseId, utbetalingId, fattetTidspunkt)
        val vedtak2 = Vedtak(vedtaksperiodeId, UUID.randomUUID(), utbetalingId, fattetTidspunkt)
        assertNotEquals(vedtak1, vedtak2)
    }

    @Test
    fun `not equals - vedtaksperiodeId`() {
        val hendelseId = UUID.randomUUID()
        val vedtaksperiodeId = UUID.randomUUID()
        val utbetalingId = UUID.randomUUID()
        val fattetTidspunkt = LocalDateTime.now()
        val vedtak1 = Vedtak(vedtaksperiodeId, hendelseId, utbetalingId, fattetTidspunkt)
        val vedtak2 = Vedtak(UUID.randomUUID(), hendelseId, utbetalingId, fattetTidspunkt)
        assertNotEquals(vedtak1, vedtak2)
    }

    @Test
    fun `not equals - utbetalingId`() {
        val hendelseId = UUID.randomUUID()
        val vedtaksperiodeId = UUID.randomUUID()
        val utbetalingId = UUID.randomUUID()
        val fattetTidspunkt = LocalDateTime.now()
        val vedtak1 = Vedtak(vedtaksperiodeId, hendelseId, utbetalingId, fattetTidspunkt)
        val vedtak2 = Vedtak(vedtaksperiodeId, hendelseId, UUID.randomUUID(), fattetTidspunkt)
        assertNotEquals(vedtak1, vedtak2)
    }

    @Test
    fun `not equals - fattetTidspunkt`() {
        val hendelseId = UUID.randomUUID()
        val vedtaksperiodeId = UUID.randomUUID()
        val utbetalingId = UUID.randomUUID()
        val fattetTidspunkt = LocalDateTime.now().minusDays(1)
        val vedtak1 = Vedtak(vedtaksperiodeId, hendelseId, utbetalingId, fattetTidspunkt)
        val vedtak2 = Vedtak(vedtaksperiodeId, hendelseId, utbetalingId, LocalDateTime.now())
        assertNotEquals(vedtak1, vedtak2)
    }
}