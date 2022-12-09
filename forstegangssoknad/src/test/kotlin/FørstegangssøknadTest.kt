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
        assertTrue(fgb.handle(Søknad(1.januar(2022), 31.januar(2022), 31.januar(2022))))
    }

    @Test
    fun `Tilstøtende søknad er ikke førstegangsbehandling`() {
        val fgb = Førstegangssøknad()
        assertTrue(fgb.handle(Søknad(1.januar(2022), 31.januar(2022), 31.januar(2022))))
        assertFalse(fgb.handle(Søknad(1.februar(2022), 28.februar(2022), 28.februar(2022))))
    }
}
