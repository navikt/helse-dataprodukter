package no.nav.helse.arbeidsgiveropplysninger

import com.zaxxer.hikari.HikariDataSource
import kotliquery.queryOf
import kotliquery.sessionOf
import org.flywaydb.core.Flyway
import org.intellij.lang.annotations.Language
import org.slf4j.LoggerFactory
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.output.Slf4jLogConsumer
import javax.sql.DataSource

private object PostgresContainer {
    val instance by lazy {
        PostgreSQLContainer<Nothing>("postgres:14").apply {
            withReuse(true)
            withLabel("app-navn", "arbeidsgiveropplysninger")
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

    private val dataSource = instance.also {
        migrate(it)
        it.createTruncateFunction()
    }

    fun resetDatabase() {
        sessionOf(dataSource).use { session ->
            session.run(queryOf("DROP PUBLICATION IF EXISTS dataprodukter_arbeidsgiveropplysninger_publication").asExecute)
            session.run(queryOf("SELECT PG_DROP_REPLICATION_SLOT('dataprodukter_arbeidsgiveropplysninger_replication')").asExecute)
            session.run(queryOf("SELECT truncate_tables()").asExecute)
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


    private fun DataSource.createTruncateFunction() {
        @Language("PostgreSQL")
        val query = """
            CREATE OR REPLACE FUNCTION truncate_tables() RETURNS void AS $$
            DECLARE
            truncate_statement text;
            BEGIN
                SELECT 'TRUNCATE ' || string_agg(format('%I.%I', schemaname, tablename), ',') || ' RESTART IDENTITY CASCADE' 
                    INTO truncate_statement
                FROM pg_tables
                WHERE schemaname='public';

                EXECUTE truncate_statement;
            END;
            $$ LANGUAGE plpgsql;
        """

        sessionOf(this).use { session ->
            session.run(queryOf(query).asExecute)
        }
    }
}