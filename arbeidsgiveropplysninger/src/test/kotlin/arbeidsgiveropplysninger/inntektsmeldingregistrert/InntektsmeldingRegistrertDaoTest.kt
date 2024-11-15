package arbeidsgiveropplysninger.inntektsmeldingregistrert

import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.helse.arbeidsgiveropplysninger.databaseTest
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import javax.sql.DataSource

class InntektsmeldingRegistrertDaoTest {

    @Test
    fun `lagrer kobling mellom inntektsmeldingens hendelseId og dokumentId i databasen`() = e2e {
        val id = UUID.randomUUID()
        val hendelseId = UUID.randomUUID()
        val dokumentId = UUID.randomUUID()
        val opprettet = LocalDate.EPOCH.atStartOfDay()
        dao.lagre(
            InntektsmeldingRegistrertDto(
                id = id,
                hendelseId = hendelseId,
                dokumentId = dokumentId,
                opprettet = opprettet
            )
        )

        assertInnslag(id, hendelseId, dokumentId, opprettet, 1)
        assertAntallInnslag(1)
    }

    @Test
    fun `lagrer ikke kobling mellom hendelseId og dokumentId dersom den eksisterer i databasen fra før`() = e2e {
        val hendelseId = UUID.randomUUID()
        val dokumentId = UUID.randomUUID()
        val opprettet = LocalDate.EPOCH.atStartOfDay()

        dao.lagre(
            InntektsmeldingRegistrertDto(
                id = UUID.randomUUID(),
                hendelseId = hendelseId,
                dokumentId = dokumentId,
                opprettet = opprettet
            )
        )
        dao.lagre(
            InntektsmeldingRegistrertDto(
                id = UUID.randomUUID(),
                hendelseId = hendelseId,
                dokumentId = dokumentId,
                opprettet = opprettet
            )
        )

        assertAntallInnslag(1)
    }

    data class E2ETestContext(
        val dao: InntektsmeldingRegistrertDao,
        val dataSource: DataSource
    )

    private fun e2e(testblokk: E2ETestContext.() -> Unit) {
        databaseTest { ds ->
            val dao = InntektsmeldingRegistrertDao(ds)
            testblokk(E2ETestContext(dao, ds))
        }
    }

    private fun E2ETestContext.assertInnslag(
        id: UUID,
        hendelseId: UUID,
        dokumentId: UUID,
        opprettet: LocalDateTime,
        forventetAntall: Int
    ) {
        @Language("PostgreSQL")
        val query = "SELECT COUNT(1) FROM inntektsmelding_registrert WHERE id = ? AND hendelse_id = ? AND dokument_id = ? AND opprettet = ?"
        val antall = sessionOf(dataSource).use { session ->
            session.run(queryOf(query, id, hendelseId, dokumentId, opprettet).map { it.int(1) }.asSingle)
        }

        Assertions.assertEquals(forventetAntall, antall)
    }

    private fun E2ETestContext.assertAntallInnslag(
        forventetAntall: Int
    ) {
        @Language("PostgreSQL")
        val query = "SELECT COUNT(1) FROM inntektsmelding_registrert"
        val antall = sessionOf(dataSource).use { session ->
            session.run(queryOf(query).map { it.int(1) }.asSingle)
        }

        Assertions.assertEquals(forventetAntall, antall)
    }
}