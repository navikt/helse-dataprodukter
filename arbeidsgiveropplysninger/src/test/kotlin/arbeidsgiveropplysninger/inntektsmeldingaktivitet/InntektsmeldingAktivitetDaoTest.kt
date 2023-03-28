package arbeidsgiveropplysninger.inntektsmeldingaktivitet

import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.helse.arbeidsgiveropplysninger.TestDatasource.getDataSource
import no.nav.helse.arbeidsgiveropplysninger.TestDatasource.resetDatabase
import no.nav.helse.arbeidsgiveropplysninger.mockInntektsmeldingAktivitet
import no.nav.helse.arbeidsgiveropplysninger.mockInntektsmelingAktiviteter
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import java.util.UUID

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InntektsmeldingAktivitetDaoTest {

    private val dataSource = getDataSource()
    private val dao = InntektsmeldingAktivitetDao(dataSource)

    @Test
    fun `lagrer aktivtet`() {
        val inntektsmeldingId = UUID.randomUUID()
        val aktiviteter = mockInntektsmelingAktiviteter(inntektsmeldingId)

        aktiviteter.forEach {
            dao.lagre(it)
        }

        assertInnslag(inntektsmeldingId,2)
        assertAntallInnslag(2)
    }

    @Test
    fun `lagrer ikke innslag dersom det eksisterer et innslag med lik inntektsmeldingId og varselkode`() {
        val inntektsmeldingId = UUID.randomUUID()
        val aktivitet1 = mockInntektsmeldingAktivitet(inntektsmeldingId, "varselkode")
        val aktivitet2 = mockInntektsmeldingAktivitet(inntektsmeldingId, "varselkode")

        dao.lagre(aktivitet1)
        assertAntallInnslag(1)

        dao.lagre(aktivitet2)
        assertAntallInnslag(1)
    }

    @AfterEach
    fun reset() {
        resetDatabase()
    }

    private fun assertInnslag(
        id: UUID,
        forventetAntall: Int
    ) {
        @Language("PostgreSQL")
        val query = "SELECT COUNT(1) FROM inntektsmelding_aktivitet WHERE inntektsmelding_id = ?"
        val antall = sessionOf(dataSource).use { session ->
            session.run(queryOf(query, id).map { it.int(1) }.asSingle)
        }

        assertEquals(forventetAntall, antall)
    }

    private fun assertAntallInnslag(forventetAntall: Int) {
        @Language("PostgreSQL")
        val query = "SELECT COUNT(1) FROM inntektsmelding_aktivitet"
        val antall = sessionOf(dataSource).use { session ->
            session.run(queryOf(query).map { it.int(1) }.asSingle)
        }

        assertEquals(forventetAntall, antall)
    }
}