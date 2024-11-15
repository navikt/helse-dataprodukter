package arbeidsgiveropplysninger.arbeidsgiveropplysningerkorrigert

import arbeidsgiveropplysninger.arbeidsgiveropplysningerkorrigert.ArbeidsgiveropplysningerKorrigertDto.KorrigerendeInntektektsopplysningstype.INNTEKTSMELDING
import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.helse.arbeidsgiveropplysninger.databaseTest
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*
import javax.sql.DataSource

class ArbeidsgiveropplysningerKorrigertDaoTest {

    @Test
    fun `lagrer korrigering av arbeidsgiveropplysninger i databasen`() = e2e {
        val id = UUID.randomUUID()
        val korrigertInntektsmeldingId = UUID.randomUUID()
        val korrigerendeInntektsopplysningId = UUID.randomUUID()
        val korrigerendeInntektektsopplysningstype = INNTEKTSMELDING
        val opprettet = LocalDateTime.now()
        dao.lagre(
            ArbeidsgiveropplysningerKorrigertDto(
                id,
                korrigertInntektsmeldingId,
                korrigerendeInntektsopplysningId,
                korrigerendeInntektektsopplysningstype,
                opprettet
            )
        )

        assertInnslag(
            id = id,
            korrigertInntektsmeldingId = korrigertInntektsmeldingId,
            korrigerendeInntektsopplysningId = korrigerendeInntektsopplysningId,
            korrigerendeInntektektsopplysningstype = korrigerendeInntektektsopplysningstype.name,
            opprettet = opprettet,
            forventetAntall = 1
        )
        assertAntallInnslag(1)
    }

    data class E2ETestContext(
        val dao: ArbeidsgiveropplysningerKorrigertDao,
        val dataSource: DataSource
    )

    private fun e2e(testblokk: E2ETestContext.() -> Unit) {
        databaseTest { ds ->
            val dao = ArbeidsgiveropplysningerKorrigertDao(ds)
            testblokk(E2ETestContext(dao, ds))
        }
    }

    private fun E2ETestContext.assertInnslag(
        id: UUID,
        korrigertInntektsmeldingId: UUID,
        korrigerendeInntektsopplysningId: UUID,
        korrigerendeInntektektsopplysningstype: String,
        opprettet: LocalDateTime,
        forventetAntall: Int
    ) {
        @Language("PostgreSQL")
        val query = "SELECT COUNT(1) FROM arbeidsgiveropplysninger_korrigert WHERE id = ? AND korrigert_inntektsmelding_id = ? AND korrigerende_inntektsopplysning_id = ? AND korrigerende_inntektektsopplysningstype = ? AND opprettet = ?"
        val antall = sessionOf(dataSource).use { session ->
            session.run(queryOf(query, id, korrigertInntektsmeldingId, korrigerendeInntektsopplysningId, korrigerendeInntektektsopplysningstype, opprettet).map { it.int(1) }.asSingle)
        }

        assertEquals(forventetAntall, antall)
    }

    private fun E2ETestContext.assertAntallInnslag(
        forventetAntall: Int
    ) {
        @Language("PostgreSQL")
        val query = "SELECT COUNT(1) FROM arbeidsgiveropplysninger_korrigert"
        val antall = sessionOf(dataSource).use { session ->
            session.run(queryOf(query).map { it.int(1) }.asSingle)
        }

        assertEquals(forventetAntall, antall)
    }
}