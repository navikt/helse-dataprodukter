import FørstegangssøknadTest.Companion.lagSøknad
import TestDatasource.migratedDb
import com.zaxxer.hikari.HikariDataSource
import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.helse.FørstegangsbehandlingDao
import no.nav.helse.februar
import no.nav.helse.januar
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.testcontainers.containers.PostgreSQLContainer

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class FørstegangsbehandlingDaoTest {

    private val db = migratedDb
    private val dao = FørstegangsbehandlingDao(db)


    @BeforeEach
    fun reset() = resetDatabase()


    @Test
    fun `insert førstegangssøknad`() {
        val testSøknad = lagSøknad( 1.januar(2022), 31.januar(2022), null)
        val personRef = dao.lagrePerson(testSøknad.fnr, testSøknad.orgnummer)
        val result = dao.lagreSøknad(personRef, testSøknad, true)
        assertTrue(result == 1) {"PersonOgOrgnummer ref: $result er ikke riktig"}
    }

    @Test
    fun `hent søkander for personRef`() {
        val testSøknad = lagSøknad( 1.januar(2022), 31.januar(2022), null)
        val testSøknad2 = lagSøknad( 1.februar(2022), 28.februar(2022), null)
        val personRef = dao.lagrePerson(testSøknad.fnr, testSøknad.orgnummer)
        dao.lagreSøknad(personRef, testSøknad, true)
        dao.lagreSøknad(personRef, testSøknad2, false)
        val søkander = dao.hentSøknader(personRef, testSøknad.fnr, testSøknad.orgnummer)
        assertEquals(testSøknad, søkander[0])
        assertEquals(testSøknad2, søkander[1])
    }
}




object PostgresContainer {
    val instance by lazy {
        PostgreSQLContainer<Nothing>("postgres:14").apply {
            withReuse(true)
            withLabel("app-navn", "forstegangssoknader")
            start()
        }
    }
}

object TestDatasource {
    private val instance: HikariDataSource by lazy {
        HikariDataSource().apply {
            initializationFailTimeout = 5000
            username = PostgresContainer.instance.username
            password = PostgresContainer.instance.password
            jdbcUrl = PostgresContainer.instance.jdbcUrl
            connectionTimeout = 1000L
        }
    }

    val migratedDb = instance.also { migrate(it) }
}

private val tabeller = listOf("person")
fun resetDatabase() {
    //migrate(migratedDb)
    sessionOf(migratedDb).use { session ->
        tabeller.forEach {  table ->
            session.run(queryOf("truncate table $table cascade").asExecute)
        }
    }
}

private fun migrate(dataSource: HikariDataSource) =
    Flyway.configure()
        .dataSource(dataSource)
        .cleanDisabled(false)
        .load()
        .also { it.clean() }
        .migrate()