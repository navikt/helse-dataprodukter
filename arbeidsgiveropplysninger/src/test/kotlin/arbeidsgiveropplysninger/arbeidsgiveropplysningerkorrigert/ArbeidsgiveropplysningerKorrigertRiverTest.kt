package arbeidsgiveropplysninger.arbeidsgiveropplysningerkorrigert

import arbeidsgiveropplysninger.arbeidsgiveropplysningerkorrigert.ArbeidsgiveropplysningerKorrigertDto.KorrigerendeInntektektsopplysningstype.INNTEKTSMELDING
import com.github.navikt.tbd_libs.rapids_and_rivers.test_support.TestRapid
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime
import java.util.UUID


internal class ArbeidsgiveropplysningerKorrigertRiverTest {
    private val testRapid = TestRapid()
    private val dao = mockk<ArbeidsgiveropplysningerKorrigertDao>()

    init {
        ArbeidsgiveropplysningerKorrigertRiver(testRapid, dao)
    }

    @BeforeEach
    fun clear() {
        clearMocks(dao)
    }

    @Test
    fun `lagrer melding med gyldig format`() {
        every { dao.lagre(any()) } returns true

        val korrigertInntektsmeldingId = UUID.randomUUID()
        val korrigerendeInntektsopplysningId = UUID.randomUUID()
        val opprettet = LocalDateTime.now()

        testRapid.sendJson(
            korrigertInntektsmeldingId = korrigertInntektsmeldingId,
            korrigerendeInntektsopplysningId = korrigerendeInntektsopplysningId,
            opprettet = opprettet
        )

        verify {
            dao.lagre(match {
                it.korrigertInntektsmeldingId == korrigertInntektsmeldingId &&
                it.korrigerendeInntektsopplysningId == korrigerendeInntektsopplysningId &&
                it.korrigerendeInntektektsopplysningstype == INNTEKTSMELDING
            })
        }
    }

    @Test
    fun `lagrer ikke melding med ugyldig korrigerendeInntektektsopplysningstype`() {
        every { dao.lagre(any()) } returns true

        assertThrows<IllegalArgumentException> { testRapid.sendJson(korrigerendeInntektektsopplysningstype = "tulletype") }

        verify(exactly = 0) {
            dao.lagre(any())
        }
    }

    @Test
    fun `lagrer ikke melding med ugyldig id-er`() {
        every { dao.lagre(any()) } returns true

        assertThrows<IllegalArgumentException> {
            testRapid.sendTestMessage(
                """
                {
                    "@event_name": "arbeidsgiveropplysninger_korrigert",
                    "@id": "${UUID.randomUUID()}",
                    "korrigertInntektsmeldingId": "dette er ikke en uuid",
                    "korrigerendeInntektsopplysningId": "dette er heller ikke en uuid",
                    "korrigerendeInntektektsopplysningstype": "INNTEKTSMELDING",
                    "opprettet": "${LocalDateTime.now()}"
                } 
                """
            )
        }

        verify(exactly = 0) {
            dao.lagre(any())
        }
    }

    @Test
    fun `plukker ikke opp melding med annet event-name`() {
        every { dao.lagre(any()) } returns true

        testRapid.sendJson(eventName = "feil_event")

        verify(exactly = 0) {
            dao.lagre(any())
        }
    }

    private fun TestRapid.sendJson(
        eventName: String = "arbeidsgiveropplysninger_korrigert",
        korrigertInntektsmeldingId: UUID = UUID.randomUUID(),
        korrigerendeInntektsopplysningId: UUID  = UUID.randomUUID(),
        korrigerendeInntektektsopplysningstype: String = "INNTEKTSMELDING",
        opprettet: LocalDateTime = LocalDateTime.now()
    ) = sendTestMessage(
        """
       {
            "@event_name": "$eventName",
            "@id": "${UUID.randomUUID()}",
            "korrigertInntektsmeldingId": "$korrigertInntektsmeldingId",
            "korrigerendeInntektsopplysningId": "$korrigerendeInntektsopplysningId",
            "korrigerendeInntektektsopplysningstype": "$korrigerendeInntektektsopplysningstype",
            "opprettet": "$opprettet"
       } 
       """
    )
}