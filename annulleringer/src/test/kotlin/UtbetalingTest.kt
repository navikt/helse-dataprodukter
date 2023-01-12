import no.nav.helse.Utbetaling
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

class UtbetalingTest {
    @Test
    fun `referential equals`() {
        val korrelasjonsId = UUID.randomUUID()
        val arbeidsgiverFagsystemId = "ARBEIDSGIVER_FAGSYSTEM_ID"
        val personFagsystemId = "PERSON_FAGSYSTEM_ID"
        val opprettet = LocalDateTime.now()
        val utbetaling = Utbetaling(
            korrelasjonsId = korrelasjonsId,
            arbeidsgiverFagsystemId = arbeidsgiverFagsystemId,
            personFagsystemId = personFagsystemId,
            opprettet = opprettet
        )
        assertEquals(utbetaling, utbetaling)
    }

    @Test
    fun `structural equals`() {
        val korrelasjonsId = UUID.randomUUID()
        val arbeidsgiverFagsystemId = "ARBEIDSGIVER_FAGSYSTEM_ID"
        val personFagsystemId = "PERSON_FAGSYSTEM_ID"
        val opprettet = LocalDateTime.now()
        val utbetaling1 = Utbetaling(korrelasjonsId, arbeidsgiverFagsystemId, personFagsystemId, opprettet)
        val utbetaling2 = Utbetaling(korrelasjonsId, arbeidsgiverFagsystemId, personFagsystemId, opprettet)
        assertEquals(utbetaling1, utbetaling2)
    }

    @Test
    fun `not equals - korrelasjonsId`() {
        val korrelasjonsId = UUID.randomUUID()
        val arbeidsgiverFagsystemId = "ARBEIDSGIVER_FAGSYSTEM_ID"
        val personFagsystemId = "PERSON_FAGSYSTEM_ID"
        val opprettet = LocalDateTime.now()
        val utbetaling1 = Utbetaling(korrelasjonsId, arbeidsgiverFagsystemId, personFagsystemId, opprettet)
        val utbetaling2 = Utbetaling(UUID.randomUUID(), arbeidsgiverFagsystemId, personFagsystemId, opprettet)
        assertNotEquals(utbetaling1, utbetaling2)
    }

    @Test
    fun `not equals - arbeidsgiverFagystemId`() {
        val korrelasjonsId = UUID.randomUUID()
        val arbeidsgiverFagsystemId = "ARBEIDSGIVER_FAGSYSTEM_ID"
        val personFagsystemId = "PERSON_FAGSYSTEM_ID"
        val opprettet = LocalDateTime.now()
        val utbetaling1 = Utbetaling(korrelasjonsId, arbeidsgiverFagsystemId, personFagsystemId, opprettet)
        val utbetaling2 = Utbetaling(korrelasjonsId, "EN_ANNEN_FAGSYSTEM_ID", personFagsystemId, opprettet)
        assertNotEquals(utbetaling1, utbetaling2)
    }

    @Test
    fun `not equals - personFagsystemId`() {
        val korrelasjonsId = UUID.randomUUID()
        val arbeidsgiverFagsystemId = "ARBEIDSGIVER_FAGSYSTEM_ID"
        val personFagsystemId = "PERSON_FAGSYSTEM_ID"
        val opprettet = LocalDateTime.now()
        val utbetaling1 = Utbetaling(korrelasjonsId, arbeidsgiverFagsystemId, personFagsystemId, opprettet)
        val utbetaling2 = Utbetaling(korrelasjonsId, arbeidsgiverFagsystemId, "EN_ANNEN_FAGSYSTEM_ID", opprettet)
        assertNotEquals(utbetaling1, utbetaling2)
    }

    @Test
    fun `not equals - opprettet`() {
        val korrelasjonsId = UUID.randomUUID()
        val arbeidsgiverFagsystemId = "ARBEIDSGIVER_FAGSYSTEM_ID"
        val personFagsystemId = "PERSON_FAGSYSTEM_ID"
        val opprettet = LocalDateTime.now().minusDays(1)
        val utbetaling1 = Utbetaling(korrelasjonsId, arbeidsgiverFagsystemId, personFagsystemId, opprettet)
        val utbetaling2 = Utbetaling(korrelasjonsId, arbeidsgiverFagsystemId, personFagsystemId, LocalDateTime.now())
        assertNotEquals(utbetaling1, utbetaling2)
    }
}