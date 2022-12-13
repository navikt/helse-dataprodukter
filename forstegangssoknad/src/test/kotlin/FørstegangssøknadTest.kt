import no.nav.helse.Førstegangssøknad
import no.nav.helse.Søknad
import no.nav.helse.februar
import no.nav.helse.januar
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

internal class FørstegangssøknadTest {

    companion object {
        internal fun lagSøknad(fom: LocalDate, tom: LocalDate, arbeidGjenopptatt: LocalDate?) = Søknad(
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            "12345678910",
            "123456789",
            fom,
            tom,
            arbeidGjenopptatt,
            LocalDateTime.now()
        )
    }

    @Test
    fun `Første søknad mottatt er førstegangsbehandling`() {
        val fgb = Førstegangssøknad()
        val søknad = lagSøknad(1.januar(2022), 31.januar(2022), 31.januar(2022))
        fgb.motta(søknad)
        assertTrue(fgb.førstegangsbehandlinger()[0] == søknad.id)
    }

    @Test
    fun `Tilstøtende søknad er ikke førstegangsbehandling`() {
        val fgb = Førstegangssøknad()
        val søknad = lagSøknad(1.januar(2022), 31.januar(2022), 31.januar(2022))
        fgb.motta(søknad)
        fgb.motta(lagSøknad(1.februar(2022), 28.februar(2022), 28.februar(2022)))
        assertTrue(fgb.førstegangsbehandlinger().size == 1)
        assertTrue(fgb.førstegangsbehandlinger().contains(søknad.id))
    }

    @Test
    fun `To søknader seprarert av helg er tilstøtende`() {
        val fgb = Førstegangssøknad()
        val først = lagSøknad(3.januar(2022), 7.januar(2022), 7.januar(2022))
        fgb.motta(først)
        fgb.motta(lagSøknad(10.januar(2022), 31.januar(2022), 31.januar(2022)))
        assertTrue(fgb.førstegangsbehandlinger().size == 1)
        assertTrue(fgb.førstegangsbehandlinger().contains(først.id))
    }


    @Test
    fun `Arbeid gjennopptatt avkutter søknadsperioden`() {
        val fgb = Førstegangssøknad()
        val først = lagSøknad(1.januar(2022), 31.januar(2022), 30.januar(2022))
        val sist = lagSøknad(1.februar(2022), 28.februar(2022), 28.februar(2022))
        fgb.motta(først)
        fgb.motta(sist)
        assertTrue(fgb.førstegangsbehandlinger().containsAll(listOf(først.id, sist.id)))
    }
}
