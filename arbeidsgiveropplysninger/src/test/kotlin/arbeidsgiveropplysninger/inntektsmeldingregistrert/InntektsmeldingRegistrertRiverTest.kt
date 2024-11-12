package arbeidsgiveropplysninger.inntektsmeldingregistrert

import com.github.navikt.tbd_libs.rapids_and_rivers.test_support.TestRapid
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID

class InntektsmeldingRegistrertRiverTest {

    private val testRapid = TestRapid()
    private val dao = mockk<InntektsmeldingRegistrertDao>()

    init {
        InntektsmeldingRegistrertRiver(testRapid, dao)
    }

    @BeforeEach
    fun clear() {
        clearMocks(dao)
    }

    @Test
    fun `lagrer korrekte meldinger i databasen`() {
        every { dao.lagre(any()) } returns true

        val hendelseId = UUID.randomUUID()
        val dokumentId = UUID.randomUUID()
        val opprettet = LocalDateTime.now()

        testRapid.sendJson(
            hendelseId = hendelseId,
            dokumentId = dokumentId,
            opprettet = opprettet
        )

        verify {
            dao.lagre(match {
                it.hendelseId == hendelseId &&
                it.dokumentId == dokumentId &&
                it.opprettet == opprettet
            })
        }
    }

    @Test
    fun `parser ikke event med feil event_name`() {
        every { dao.lagre(any()) } returns true

        testRapid.sendJson(eventName = "søknad")
        verify(exactly = 0) {
            dao.lagre(any())
        }
    }

    fun TestRapid.sendJson(
        eventName: String = "inntektsmelding",
        hendelseId: UUID = UUID.randomUUID(),
        dokumentId: UUID = UUID.randomUUID(),
        opprettet: LocalDateTime = LocalDateTime.now()
    ) = sendTestMessage(
        """
       {
            "@event_name": "${eventName}",
            "@id": "${hendelseId}",
            "inntektsmeldingId": "${dokumentId}",
            "@opprettet": "${opprettet}"
       } 
    """
    )
}