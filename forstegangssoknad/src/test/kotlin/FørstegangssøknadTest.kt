import no.nav.helse.Førstegangssøknad
import no.nav.helse.Søknad
import no.nav.helse.februar
import no.nav.helse.januar
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class FørstegangssøknadTest {

    @Test
    fun `Første søknad mottatt er førstegangsbehandling`() {
        val fgb = Førstegangssøknad()
        assertTrue(fgb.motta(Søknad(1.januar(2022), 31.januar(2022), 31.januar(2022))))
    }

    @Test
    fun `Tilstøtende søknad er ikke førstegangsbehandling`() {
        val fgb = Førstegangssøknad()
        assertTrue(fgb.motta(Søknad(1.januar(2022), 31.januar(2022), 31.januar(2022))))
        assertFalse(fgb.motta(Søknad(1.februar(2022), 28.februar(2022), 28.februar(2022))))
    }

    @Test
    fun `To søknader seprarert av helg er tilstøtende`() {
        val fgb = Førstegangssøknad()
        assertTrue(fgb.motta(Søknad(3.januar(2022), 7.januar(2022), 7.januar(2022))))
        assertFalse(fgb.motta(Søknad(10.januar(2022), 31.januar(2022), 31.januar(2022))))
    }

    @Test
    fun `Arbeid gjennopptatt avkutter søknadsperioden`() {
        val fgb = Førstegangssøknad()
        assertTrue(fgb.motta(Søknad(1.januar(2022), 31.januar(2022), 30.januar(2022))))
        assertTrue(fgb.motta(Søknad(1.februar(2022), 28.februar(2022), 28.februar(2022))))
    }
}
