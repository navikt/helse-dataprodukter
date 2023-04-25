package arbeidsgiveropplysninger.arbeidsgiveropplysningerkorrigert

import arbeidsgiveropplysninger.arbeidsgiveropplysningerkorrigert.ArbeidsgiveropplysningerKorrigertDto.KorrigerendeInntektektsopplysningstype.INNTEKTSMELDING
import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.helse.arbeidsgiveropplysninger.TestDatasource.getDataSource
import no.nav.helse.arbeidsgiveropplysninger.TestDatasource.resetDatabase
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.LocalDateTime
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ArbeidsgiveropplysningerKorrigertDaoTest {

    private val dataSource = getDataSource()
    private val dao = ArbeidsgiveropplysningerKorrigertDao(dataSource)

    @Test
    fun `lagrer korrigering av arbeidsgiveropplysninger i databasen`() {
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

    @BeforeEach
    fun reset() {
        resetDatabase()
    }

    private fun assertInnslag(
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

    private fun assertAntallInnslag(
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