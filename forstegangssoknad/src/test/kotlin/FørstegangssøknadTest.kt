import no.nav.helse.Førstegangssøknad
import no.nav.helse.Søknad
import no.nav.helse.februar
import no.nav.helse.januar
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

internal class FørstegangssøknadTest {

    companion object {
        internal fun søknad(fom: LocalDate, tom: LocalDate, arbeidGjenopptatt: LocalDate?) = Søknad(
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


    @Test
    fun `Arbeid gjennopptatt avkutter søknadsperioden`() {
        val fgb = Førstegangssøknad()
        assertTrue(fgb.motta(søknad(1.januar(2022), 31.januar(2022), 30.januar(2022))))
        assertTrue(fgb.motta(søknad(1.februar(2022), 28.februar(2022), 28.februar(2022))))
    }
}
