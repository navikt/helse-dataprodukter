import no.nav.helse.Førstegangssøknad
import no.nav.helse.Søknad
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class FørstegangssøknadTest {

    @Test
    fun `Første søknad mottatt er førstegangsbehandling`() {
        val fgb = Førstegangssøknad()
        assertTrue(fgb.handle(Søknad(1.januar(), 31.januar(), 31.januar())))
    }

    @Disabled
    @Test
    fun `Tilstøtende søknad er ikke førstegangsbehandling`() {
        val fgb = Førstegangssøknad()
        assertTrue(fgb.handle(Søknad(1.januar(), 31.januar(), 31.januar())))
        assertFalse(fgb.handle(Søknad(1.februar(), 28.februar(), 28.februar())))
    }



}

