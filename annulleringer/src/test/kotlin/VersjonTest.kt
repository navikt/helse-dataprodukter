import no.nav.helse.Utbetaling
import no.nav.helse.Utbetalingstype
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

class VersjonTest {
    @Test
    fun `referential equals`() {
        val versjon = Utbetaling.Versjon(UUID.randomUUID(), Utbetalingstype.UTBETALING, LocalDateTime.now())
        assertEquals(versjon, versjon)
        assertEquals(versjon.hashCode(), versjon.hashCode())
    }

    @Test
    fun `structural equals`() {
        val utbetalingId = UUID.randomUUID()
        val opprettet = LocalDateTime.now()
        val versjonObjekt1 = Utbetaling.Versjon(utbetalingId, Utbetalingstype.UTBETALING, opprettet)
        val versjonObjekt2 = Utbetaling.Versjon(utbetalingId, Utbetalingstype.UTBETALING, opprettet)
        assertEquals(versjonObjekt1, versjonObjekt2)
        assertEquals(versjonObjekt1.hashCode(), versjonObjekt2.hashCode())
    }

    @Test
    fun `not equals - utbetalingId`() {
        val utbetalingId = UUID.randomUUID()
        val opprettet = LocalDateTime.now()
        val versjonObjekt1 = Utbetaling.Versjon(utbetalingId, Utbetalingstype.UTBETALING, opprettet)
        val versjonObjekt2 = Utbetaling.Versjon(UUID.randomUUID(), Utbetalingstype.UTBETALING, opprettet)
        assertNotEquals(versjonObjekt1, versjonObjekt2)
        assertNotEquals(versjonObjekt1.hashCode(), versjonObjekt2.hashCode())
    }

    @Test
    fun `not equals - utbetalingstype`() {
        val utbetalingId = UUID.randomUUID()
        val opprettet = LocalDateTime.now()
        val versjonObjekt1 = Utbetaling.Versjon(utbetalingId, Utbetalingstype.UTBETALING, opprettet)
        val versjonObjekt2 = Utbetaling.Versjon(utbetalingId, Utbetalingstype.REVURDERING, opprettet)
        assertNotEquals(versjonObjekt1, versjonObjekt2)
        assertNotEquals(versjonObjekt1.hashCode(), versjonObjekt2.hashCode())
    }

    @Test
    fun `not equals - opprettet`() {
        val utbetalingId = UUID.randomUUID()
        val opprettet = LocalDateTime.now().minusDays(1)
        val versjonObjekt1 = Utbetaling.Versjon(utbetalingId, Utbetalingstype.UTBETALING, opprettet)
        val versjonObjekt2 = Utbetaling.Versjon(utbetalingId, Utbetalingstype.UTBETALING, LocalDateTime.now())
        assertNotEquals(versjonObjekt1, versjonObjekt2)
        assertNotEquals(versjonObjekt1.hashCode(), versjonObjekt2.hashCode())
    }
}