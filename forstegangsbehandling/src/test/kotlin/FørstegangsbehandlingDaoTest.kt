import FørstegangsbehandlingTest.Companion.lagSøknad
import com.github.navikt.tbd_libs.test_support.CleanupStrategy
import com.github.navikt.tbd_libs.test_support.DatabaseContainers
import no.nav.helse.FørstegangsbehandlingDao
import no.nav.helse.februar
import no.nav.helse.januar
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import javax.sql.DataSource

val databaseContainer = DatabaseContainers.container("dataprodukt-forstegangsbehandling", CleanupStrategy.tables("person,soknad"), walLevelLogical = true)

internal class FørstegangsbehandlingDaoTest {

    @Test
    fun `insert førstegangssøknad`() = databaseTest {
        val testSøknad = lagSøknad( 1.januar(2022), 31.januar(2022), null)
        val personRef = lagrePerson(testSøknad.fnr, testSøknad.orgnummer)
        val result = lagreSøknad(personRef, testSøknad, true)
        assertTrue(result == 1) {"Lagring av person eller søknad ble ikke gjennomført riktig"}
    }

    @Test
    fun `insert langt orgnummer`() = databaseTest {
        val testSøknad = lagSøknad(
            1.januar(2022),
            31.januar(2022),
            null,
            orgnr = "123456789123456789"
        )
        val personRef = lagrePerson(testSøknad.fnr, testSøknad.orgnummer)
        val result = lagreSøknad(personRef, testSøknad, true)
        assertTrue(result == 1) {"Lagring av person eller søknad ble ikke gjennomført riktig"}
    }

    @Test
    fun `hent søknader for personRef`() = databaseTest {
        val testSøknad = lagSøknad( 1.januar(2022), 31.januar(2022), null)
        val testSøknad2 = lagSøknad( 1.februar(2022), 28.februar(2022), null)
        val personRef = lagrePerson(testSøknad.fnr, testSøknad.orgnummer)
        lagreSøknad(personRef, testSøknad, true)
        lagreSøknad(personRef, testSøknad2, false)
        val søknader = hentSøknader(personRef)
        assertEquals(testSøknad.id, søknader[0].id) {"Wrong søknad: ${søknader[0]} should be ${testSøknad.id}"}
        assertEquals(testSøknad2.id, søknader[1].id) {"Wrong søknad: ${søknader[1]} should be ${testSøknad2.id}"}
    }
}

fun databaseTest(testblokk: FørstegangsbehandlingDao.(DataSource) -> Unit) {
    val testDataSource = databaseContainer.nyTilkobling()
    try {
        testblokk(FørstegangsbehandlingDao(testDataSource.ds), testDataSource.ds)
    } finally {
        databaseContainer.droppTilkobling(testDataSource)
    }
}
