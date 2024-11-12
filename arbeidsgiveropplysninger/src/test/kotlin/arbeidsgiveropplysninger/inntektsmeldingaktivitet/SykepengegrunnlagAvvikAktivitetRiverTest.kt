package arbeidsgiveropplysninger.inntektsmeldingaktivitet

import arbeidsgiveropplysninger.inntektsmeldinghåndtert.InntektsmeldingHåndtertDao
import com.github.navikt.tbd_libs.rapids_and_rivers.test_support.TestRapid
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

class SykepengegrunnlagAvvikAktivitetRiverTest {

    private val testRapid = TestRapid()
    private val inntektsmeldingAktivitetDao = mockk<InntektsmeldingAktivitetDao>()
    private val inntektsmeldingHåndtertDao = mockk<InntektsmeldingHåndtertDao>()

    init {
        SykepengegrunnlagAvvikAktivitetRiver(testRapid, inntektsmeldingAktivitetDao, inntektsmeldingHåndtertDao)
    }

    @BeforeEach
    fun clear() {
        clearMocks(inntektsmeldingAktivitetDao, inntektsmeldingHåndtertDao)
    }

    @Test
    fun `lagrer melding med relevant varselkode`() {
        val inntektsmeldingId = UUID.randomUUID()
        every { inntektsmeldingAktivitetDao.lagre(any()) } returns true
        every { inntektsmeldingHåndtertDao.finnHendelseId(any()) } returns inntektsmeldingId

        val id = UUID.randomUUID()
        val vedtaksperiodeId = UUID.randomUUID()
        val tidsstempel = LocalDateTime.now()
        val varselkode = "RV_IV_2"
        testRapid.sendJson(
            id = id,
            varselkode = varselkode,
            vedtaksperiodeId = vedtaksperiodeId,
            tidsstempel = tidsstempel
        )

        verify(exactly = 1) {
            inntektsmeldingAktivitetDao.lagre(
                InntektsmeldingAktivitetDto(
                    id = id,
                    hendelseId = inntektsmeldingId,
                    varselkode = varselkode,
                    nivå = "FUNKSJONELL_FEIL",
                    melding = "Dette er en melding",
                    tidsstempel = tidsstempel
                )
            )
        }
    }

    @Test
    fun `lagrer ikke melding dersom vi ikke finner inntektsmeldingId i databasen`() {
        every { inntektsmeldingHåndtertDao.finnHendelseId(any()) } returns null

        testRapid.sendJson(
            id = UUID.randomUUID(),
            vedtaksperiodeId = UUID.randomUUID(),
            tidsstempel = LocalDateTime.now()
        )

        verify(exactly = 0) { inntektsmeldingAktivitetDao.lagre(any()) }

    }
    @Test
    fun `lagrer ikke melding dersom vi ikke finner vedtaksperiodeId i meldingen`() {
        testRapid.sendJsonUtenVedtaksperiodeId()

        verify(exactly = 0) { inntektsmeldingAktivitetDao.lagre(any()) }
    }

    @Test
    fun `lagrer ikke melding med ikke-relevant varselkode`() {
        testRapid.sendJson(
            varselkode = "RV_IV_1",
            id = UUID.randomUUID(),
            vedtaksperiodeId = UUID.randomUUID(),
            tidsstempel = LocalDateTime.now()
        )

        verify(exactly = 0) { inntektsmeldingAktivitetDao.lagre(any()) }
    }

    @Test
    fun `plukker ikke opp melding med annet event-name`() {
        testRapid.sendJson(eventName = "feil_event")

        verify(exactly = 0) { inntektsmeldingHåndtertDao.lagre(any()) }
    }

    private fun TestRapid.sendJson(
        eventName: String = "aktivitetslogg_ny_aktivitet",
        varselkode: String = "RV_IV_2",
        id: UUID = UUID.randomUUID(),
        vedtaksperiodeId: UUID = UUID.randomUUID(),
        tidsstempel: LocalDateTime = LocalDateTime.now()
    ) = sendTestMessage(
        """
       {
            "@event_name": "$eventName",
            "@id": "${UUID.randomUUID()}",
           
            "aktiviteter": [
                {
                    "id": "$id",
                    "nivå": "FUNKSJONELL_FEIL",
                    "melding": "Dette er en melding",  
                    "tidsstempel": "$tidsstempel",
                    "kontekster": [
                        {
                          "konteksttype": "Vedtaksperiode",
                          "kontekstmap": {
                            "vedtaksperiodeId": "$vedtaksperiodeId"
                          }
                        }
                    ],
                    "varselkode": "$varselkode" 
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
    private fun TestRapid.sendJsonUtenVedtaksperiodeId() = sendTestMessage(
        """
       {
            "@event_name": "aktivitetslogg_ny_aktivitet",
            "@id": "${UUID.randomUUID()}",
           
            "aktiviteter": [
                {
                    "id": "${UUID.randomUUID()}",
                    "nivå": "FUNKSJONELL_FEIL",
                    "melding": "Dette er en melding",  
                    "tidsstempel": "${LocalDateTime.now()}",
                    "kontekster": [
                        {
                          "konteksttype": "Vedtaksperiode",
                          "kontekstmap": {
                          }
                        }
                    ],
                    "varselkode": "RV_IV_2" 
                },
                {
                    "id": "${UUID.randomUUID()}",
                    "nivå": "INFO",
                    "melding": "Dette er en melding",  
                    "tidsstempel": "${LocalDateTime.now()}"
                }
            ]
       } 
       """
    )

}