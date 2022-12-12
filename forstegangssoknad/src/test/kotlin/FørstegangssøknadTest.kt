import no.nav.helse.Førstegangssøknad
import no.nav.helse.Søknad
import no.nav.helse.februar
import no.nav.helse.januar
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class FørstegangssøknadTest {

    @Test
    fun `Første søknad mottatt er førstegangsbehandling`() {
        val fgb = Førstegangssøknad()
        assertTrue(fgb.motta(søknad(1.januar(2022), 31.januar(2022), 31.januar(2022))))
    }

    @Test
    fun `Tilstøtende søknad er ikke førstegangsbehandling`() {
        val fgb = Førstegangssøknad()
        assertTrue(fgb.motta(søknad(1.januar(2022), 31.januar(2022), 31.januar(2022))))
        assertFalse(fgb.motta(søknad(1.februar(2022), 28.februar(2022), 28.februar(2022))))
    }

    @Test
    fun `To søknader seprarert av helg er tilstøtende`() {
        val fgb = Førstegangssøknad()
        assertTrue(fgb.motta(søknad(3.januar(2022), 7.januar(2022), 7.januar(2022))))
        assertFalse(fgb.motta(søknad(10.januar(2022), 31.januar(2022), 31.januar(2022))))
    }

    private fun søknad(fom: LocalDate, tom: LocalDate, arbeidGjenopptatt: LocalDate?) = Søknad("123", "123", fom, tom, arbeidGjenopptatt)

    @Test
    fun `Arbeid gjennopptatt avkutter søknadsperioden`() {
        val fgb = Førstegangssøknad()
        assertTrue(fgb.motta(søknad(1.januar(2022), 31.januar(2022), 30.januar(2022))))
        assertTrue(fgb.motta(søknad(1.februar(2022), 28.februar(2022), 28.februar(2022))))
    }
}
