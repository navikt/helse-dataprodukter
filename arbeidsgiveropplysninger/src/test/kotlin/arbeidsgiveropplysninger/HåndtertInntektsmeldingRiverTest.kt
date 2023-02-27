package arbeidsgiveropplysninger

import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID


class HåndtertInntektsmeldingRiverTest {

    private val testRapid = TestRapid()
    private val dao = mockk<HåndtertInntektsmeldingDao>(relaxed = true)

    init {
        HåndtertInntektsmeldingRiver(testRapid, dao)
    }

    @BeforeEach
    fun clear() {
        clearMocks(dao)
    }

    @Test
    fun `lagrer korrekte meldinger i databasen`() {
        every { dao.lagre(any()) } returns true

        val inntektsmeldingId = UUID.randomUUID()
        val vedtaksperiodeId = UUID.randomUUID()
        val opprettet = LocalDateTime.now()

        testRapid.sendJson(
            inntektsmeldingId = inntektsmeldingId,
            vedtaksperiodeId = vedtaksperiodeId,
            opprettet = opprettet
        )

        verify {
            dao.lagre(match {
                it.inntektsmeldingId == inntektsmeldingId &&
                it.vedtaksperiodeId == vedtaksperiodeId &&
                it.opprettet == opprettet
            })
        }
    }

    @Test
    fun `parser ikke event med feil event_name`() {
        every { dao.lagre(any()) } returns true

        testRapid.sendJson(
            eventName = "handtert_inntektsmelding_med_skrivefeil"
        )
        verify(exactly = 0) {
            dao.lagre(any())
        }
    }

    fun TestRapid.sendJson(
        eventName: String = "håndtert_inntektsmelding",
        vedtaksperiodeId: UUID = UUID.randomUUID(),
        inntektsmeldingId: UUID = UUID.randomUUID(),
        opprettet: LocalDateTime = LocalDateTime.now()
    ) = sendTestMessage(
        """
       {
            "@event_name": "${eventName}",
            "vedtaksperiodeId": "${vedtaksperiodeId}",
            "inntektsmeldingId": "${inntektsmeldingId}",
            "opprettet": "${opprettet}"
       } 
    """
    )
}