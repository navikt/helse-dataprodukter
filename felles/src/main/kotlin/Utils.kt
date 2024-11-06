package no.nav.helse

import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway

fun datasource(username: String, password: String, url: String) =
    HikariDataSource().apply {
        initializationFailTimeout = 5000
        this.username = username
        this.password = password
        jdbcUrl = url
        connectionTimeout = 1000L
        maximumPoolSize = 2
    }

fun migrate(dataSource: HikariDataSource) =
    Flyway.configure()
        .dataSource(dataSource)
        .load().also { it.repair() }
        .migrate()
