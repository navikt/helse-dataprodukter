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
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            LocalDateTime.now()
        )
        assertEquals(vedtak, vedtak)
    }
    @Test
    fun `structural equals`() {
        val hendelseId = UUID.randomUUID()
        val vedtaksperiodeId = UUID.randomUUID()
        val utbetalingId = UUID.randomUUID()
        val korrelasjonsId = UUID.randomUUID()
        val fattetTidspunkt = LocalDateTime.now()
        val vedtak1 = Vedtak(vedtaksperiodeId, hendelseId, utbetalingId, korrelasjonsId, fattetTidspunkt)
        val vedtak2 = Vedtak(vedtaksperiodeId, hendelseId, utbetalingId, korrelasjonsId, fattetTidspunkt)
        assertEquals(vedtak1, vedtak2)
    }

    @Test
    fun `not equals - hendelseId`() {
        val hendelseId = UUID.randomUUID()
        val vedtaksperiodeId = UUID.randomUUID()
        val utbetalingId = UUID.randomUUID()
        val korrelasjonsId = UUID.randomUUID()
        val fattetTidspunkt = LocalDateTime.now()
        val vedtak1 = Vedtak(vedtaksperiodeId, hendelseId, utbetalingId, korrelasjonsId, fattetTidspunkt)
        val vedtak2 = Vedtak(vedtaksperiodeId, UUID.randomUUID(), utbetalingId, korrelasjonsId, fattetTidspunkt)
        assertNotEquals(vedtak1, vedtak2)
    }
    @Test
    fun `not equals - vedtaksperiodeId`() {
        val hendelseId = UUID.randomUUID()
        val vedtaksperiodeId = UUID.randomUUID()
        val utbetalingId = UUID.randomUUID()
        val korrelasjonsId = UUID.randomUUID()
        val fattetTidspunkt = LocalDateTime.now()
        val vedtak1 = Vedtak(vedtaksperiodeId, hendelseId, utbetalingId, korrelasjonsId, fattetTidspunkt)
        val vedtak2 = Vedtak(UUID.randomUUID(), hendelseId, utbetalingId, korrelasjonsId, fattetTidspunkt)
        assertNotEquals(vedtak1, vedtak2)
    }
    @Test
    fun `not equals - utbetalingId`() {
        val hendelseId = UUID.randomUUID()
        val vedtaksperiodeId = UUID.randomUUID()
        val utbetalingId = UUID.randomUUID()
        val korrelasjonsId = UUID.randomUUID()
        val fattetTidspunkt = LocalDateTime.now()
        val vedtak1 = Vedtak(vedtaksperiodeId, hendelseId, utbetalingId, korrelasjonsId, fattetTidspunkt)
        val vedtak2 = Vedtak(vedtaksperiodeId, hendelseId, UUID.randomUUID(), korrelasjonsId, fattetTidspunkt)
        assertNotEquals(vedtak1, vedtak2)
    }
    @Test
    fun `not equals - utbetalingId null`() {
        val hendelseId = UUID.randomUUID()
        val vedtaksperiodeId = UUID.randomUUID()
        val utbetalingId = UUID.randomUUID()
        val korrelasjonsId = UUID.randomUUID()
        val fattetTidspunkt = LocalDateTime.now()
        val vedtak1 = Vedtak(vedtaksperiodeId, hendelseId, utbetalingId, korrelasjonsId, fattetTidspunkt)
        val vedtak2 = Vedtak(vedtaksperiodeId, hendelseId, null, korrelasjonsId, fattetTidspunkt)
        assertNotEquals(vedtak1, vedtak2)
    }
    @Test
    fun `not equals - korrelasjonsId`() {
        val hendelseId = UUID.randomUUID()
        val vedtaksperiodeId = UUID.randomUUID()
        val utbetalingId = UUID.randomUUID()
        val korrelasjonsId = UUID.randomUUID()
        val fattetTidspunkt = LocalDateTime.now()
        val vedtak1 = Vedtak(vedtaksperiodeId, hendelseId, utbetalingId, korrelasjonsId, fattetTidspunkt)
        val vedtak2 = Vedtak(vedtaksperiodeId, hendelseId, utbetalingId, UUID.randomUUID(), fattetTidspunkt)
        assertNotEquals(vedtak1, vedtak2)
    }
    @Test
    fun `not equals - korrelasjonsId null`() {
        val hendelseId = UUID.randomUUID()
        val vedtaksperiodeId = UUID.randomUUID()
        val utbetalingId = UUID.randomUUID()
        val korrelasjonsId = UUID.randomUUID()
        val fattetTidspunkt = LocalDateTime.now()
        val vedtak1 = Vedtak(vedtaksperiodeId, hendelseId, utbetalingId, korrelasjonsId, fattetTidspunkt)
        val vedtak2 = Vedtak(vedtaksperiodeId, hendelseId, utbetalingId, null, fattetTidspunkt)
        assertNotEquals(vedtak1, vedtak2)
    }
    @Test
    fun `not equals - fattetTidspunkt`() {
        val hendelseId = UUID.randomUUID()
        val vedtaksperiodeId = UUID.randomUUID()
        val utbetalingId = UUID.randomUUID()
        val korrelasjonsId = UUID.randomUUID()
        val fattetTidspunkt = LocalDateTime.now()
        val vedtak1 = Vedtak(vedtaksperiodeId, hendelseId, utbetalingId, korrelasjonsId, fattetTidspunkt)
        val vedtak2 = Vedtak(vedtaksperiodeId, hendelseId, utbetalingId, null, LocalDateTime.now())
        assertNotEquals(vedtak1, vedtak2)
    }
}