import no.nav.helse.Førstegangsbehandling
import no.nav.helse.Søknad
import no.nav.helse.februar
import no.nav.helse.januar
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

internal class FørstegangsbehandlingTest {

    companion object {
        internal fun lagSøknad(
            fom: LocalDate,
            tom: LocalDate,
            arbeidGjenopptatt: LocalDate?,
            hendelseId: UUID = UUID.randomUUID(),
            opprettet: LocalDateTime = LocalDateTime.now(),
            fnr: String = "12345678910",
            orgnr: String = "123456789",
        ) = Søknad(
            hendelseId,
            UUID.randomUUID(),
            UUID.randomUUID(),
            fnr,
            orgnr,
            fom,
            tom,
            arbeidGjenopptatt,
            opprettet
        )
    }

    @Test
    fun `Første søknad mottatt er førstegangsbehandling`() {
        val fgb = Førstegangsbehandling()
        val søknad = lagSøknad(1.januar(2022), 31.januar(2022), 31.januar(2022))
        fgb.motta(søknad)
        assertTrue(fgb.førstegangsbehandlinger()[0] == søknad.id)
    }

    @Test
    fun `Tilstøtende søknad er ikke førstegangsbehandling`() {
        val fgb = Førstegangsbehandling()
        val søknad = lagSøknad(1.januar(2022), 31.januar(2022), 31.januar(2022))
        fgb.motta(søknad)
        fgb.motta(lagSøknad(1.februar(2022), 28.februar(2022), 28.februar(2022)))
        assertTrue(fgb.førstegangsbehandlinger().size == 1)
        assertTrue(fgb.førstegangsbehandlinger().contains(søknad.id))
    }

    @Test
    fun `To søknader seprarert av helg er tilstøtende`() {
        val fgb = Førstegangsbehandling()
        val først = lagSøknad(3.januar(2022), 7.januar(2022), 7.januar(2022))
        fgb.motta(først)
        fgb.motta(lagSøknad(10.januar(2022), 31.januar(2022), 31.januar(2022)))
        assertTrue(fgb.førstegangsbehandlinger().size == 1)
        assertTrue(fgb.førstegangsbehandlinger().contains(først.id))
    }


    @Test
    fun `Arbeid gjennopptatt avkutter søknadsperioden`() {
        val fgb = Førstegangsbehandling()
        val først = lagSøknad(1.januar(2022), 31.januar(2022), 30.januar(2022))
        val sist = lagSøknad(1.februar(2022), 28.februar(2022), 28.februar(2022))
        fgb.motta(først)
        fgb.motta(sist)
        assertTrue(fgb.førstegangsbehandlinger().containsAll(listOf(først.id, sist.id)))
    }


    @Test
    fun `sist opprettede søknad telles`() {
        val fgb = Førstegangsbehandling()
        val opprettet = LocalDateTime.of(2022, 1, 1, 1, 0)
        val søknader = listOf(
            lagSøknad(1.januar(2022), 31.januar(2022), 30.januar(2022), UUID.randomUUID(), opprettet.plusDays(1)),
            lagSøknad(1.januar(2022), 31.januar(2022), 30.januar(2022), UUID.randomUUID(), opprettet),
            lagSøknad(1.januar(2022), 31.januar(2022), 30.januar(2022), UUID.randomUUID(), opprettet)
        )
        søknader.forEach { fgb.motta(it) }
        val result = fgb.førstegangsbehandlinger()
        assertEquals(1, result.size)
        assertEquals(søknader[0].id, result.first())
    }
}
