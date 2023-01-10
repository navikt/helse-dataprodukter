import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.helse.*
import no.nav.helse.IMediator
import no.nav.helse.Utbetaling
import no.nav.helse.Vedtak
import no.nav.helse.VedtakFattetRiver
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

class VedtakFattetRiverTest {
    private val testRapid = TestRapid()
    private val håndterteVedtak = mutableListOf<Vedtak>()

    init {
        VedtakFattetRiver(testRapid, mediator)
        håndterteVedtak.clear()
        testRapid.reset()
    }

    @Test
    fun `håndterer vedtak fattet`() {
        testRapid.sendTestMessage(vedtakFattet())
        assertEquals(1, håndterteVedtak.size)
    }

    @Test
    fun `håndterer ikke vedtak fattet når utbetalingId ikke er satt`() {
        testRapid.sendTestMessage(vedtakFattetUten("utbetalingId"))
        assertEquals(0, håndterteVedtak.size)
    }

    @Test
    fun `håndterer ikke vedtak fattet når vedtaksperiodeId ikke er satt`() {
        testRapid.sendTestMessage(vedtakFattetUten("vedtaksperiodeId"))
        assertEquals(0, håndterteVedtak.size)
    }

    @Test
    fun `håndterer ikke vedtak fattet når vedtakFattetTidspunkt ikke er satt`() {
        testRapid.sendTestMessage(vedtakFattetUten("vedtakFattetTidspunkt"))
        assertEquals(0, håndterteVedtak.size)
    }

    @Test
    fun `ignorerer eventer som ikke er av typen vedtak_fattet`() {
        testRapid.sendTestMessage("""{ "@event_name": "some_event" }""")
        assertEquals(0, håndterteVedtak.size)
    }

    private fun vedtakFattet(): String {
        return jacksonObjectMapper().writeValueAsString(jsonMap)
    }

    private fun vedtakFattetUten(felt: String): String {
        val json = jsonMap.toMutableMap().apply {
            remove(felt)
        }
        return jacksonObjectMapper().writeValueAsString(json)
    }

    private val jsonMap = mapOf(
        "@event_name" to "vedtak_fattet",
        "organisasjonsnummer" to "987654321",
        "vedtaksperiodeId" to "${UUID.randomUUID()}",
        "fom" to "2018-01-01",
        "tom" to "2018-01-31",
        "hendelser" to listOf("${UUID.randomUUID()}", "${UUID.randomUUID()}"),
        "skjæringstidspunkt" to "2018-01-01",
        "sykepengegrunnlag" to 600000.0,
        "grunnlagForSykepengegrunnlag" to 600000.0,
        "grunnlagForSykepengegrunnlagPerArbeidsgiver" to mapOf(
            "987654321" to 600000.0
        ),
        "begrensning" to "ER_IKKE_6G_BEGRENSET",
        "inntekt" to 50000.0,
        "vedtakFattetTidspunkt" to "2018-02-01T00:00:00.000",
        "utbetalingId" to "${UUID.randomUUID()}",
        "@id" to "${UUID.randomUUID()}",
        "@opprettet" to "2018-02-01T00:00:00.000",
        "aktørId" to "1234567891011",
        "fødselsnummer" to "12345678910"
    )

    private val mediator get() = object : IMediator {
        override fun håndter(vedtaksperiodeId: UUID, vedtak: Vedtak) {
            håndterteVedtak.add(vedtak)
        }

        override fun håndter(korrelasjonsId: UUID, utbetaling: Utbetaling) {
            TODO("Not yet implemented")
        }

        override fun håndterAnnullering(korrelasjonsId: UUID) {
            TODO("Not yet implemented")
        }

        override fun nyUtbetaling(
            korrelasjonsId: UUID,
            utbetalingId: UUID,
            utbetalingstype: Utbetalingstype,
            opprettet: LocalDateTime
        ) {
            TODO("Not yet implemented")
        }
    }
}
