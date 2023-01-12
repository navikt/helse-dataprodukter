import TestDatasource.getDataSource
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.helse.Mediator
import no.nav.helse.UtbetalingEndretDao
import no.nav.helse.Utbetalingstype
import no.nav.helse.Utbetalingstype.ANNULLERING
import no.nav.helse.Utbetalingstype.UTBETALING
import no.nav.helse.VedtakFattetDao
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*
import javax.sql.DataSource

internal class EndToEndTest {
    private val dataSource: DataSource = getDataSource()
    private val testRapid = TestRapid()

    init {
        Mediator(
            testRapid,
            VedtakFattetDao(dataSource),
            UtbetalingEndretDao(dataSource)
        )
    }

    @Test
    fun `happy case`() {
        val vedtaksperiodeId = UUID.randomUUID()
        val utbetalingId = UUID.randomUUID()
        val korrelasjonsId = UUID.randomUUID()
        sendVedtakFattet(utbetalingId, vedtaksperiodeId)
        sendUtbetalingEndret(utbetalingId, korrelasjonsId, UTBETALING)
        sendUtbetalingEndret(UUID.randomUUID(), korrelasjonsId, ANNULLERING)
        assertAnnullertUtbetaling(korrelasjonsId, true)
        assertAnnullertVedtak(vedtaksperiodeId, true)
    }

    @Test
    fun `happy case med flere vedtaksperioder`() {
        val vedtaksperiodeId = UUID.randomUUID()
        val vedtaksperiodeId2 = UUID.randomUUID()
        val utbetalingId = UUID.randomUUID()
        val utbetalingId2 = UUID.randomUUID()
        val korrelasjonsId = UUID.randomUUID()
        sendVedtakFattet(utbetalingId, vedtaksperiodeId)
        sendVedtakFattet(utbetalingId2, vedtaksperiodeId2)
        sendUtbetalingEndret(utbetalingId, korrelasjonsId, UTBETALING)
        sendUtbetalingEndret(utbetalingId2, korrelasjonsId, UTBETALING)
        sendUtbetalingEndret(UUID.randomUUID(), korrelasjonsId, ANNULLERING)
        assertAnnullertUtbetaling(korrelasjonsId, true)
        assertAnnullertVedtak(vedtaksperiodeId, true)
        assertAnnullertVedtak(vedtaksperiodeId2, true)
    }

    @Test
    fun `markerer kun relevante ting som annullert`() {
        val vedtaksperiodeId = UUID.randomUUID()
        val utbetalingId = UUID.randomUUID()
        val korrelasjonsId = UUID.randomUUID()
        sendVedtakFattet(utbetalingId, vedtaksperiodeId)
        sendUtbetalingEndret(utbetalingId, korrelasjonsId, UTBETALING)
        sendUtbetalingEndret(UUID.randomUUID(), UUID.randomUUID(), ANNULLERING)
        assertAnnullertUtbetaling(korrelasjonsId, false)
        assertAnnullertVedtak(vedtaksperiodeId, false)
    }

    private fun assertAnnullertUtbetaling(korrelasjonsId: UUID, annullert: Boolean) {
        @Language("PostgreSQL")
        val query = "SELECT COUNT(1) FROM utbetaling WHERE korrelasjon_id = ? AND annullert = ?"
        val antall = sessionOf(dataSource).use { session ->
            session.run(queryOf(query, korrelasjonsId, annullert).map { it.int(1) }.asSingle)
        }
        assertEquals(1, antall)
    }

    private fun assertAnnullertVedtak(vedtaksperiodeId: UUID, annullert: Boolean) {
        @Language("PostgreSQL")
        val query = "SELECT COUNT(1) FROM vedtak_fattet WHERE vedtaksperiode_id = ? AND annullert = ?"
        val antall = sessionOf(dataSource).use { session ->
            session.run(queryOf(query, vedtaksperiodeId, annullert).map { it.int(1) }.asSingle)
        }
        assertEquals(1, antall)
    }

    private fun sendVedtakFattet(utbetalingId: UUID, vedtaksperiodeId: UUID) {
        testRapid.sendTestMessage(
            vedtakFattetMedVerdi(
                mapOf(
                    "utbetalingId" to utbetalingId.toString(),
                    "vedtaksperiodeId" to vedtaksperiodeId.toString()
                )
            )
        )
    }

    private fun sendUtbetalingEndret(
        utbetalingId: UUID,
        korrelasjonsId: UUID,
        utbetalingstype: Utbetalingstype,
    ) {
        testRapid.sendTestMessage(
            utbetalingEndretMedVerdi(
                mapOf(
                    "utbetalingId" to utbetalingId.toString(),
                    "type" to utbetalingstype.name,
                    "korrelasjonsId" to korrelasjonsId.toString(),
                )
            )
        )
    }

    private fun vedtakFattetMedVerdi(endringer: Map<String, String>): String {
        val json = vedtakFattetJsonMap.toMutableMap().apply {
            endringer.forEach {(nøkkel, verdi) ->
                replace(nøkkel, verdi)
            }
        }
        return jacksonObjectMapper().writeValueAsString(json)
    }

    private fun utbetalingEndretMedVerdi(endringer: Map<String, String>): String {
        val json = utbetalingEndretJsonMap.toMutableMap().apply {
            endringer.forEach {(nøkkel, verdi) ->
                replace(nøkkel, verdi)
            }
        }
        return jacksonObjectMapper().writeValueAsString(json)
    }

    private val utbetalingEndretJsonMap get() = mapOf(
        "@event_name" to "utbetaling_endret",
        "organisasjonsnummer" to "987654321",
        "utbetalingId" to "${UUID.randomUUID()}",
        "type" to "UTBETALING",
        "forrigeStatus" to "OVERFØRT",
        "gjeldendeStatus" to "UTBETALT",
        "korrelasjonsId" to "${UUID.randomUUID()}",
        "arbeidsgiverOppdrag" to mapOf("fagsystemId" to "${UUID.randomUUID()}"),
        "personOppdrag" to mapOf("fagsystemId" to "${UUID.randomUUID()}"),
        "@id" to "${UUID.randomUUID()}",
        "@opprettet" to "2018-02-01T00:00:00.000",
        "aktørId" to "1234567891011",
        "fødselsnummer" to "12345678910"
    )


    private val vedtakFattetJsonMap = mapOf(
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
}