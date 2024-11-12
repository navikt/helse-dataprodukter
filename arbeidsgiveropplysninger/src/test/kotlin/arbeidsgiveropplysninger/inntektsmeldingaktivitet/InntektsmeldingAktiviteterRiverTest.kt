package arbeidsgiveropplysninger.inntektsmeldingaktivitet

import com.github.navikt.tbd_libs.rapids_and_rivers.test_support.TestRapid
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID

class InntektsmeldingAktiviteterRiverTest {

    private val testRapid = TestRapid()
    private val dao = mockk<InntektsmeldingAktivitetDao>()

    init {
        InntektsmeldingAktiviteterRiver(testRapid, dao)
    }

    @BeforeEach
    fun clear() {
        clearMocks(dao)
    }

    @Test
    fun `lagrer melding med relevant varselkode`() {
        every { dao.lagre(any()) } returns true

        val id = UUID.randomUUID()
        val hendelseId = UUID.randomUUID()
        val tidsstempel = LocalDateTime.now()
        val varselkode = "RV_IM_2"
        testRapid.sendJson(
            id = id,
            hendelseId = hendelseId,
            varselkode = varselkode,
            tidsstempel = tidsstempel
        )

        verify(exactly = 1) {
            dao.lagre(
                InntektsmeldingAktivitetDto(
                id = id,
                hendelseId = hendelseId,
                varselkode = varselkode,
                nivå = "VARSEL",
                melding = "Dette er en melding",
                tidsstempel = tidsstempel
            )
            )
        }
    }

    @Test
    fun `lagrer ikke melding med varselkode som ikke er relevant`() {
        every { dao.lagre(any()) } returns true

        testRapid.sendJson(varselkode = "tullekode")

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

    @Test
    fun `plukker ikke opp melding som ikke gjelder inntektsmelding`() {
        every { dao.lagre(any()) } returns true

        testRapid.sendJson(forårsaketAvEventName = "ikke_inntektsmelding")

        verify(exactly = 0) {
            dao.lagre(any())
        }
    }

    @Test
    fun `lagrer aktivitet forårsaket av inntektsmelding-replay`() {
        every { dao.lagre(any()) } returns true
        testRapid.sendJson(forårsaketAvEventName = "inntektsmelding_replay")

        verify(exactly = 1) {
            dao.lagre(any())
        }
    }

    private fun TestRapid.sendJson(
        eventName: String = "aktivitetslogg_ny_aktivitet",
        forårsaketAvEventName: String = "inntektsmelding",
        varselkode: String = "RV_IM_2",
        hendelseId: UUID = UUID.randomUUID(),
        id: UUID = UUID.randomUUID(),
        tidsstempel: LocalDateTime = LocalDateTime.now()
    ) = sendTestMessage(
       """
       {
            "@event_name": "$eventName",
            "@id": "${UUID.randomUUID()}",
            "@forårsaket_av": {
                "event_name": "$forårsaketAvEventName", 
                 "id": "$hendelseId"
            },
            "aktiviteter": [
                {
                    "id": "$id",
                    "nivå": "VARSEL",
                    "melding": "Dette er en melding",  
                    "varselkode": "$varselkode", 
                    "tidsstempel": "$tidsstempel"
                },
                {
                    "id": "$id",
                    "nivå": "INFO",
                    "melding": "Dette er en melding",  
                    "tidsstempel": "$tidsstempel"
                }
            ]
       } 
       """
    )
}