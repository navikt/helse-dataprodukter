import TestDatasource.migratedDb
import com.zaxxer.hikari.HikariDataSource
import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.helse.Vedtak
import no.nav.helse.VedtakFattetDao
import org.flywaydb.core.Flyway
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.output.Slf4jLogConsumer
import java.time.LocalDateTime
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class VedtakDaoTest {

    private val db = migratedDb
    private val dao = VedtakFattetDao(db)

    @BeforeEach
    fun reset() = resetDatabase()

    @Test
    fun `kan lagre vedtak`() {
        val hendelseId = UUID.randomUUID()
        val vedtaksperiodeId = UUID.randomUUID()
        val utbetalingId = UUID.randomUUID()
        val fattetTidspunkt = LocalDateTime.now()
        dao.lagre(hendelseId, vedtaksperiodeId, utbetalingId, fattetTidspunkt)
        assertVedtak(hendelseId, vedtaksperiodeId, utbetalingId, fattetTidspunkt)
    }
    @Test
    fun `kan lese vedtak`() {
        val vedtaksperiodeId = UUID.randomUUID()
        opprettVedtak(vedtaksperiodeId)
        val vedtak = dao.finnVedtakFor(vedtaksperiodeId)
        assertNotNull(vedtak)
    }

    @Test
    fun `kan lagre og lese vedtak`() {
        val hendelseId = UUID.randomUUID()
        val vedtaksperiodeId = UUID.randomUUID()
        val utbetalingId = UUID.randomUUID()
        val fattetTidspunkt = LocalDateTime.now()
        dao.lagre(hendelseId, vedtaksperiodeId, utbetalingId, fattetTidspunkt)

        val funnetVedtak = dao.finnVedtakFor(vedtaksperiodeId)
        assertEquals(Vedtak(vedtaksperiodeId, hendelseId, utbetalingId, fattetTidspunkt), funnetVedtak)
    }

    private fun assertVedtak(
        hendelseId: UUID,
        vedtaksperiodeId: UUID,
        utbetalingId: UUID?,
        fattetTidspunkt: LocalDateTime
    ) {
        @Language("PostgreSQL")
        val query = "SELECT COUNT(1) FROM vedtak_fattet WHERE vedtaksperiode_id = ? AND hendelse_id = ? AND utbetaling_id = ? AND fattet_tidspunkt = ?"
        val antall = sessionOf(db).use { session ->
            session.run(queryOf(query, vedtaksperiodeId, hendelseId, utbetalingId, fattetTidspunkt).map { it.int(1) }.asSingle)
        }

        assertEquals(1, antall)
    }

    private fun opprettVedtak(vedtaksperiodeId: UUID) {
        @Language("PostgreSQL")
        val query = "INSERT INTO vedtak_fattet(vedtaksperiode_id, hendelse_id, utbetaling_id, fattet_tidspunkt) VALUES (?, ?, ?, ?)"
        sessionOf(db).use { session ->
            session.run(queryOf(query, vedtaksperiodeId, UUID.randomUUID(), UUID.randomUUID(), LocalDateTime.now()).asExecute)
        }
    }
}

private object PostgresContainer {
    val instance by lazy {
        PostgreSQLContainer<Nothing>("postgres:14").apply {
            withReuse(true)
            withLabel("app-navn", "annulleringer")
            setCommand("postgres", "-c", "fsync=off", "-c", "log_statement=all")
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

private val tabeller = listOf("vedtak_fattet", "utbetaling", "utbetalingsversjon")
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
