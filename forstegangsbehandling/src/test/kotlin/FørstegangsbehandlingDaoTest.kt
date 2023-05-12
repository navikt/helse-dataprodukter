import FørstegangsbehandlingTest.Companion.lagSøknad
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
import org.testcontainers.containers.output.Slf4jLogConsumer

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
    fun `hent søknader for personRef`() {
        val testSøknad = lagSøknad( 1.januar(2022), 31.januar(2022), null)
        val testSøknad2 = lagSøknad( 1.februar(2022), 28.februar(2022), null)
        val personRef = dao.lagrePerson(testSøknad.fnr, testSøknad.orgnummer)
        dao.lagreSøknad(personRef, testSøknad, true)
        dao.lagreSøknad(personRef, testSøknad2, false)
        val søknader = dao.hentSøknader(personRef)
        assertEquals(testSøknad.id, søknader[0].id) {"Wrong søknad: ${søknader[0]} should be ${testSøknad.id}"}
        assertEquals(testSøknad2.id, søknader[1].id) {"Wrong søknad: ${søknader[1]} should be ${testSøknad2.id}"}
    }
}




object PostgresContainer {
    val instance by lazy {
        PostgreSQLContainer<Nothing>("postgres:14").apply {
            withReuse(true)
            withLabel("app-navn", "forstegangssoknader")
            setCommand("postgres", "-c", "fsync=off", "-c", "log_statement=all", "-c", "wal_level=logical")
            start()
            followOutput(Slf4jLogConsumer(org.slf4j.LoggerFactory.getLogger("postgres")))
        }
    }
}

internal object TestDatasource {
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

internal val tabeller = listOf("person", "soknad")
fun resetDatabase() {
    sessionOf(migratedDb).use { session ->
        tabeller.forEach {  table ->
            session.run(queryOf("truncate table $table cascade").asExecute)
        }
    }
}

internal fun migrate(dataSource: HikariDataSource) =
    Flyway.configure()
        .dataSource(dataSource)
        .cleanDisabled(false)
        .load()
        .also { it.clean() }
        .migrate()
