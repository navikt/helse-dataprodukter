import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.helse.Utbetaling
import no.nav.helse.UtbetalingEndretDao
import no.nav.helse.Utbetalingstype
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.sql.SQLException
import java.time.LocalDateTime
import java.util.*
import javax.sql.DataSource

internal class UtbetalingEndretDaoTest {

    @Test
    fun `kan lagre utbetaling`() = e2e {
        val korrelasjonsId = UUID.randomUUID()
        val arbeidsgiverFagsystemId = "${UUID.randomUUID()}"
        val personFagsystemId = "${UUID.randomUUID()}"
        val opprettet = LocalDateTime.now()
        val utbetaling = dao.nyUtbetalingFor(korrelasjonsId, arbeidsgiverFagsystemId, personFagsystemId, opprettet)

        assertEquals(
            Utbetaling(
                korrelasjonsId,
                arbeidsgiverFagsystemId,
                personFagsystemId,
                opprettet
            ),
            utbetaling
        )

        assertUtbetaling(korrelasjonsId, arbeidsgiverFagsystemId, personFagsystemId, false, 1)
    }

    @Test
    fun `kan lagre versjon`() = e2e {
        val korrelasjonsId = UUID.randomUUID()
        val utbetalingId = UUID.randomUUID()
        val utbetalingstype = Utbetalingstype.UTBETALING
        val arbeidsgiverFagsystemId = "${UUID.randomUUID()}"
        val personFagsystemId = "${UUID.randomUUID()}"
        val opprettet = LocalDateTime.now()
        dao.nyUtbetalingFor(korrelasjonsId, arbeidsgiverFagsystemId, personFagsystemId, opprettet)
        dao.nyVersjonFor(korrelasjonsId, utbetalingId, utbetalingstype, opprettet)

        assertUtbetaling(korrelasjonsId, arbeidsgiverFagsystemId, personFagsystemId, false, 1)
        assertUtbetalingsversjon(korrelasjonsId, utbetalingId, utbetalingstype, 1)
    }

    @Test
    fun `kan ikke lagre versjon uten utbetaling`() = e2e {
        val korrelasjonsId = UUID.randomUUID()
        val utbetalingId = UUID.randomUUID()
        val utbetalingstype = Utbetalingstype.UTBETALING
        val opprettet = LocalDateTime.now()
        assertThrows<SQLException> {
            dao.nyVersjonFor(korrelasjonsId, utbetalingId, utbetalingstype, opprettet)
        }
    }

    @Test
    fun `kan finne utbetaling`() = e2e {
        val korrelasjonsId = UUID.randomUUID()
        val arbeidsgiverFagsystemId = "${UUID.randomUUID()}"
        val personFagsystemId = "${UUID.randomUUID()}"
        val opprettet = LocalDateTime.now()
        dao.nyUtbetalingFor(korrelasjonsId, arbeidsgiverFagsystemId, personFagsystemId, opprettet)
        val funnetUtbetaling = dao.finnUtbetalingFor(korrelasjonsId)
        assertNotNull(funnetUtbetaling)
        assertEquals(
            Utbetaling(
                korrelasjonsId,
                arbeidsgiverFagsystemId,
                personFagsystemId,
                opprettet
            ),
            funnetUtbetaling
        )
    }

    @Test
    fun `kan finne utbetaling med versjon`() = e2e {
        val korrelasjonsId = UUID.randomUUID()
        val utbetalingId = UUID.randomUUID()
        val utbetalingstype = Utbetalingstype.UTBETALING
        val arbeidsgiverFagsystemId = "${UUID.randomUUID()}"
        val personFagsystemId = "${UUID.randomUUID()}"
        val opprettet = LocalDateTime.now()
        dao.nyUtbetalingFor(korrelasjonsId, arbeidsgiverFagsystemId, personFagsystemId, opprettet)
        dao.nyVersjonFor(korrelasjonsId, utbetalingId, utbetalingstype, opprettet)
        val funnetUtbetaling = dao.finnUtbetalingFor(korrelasjonsId)
        assertNotNull(funnetUtbetaling)
        assertEquals(
            Utbetaling(
                korrelasjonsId,
                arbeidsgiverFagsystemId,
                personFagsystemId,
                opprettet,
                listOf(Utbetaling.Versjon(utbetalingId, utbetalingstype, opprettet))
            ),
            funnetUtbetaling
        )
    }

    @Test
    fun `Kan markere utbetaling som annullert`() = e2e {
        val korrelasjonsId = UUID.randomUUID()
        val arbeidsgiverFagsystemId = "${UUID.randomUUID()}"
        val personFagsystemId = "${UUID.randomUUID()}"
        val opprettet = LocalDateTime.now()
        dao.nyUtbetalingFor(korrelasjonsId, arbeidsgiverFagsystemId, personFagsystemId, opprettet)
        dao.markerAnnullertFor(korrelasjonsId)
        assertUtbetaling(korrelasjonsId, arbeidsgiverFagsystemId, personFagsystemId, true, 1)
    }

    @Test
    fun `Markerer ikke utbetaling med annen korrelasjonsId som annullert`() = e2e {
        val korrelasjonsId = UUID.randomUUID()
        val arbeidsgiverFagsystemId = "${UUID.randomUUID()}"
        val personFagsystemId = "${UUID.randomUUID()}"
        val opprettet = LocalDateTime.now()
        dao.nyUtbetalingFor(korrelasjonsId, arbeidsgiverFagsystemId, personFagsystemId, opprettet)
        dao.markerAnnullertFor(UUID.randomUUID())
        assertUtbetaling(korrelasjonsId, arbeidsgiverFagsystemId, personFagsystemId, false, 1)
    }

    data class E2ETestContext(
        val dataSource: DataSource,
        val dao: UtbetalingEndretDao
    )
    private fun e2e(testblokk: E2ETestContext.() -> Unit) {
        databaseTest { ds ->
            val dao = UtbetalingEndretDao(ds)
            testblokk(E2ETestContext(ds, dao))
        }
    }

    private fun E2ETestContext.assertUtbetaling(korrelasjonsId: UUID, arbeidsgiverFagsystemId: String, personFagsystemId: String, annullert: Boolean, forventetAntall: Int) {
        @Language("PostgreSQL")
        val query = "SELECT COUNT(1) FROM utbetaling WHERE korrelasjon_id = ? AND arbeidsgiver_fagsystemid = ? AND person_fagsystemid = ? AND annullert = ?"
        val antall = sessionOf(dataSource).use { session ->
            session.run(queryOf(query, korrelasjonsId, arbeidsgiverFagsystemId, personFagsystemId, annullert).map { it.int(1) }.asSingle)
        }
        assertEquals(forventetAntall, antall) {
            "Fant $antall utbetalinger med korrelasjonsId=$korrelasjonsId, arbeidsgiverFagsystemId=$arbeidsgiverFagsystemId, personFagsystemId=$personFagsystemId. Forventet $forventetAntall utbetalinger"
        }
    }

    private fun E2ETestContext.assertUtbetalingsversjon(
        korrelasjonsId: UUID,
        utbetalingId: UUID,
        utbetalingstype: Utbetalingstype,
        forventetAntall: Int
    ) {
        @Language("PostgreSQL")
        val query = "SELECT COUNT(1) FROM utbetalingsversjon WHERE utbetaling_id = ? AND utbetaling_ref = ? AND type = ?"
        val antall = sessionOf(dataSource).use { session ->
            session.run(queryOf(query, utbetalingId, korrelasjonsId, utbetalingstype.name).map { it.int(1) }.asSingle)
        }
        assertEquals(forventetAntall, antall) {
            "Fant $antall utbetalingsversjoner med korrelasjonsId=$korrelasjonsId, utbetalingId=$utbetalingId. Forventet $forventetAntall utbetalingsversjoner"
        }
    }
}

