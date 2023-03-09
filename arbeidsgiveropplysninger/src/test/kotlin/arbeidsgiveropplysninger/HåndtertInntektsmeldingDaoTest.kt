package no.nav.helse.arbeidsgiveropplysninger

import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.helse.arbeidsgiveropplysninger.TestDatasource.getDataSource
import no.nav.helse.arbeidsgiveropplysninger.TestDatasource.resetDatabase
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HåndtertInntektsmeldingDaoTest {

    private val dataSource = getDataSource()
    val dao = HåndtertInntektsmeldingDao(dataSource)

    @Test
    fun `lagrer kobling mellom vedtaksperiode og inntektsmelding i databasen`() {
        val id = UUID.randomUUID()
        val vedtaksperiodeId = UUID.randomUUID()
        val inntektsmeldingId = UUID.randomUUID()
        val opprettet = LocalDate.EPOCH.atStartOfDay()
        dao.lagre(
            HåndtertInntektsmeldingDto(
                id = id,
                vedtaksperiodeId = vedtaksperiodeId,
                inntektsmeldingId = inntektsmeldingId,
                opprettet = opprettet
            )
        )

        assertInnslag(id, vedtaksperiodeId, inntektsmeldingId, opprettet, 1)
        assertAntallInnslag(1)
    }

    @AfterEach
    fun reset() {
        resetDatabase()
    }

    private fun assertInnslag(
        id: UUID,
        vedtaksperiodeId: UUID,
        inntektsmeldingId: UUID,
        opprettet: LocalDateTime,
        forventetAntall: Int
    ) {
        @Language("PostgreSQL")
        val query = "SELECT COUNT(1) FROM haandtert_inntektsmelding WHERE id = ? AND vedtaksperiode_id = ? AND inntektsmelding_id = ? AND opprettet = ?"
        val antall = sessionOf(dataSource).use { session ->
            session.run(queryOf(query, id, vedtaksperiodeId, inntektsmeldingId, opprettet).map { it.int(1) }.asSingle)
        }

        Assertions.assertEquals(forventetAntall, antall)
    }

    private fun assertAntallInnslag(
        forventetAntall: Int
    ) {
        @Language("PostgreSQL")
        val query = "SELECT COUNT(1) FROM haandtert_inntektsmelding"
        val antall = sessionOf(dataSource).use { session ->
            session.run(queryOf(query).map { it.int(1) }.asSingle)
        }

        Assertions.assertEquals(forventetAntall, antall)
    }
}