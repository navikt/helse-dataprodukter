import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.helse.*
import no.nav.helse.IMediator
import no.nav.helse.Utbetaling
import no.nav.helse.Vedtak
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

class UtbetalingEndretRiverTest {
    private val testRapid = TestRapid()
    private val håndterteUtbetalinger = mutableListOf<Utbetaling>()

    init {
        UtbetalingEndretRiver(testRapid, mediator)
        håndterteUtbetalinger.clear()
        testRapid.reset()
    }

    @Test
    fun `håndterer vedtak fattet`() {
        testRapid.sendTestMessage(utbetalingEndret())
        assertEquals(1, håndterteUtbetalinger.size)
    }

    @Test
    fun `håndterer ikke vedtak fattet når utbetalingId ikke er satt`() {
        testRapid.sendTestMessage(utbetalingEndretUten("utbetalingId"))
        assertEquals(0, håndterteUtbetalinger.size)
    }

    @Test
    fun `håndterer ikke vedtak fattet når korrelasjonsId ikke er satt`() {
        testRapid.sendTestMessage(utbetalingEndretUten("korrelasjonsId"))
        assertEquals(0, håndterteUtbetalinger.size)
    }

    @Test
    fun `håndterer ikke vedtak fattet når gjeldendeStatus ikke er satt`() {
        testRapid.sendTestMessage(utbetalingEndretUten("gjeldendeStatus"))
        assertEquals(0, håndterteUtbetalinger.size)
    }

    @Test
    fun `håndterer ikke vedtak fattet når type ikke er satt`() {
        testRapid.sendTestMessage(utbetalingEndretUten("type"))
        assertEquals(0, håndterteUtbetalinger.size)
    }

    @Test
    fun `håndterer vedtak fattet når type er akseptert verdi`() {
        testRapid.sendTestMessage(utbetalingEndretMedVerdi("type", "ANNULLERING"))
        testRapid.sendTestMessage(utbetalingEndretMedVerdi("type", "REVURDERING"))
        testRapid.sendTestMessage(utbetalingEndretMedVerdi("type", "UTBETALING"))
        assertEquals(3, håndterteUtbetalinger.size)
    }

    @Test
    fun `håndterer ikke vedtak fattet når type ikke er akseptert verdi`() {
        testRapid.sendTestMessage(utbetalingEndretMedVerdi("type", "FERIEPENGER"))
        testRapid.sendTestMessage(utbetalingEndretMedVerdi("type", "ETTERUTBETALING"))
        assertEquals(0, håndterteUtbetalinger.size)
    }

    @Test
    fun `håndterer vedtak fattet når gjeldendeStatus er akseptert verdi`() {
        testRapid.sendTestMessage(utbetalingEndretMedVerdi("gjeldendeStatus", "ANNULLERT"))
        testRapid.sendTestMessage(utbetalingEndretMedVerdi("gjeldendeStatus", "UTBETALT"))
        testRapid.sendTestMessage(utbetalingEndretMedVerdi("gjeldendeStatus", "GODKJENT_UTEN_UTBETALING"))
        assertEquals(3, håndterteUtbetalinger.size)
    }

    @Test
    fun `håndterer ikke vedtak fattet når gjeldendeStatus ikke er akseptert verdi`() {
        testRapid.sendTestMessage(utbetalingEndretMedVerdi("gjeldendeStatus", "IKKE_UTBETALT"))
        testRapid.sendTestMessage(utbetalingEndretMedVerdi("gjeldendeStatus", "FORKASTET"))
        testRapid.sendTestMessage(utbetalingEndretMedVerdi("gjeldendeStatus", "NY"))
        testRapid.sendTestMessage(utbetalingEndretMedVerdi("gjeldendeStatus", "IKKE_GODKJENT"))
        testRapid.sendTestMessage(utbetalingEndretMedVerdi("gjeldendeStatus", "UTBETALING_FEILET"))
        testRapid.sendTestMessage(utbetalingEndretMedVerdi("gjeldendeStatus", "SENDT"))
        testRapid.sendTestMessage(utbetalingEndretMedVerdi("gjeldendeStatus", "OVERFØRT"))
        testRapid.sendTestMessage(utbetalingEndretMedVerdi("gjeldendeStatus", "GODKJENT"))
        assertEquals(0, håndterteUtbetalinger.size)
    }

    @Test
    fun `ignorerer eventer som ikke er av typen utbetaling_endret`() {
        testRapid.sendTestMessage("""{ "@event_name": "some_event" }""")
        assertEquals(0, håndterteUtbetalinger.size)
    }

    private fun utbetalingEndret(): String {
        return jacksonObjectMapper().writeValueAsString(jsonMap)
    }

    private fun utbetalingEndretUten(felt: String): String {
        val json = jsonMap.toMutableMap().apply {
            remove(felt)
        }
        return jacksonObjectMapper().writeValueAsString(json)
    }

    private fun utbetalingEndretMedVerdi(nøkkel: String, verdi: String): String {
        val json = jsonMap.toMutableMap().apply {
            replace(nøkkel, verdi)
        }
        return jacksonObjectMapper().writeValueAsString(json)
    }

    private val jsonMap = mapOf(
        "@event_name" to "utbetaling_endret",
        "organisasjonsnummer" to "987654321",
        "utbetalingId" to "${UUID.randomUUID()}",
        "type" to "UTBETALING",
        "forrigeStatus" to "OVERFØRT",
        "gjeldendeStatus" to "UTBETALT",
        "korrelasjonsId" to "${UUID.randomUUID()}",
        "arbeidsgiverOppdrag" to mapOf("fagsystemId" to "ARBEIDSGIVER_FAGSYSTEM_ID"),
        "personOppdrag" to mapOf("fagsystemId" to "PERSON_FAGSYSTEM_ID"),
        "@id" to "${UUID.randomUUID()}",
        "@opprettet" to "2018-02-01T00:00:00.000",
        "aktørId" to "1234567891011",
        "fødselsnummer" to "12345678910"
    )

    private val mediator get() = object : IMediator {
        override fun håndter(korrelasjonsId: UUID, utbetaling: Utbetaling, versjon: Utbetaling.Versjon) {
            håndterteUtbetalinger.add(utbetaling)
        }

        override fun håndter(vedtaksperiodeId: UUID, vedtak: Vedtak) {
            TODO("Not yet implemented")
        }

        override fun håndterAnnullering(korrelasjonsId: UUID) {
            TODO("Not yet implemented")
        }

        override fun nyUtbetaling(
            korrelasjonsId: UUID,
            arbeidsgiverFagsystemId: String,
            personFagsystemId: String,
            opprettet: LocalDateTime
        ) {
            TODO("Not yet implemented")
        }

        override fun nyVersjon(
            korrelasjonsId: UUID,
            utbetalingId: UUID,
            utbetalingstype: Utbetalingstype,
            opprettet: LocalDateTime
        ) {
            TODO("Not yet implemented")
        }
    }
}
