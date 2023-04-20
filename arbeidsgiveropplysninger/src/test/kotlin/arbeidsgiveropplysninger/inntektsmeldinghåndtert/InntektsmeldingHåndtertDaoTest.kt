package arbeidsgiveropplysninger.inntektsmeldinghåndtert

import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.helse.arbeidsgiveropplysninger.TestDatasource.getDataSource
import no.nav.helse.arbeidsgiveropplysninger.TestDatasource.resetDatabase
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InntektsmeldingHåndtertDaoTest {

    private val dataSource = getDataSource()
    val dao = InntektsmeldingHåndtertDao(dataSource)

    @Test
    fun `lagrer kobling mellom vedtaksperiode og inntektsmelding i databasen`() {
        val id = UUID.randomUUID()
        val vedtaksperiodeId = UUID.randomUUID()
        val hendelseId = UUID.randomUUID()
        val opprettet = LocalDate.EPOCH.atStartOfDay()
        dao.lagre(
            InntektsmeldingHåndtertDto(
                id = id,
                vedtaksperiodeId = vedtaksperiodeId,
                hendelseId = hendelseId,
                opprettet = opprettet
            )
        )

        assertInnslag(id, vedtaksperiodeId, hendelseId, opprettet, 1)
        assertAntallInnslag(1)
    }

    @Test
    fun `Finner nyeste inntektsmeldingId knyttet til en vedtaksperiode`() {
        val vedtaksperiodeId = UUID.randomUUID()
        val hendelseId1 = UUID.randomUUID()
        val hendelseId2 = UUID.randomUUID()
        dao.lagre(
            InntektsmeldingHåndtertDto(
                id = UUID.randomUUID(),
                vedtaksperiodeId = vedtaksperiodeId,
                hendelseId = hendelseId1,
                opprettet = LocalDateTime.now()
            )
        )
        dao.lagre(
            InntektsmeldingHåndtertDto(
                id = UUID.randomUUID(),
                vedtaksperiodeId = vedtaksperiodeId,
                hendelseId = hendelseId2,
                opprettet = LocalDateTime.now()
            )
        )

        assertEquals(hendelseId2, dao.finnHendelseId(vedtaksperiodeId))
    }

    @Test
    fun `Returnerer null dersom vi ikke finner en kobling mellom vedtaksperiodeId og inntektsmeldingId`() {
        val vedtaksperiodeId = UUID.randomUUID()
        assertNull(dao.finnHendelseId(vedtaksperiodeId))
    }

    @AfterEach
    fun reset() {
        resetDatabase()
    }

    private fun assertInnslag(
        id: UUID,
        vedtaksperiodeId: UUID,
        hendelseId: UUID,
        opprettet: LocalDateTime,
        forventetAntall: Int
    ) {
        @Language("PostgreSQL")
        val query = "SELECT COUNT(1) FROM inntektsmelding_haandtert WHERE id = ? AND vedtaksperiode_id = ? AND hendelse_id = ? AND opprettet = ?"
        val antall = sessionOf(dataSource).use { session ->
            session.run(queryOf(query, id, vedtaksperiodeId, hendelseId, opprettet).map { it.int(1) }.asSingle)
        }

        assertEquals(forventetAntall, antall)
    }

    private fun assertAntallInnslag(
        forventetAntall: Int
    ) {
        @Language("PostgreSQL")
        val query = "SELECT COUNT(1) FROM inntektsmelding_haandtert"
        val antall = sessionOf(dataSource).use { session ->
            session.run(queryOf(query).map { it.int(1) }.asSingle)
        }

        assertEquals(forventetAntall, antall)
    }
}