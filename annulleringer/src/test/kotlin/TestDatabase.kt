import com.zaxxer.hikari.HikariDataSource
import kotliquery.queryOf
import kotliquery.sessionOf
import org.flywaydb.core.Flyway
import org.slf4j.LoggerFactory
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.output.Slf4jLogConsumer

private object PostgresContainer {
    val instance by lazy {
        PostgreSQLContainer<Nothing>("postgres:14").apply {
            withReuse(true)
            withLabel("app-navn", "annulleringer")
            setCommand("postgres", "-c", "fsync=off", "-c", "log_statement=all", "-c", "wal_level=logical")
            start()
            followOutput(Slf4jLogConsumer(LoggerFactory.getLogger("postgres")))
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

    private val dataSource = instance.also { migrate(it) }
    private val tabeller = listOf("vedtak_fattet", "utbetaling", "utbetalingsversjon")
    fun resetDatabase() {
        sessionOf(dataSource).use { session ->
            tabeller.forEach {  table ->
                session.run(queryOf("truncate table $table cascade").asExecute)
            }
        }
    }

    internal fun getDataSource() = dataSource

    private fun migrate(dataSource: HikariDataSource) =
        Flyway.configure()
            .dataSource(dataSource)
            .cleanDisabled(false)
            .load()
            .also { it.clean() }
            .migrate()
}