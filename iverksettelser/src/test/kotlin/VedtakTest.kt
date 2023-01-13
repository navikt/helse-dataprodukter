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
            meldingId = UUID.randomUUID(),
            utbetalingId = UUID.randomUUID(),
            korrelasjonsId = UUID.randomUUID(),
            fattetTidspunkt = LocalDateTime.now(),
            hendelser = emptySet()
        )
        assertEquals(vedtak, vedtak)
    }

    @Test
    fun `structural equals`() {
        val hendelseId = UUID.randomUUID()
        val vedtaksperiodeId = UUID.randomUUID()
        val utbetalingId = UUID.randomUUID()
        val korrelasjonsId = UUID.randomUUID()
        val enHendelseId = UUID.randomUUID()
        val fattetTidspunkt = LocalDateTime.now()
        val vedtak1 = Vedtak(vedtaksperiodeId, hendelseId, utbetalingId, korrelasjonsId, fattetTidspunkt, setOf(enHendelseId))
        val vedtak2 = Vedtak(vedtaksperiodeId, hendelseId, utbetalingId, korrelasjonsId, fattetTidspunkt, setOf(enHendelseId))
        assertEquals(vedtak1, vedtak2)
    }

    @Test
    fun `not equals - hendelseId`() {
        val hendelseId = UUID.randomUUID()
        val vedtaksperiodeId = UUID.randomUUID()
        val utbetalingId = UUID.randomUUID()
        val korrelasjonsId = UUID.randomUUID()
        val fattetTidspunkt = LocalDateTime.now()
        val vedtak1 = Vedtak(vedtaksperiodeId, hendelseId, utbetalingId, korrelasjonsId, fattetTidspunkt, emptySet())
        val vedtak2 = Vedtak(vedtaksperiodeId, UUID.randomUUID(), utbetalingId, korrelasjonsId, fattetTidspunkt, emptySet())
        assertNotEquals(vedtak1, vedtak2)
    }

    @Test
    fun `not equals - vedtaksperiodeId`() {
        val hendelseId = UUID.randomUUID()
        val vedtaksperiodeId = UUID.randomUUID()
        val utbetalingId = UUID.randomUUID()
        val korrelasjonsId = UUID.randomUUID()
        val fattetTidspunkt = LocalDateTime.now()
        val vedtak1 = Vedtak(vedtaksperiodeId, hendelseId, utbetalingId, korrelasjonsId, fattetTidspunkt, emptySet())
        val vedtak2 = Vedtak(UUID.randomUUID(), hendelseId, utbetalingId, korrelasjonsId, fattetTidspunkt, emptySet())
        assertNotEquals(vedtak1, vedtak2)
    }

    @Test
    fun `not equals - utbetalingId`() {
        val hendelseId = UUID.randomUUID()
        val vedtaksperiodeId = UUID.randomUUID()
        val utbetalingId = UUID.randomUUID()
        val korrelasjonsId = UUID.randomUUID()
        val fattetTidspunkt = LocalDateTime.now()
        val vedtak1 = Vedtak(vedtaksperiodeId, hendelseId, utbetalingId, korrelasjonsId, fattetTidspunkt, emptySet())
        val vedtak2 = Vedtak(vedtaksperiodeId, hendelseId, UUID.randomUUID(), korrelasjonsId, fattetTidspunkt, emptySet())
        assertNotEquals(vedtak1, vedtak2)
    }

    @Test
    fun `not equals - utbetalingId null`() {
        val hendelseId = UUID.randomUUID()
        val vedtaksperiodeId = UUID.randomUUID()
        val utbetalingId = UUID.randomUUID()
        val korrelasjonsId = UUID.randomUUID()
        val fattetTidspunkt = LocalDateTime.now()
        val vedtak1 = Vedtak(vedtaksperiodeId, hendelseId, utbetalingId, korrelasjonsId, fattetTidspunkt, emptySet())
        val vedtak2 = Vedtak(vedtaksperiodeId, hendelseId, null, korrelasjonsId, fattetTidspunkt, emptySet())
        assertNotEquals(vedtak1, vedtak2)
    }

    @Test
    fun `not equals - korrelasjonsId`() {
        val hendelseId = UUID.randomUUID()
        val vedtaksperiodeId = UUID.randomUUID()
        val utbetalingId = UUID.randomUUID()
        val korrelasjonsId = UUID.randomUUID()
        val fattetTidspunkt = LocalDateTime.now()
        val vedtak1 = Vedtak(vedtaksperiodeId, hendelseId, utbetalingId, korrelasjonsId, fattetTidspunkt, emptySet())
        val vedtak2 = Vedtak(vedtaksperiodeId, hendelseId, utbetalingId, UUID.randomUUID(), fattetTidspunkt, emptySet())
        assertNotEquals(vedtak1, vedtak2)
    }

    @Test
    fun `not equals - korrelasjonsId null`() {
        val hendelseId = UUID.randomUUID()
        val vedtaksperiodeId = UUID.randomUUID()
        val utbetalingId = UUID.randomUUID()
        val korrelasjonsId = UUID.randomUUID()
        val fattetTidspunkt = LocalDateTime.now()
        val vedtak1 = Vedtak(vedtaksperiodeId, hendelseId, utbetalingId, korrelasjonsId, fattetTidspunkt, emptySet())
        val vedtak2 = Vedtak(vedtaksperiodeId, hendelseId, utbetalingId, null, fattetTidspunkt, emptySet())
        assertNotEquals(vedtak1, vedtak2)
    }

    @Test
    fun `not equals - fattetTidspunkt`() {
        val hendelseId = UUID.randomUUID()
        val vedtaksperiodeId = UUID.randomUUID()
        val utbetalingId = UUID.randomUUID()
        val korrelasjonsId = UUID.randomUUID()
        val fattetTidspunkt = LocalDateTime.now().minusDays(1)
        val vedtak1 = Vedtak(vedtaksperiodeId, hendelseId, utbetalingId, korrelasjonsId, fattetTidspunkt, emptySet())
        val vedtak2 = Vedtak(vedtaksperiodeId, hendelseId, utbetalingId, korrelasjonsId, LocalDateTime.now(), emptySet())
        assertNotEquals(vedtak1, vedtak2)
    }

    @Test
    fun `not equals - hendelser`() {
        val hendelseId = UUID.randomUUID()
        val vedtaksperiodeId = UUID.randomUUID()
        val utbetalingId = UUID.randomUUID()
        val korrelasjonsId = UUID.randomUUID()
        val fattetTidspunkt = LocalDateTime.now()
        val vedtak1 = Vedtak(vedtaksperiodeId, hendelseId, utbetalingId, korrelasjonsId, fattetTidspunkt, emptySet())
        val vedtak2 = Vedtak(vedtaksperiodeId, hendelseId, utbetalingId, korrelasjonsId, fattetTidspunkt, setOf(UUID.randomUUID()))
        assertNotEquals(vedtak1, vedtak2)
    }
}