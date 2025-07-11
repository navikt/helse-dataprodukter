val rapidsAndRiversVersion = "2025061811051750237542.df739400e55e"
val flywayVersion = "11.5.0"
val hikariCPVersion = "6.3.0"
val postgresqlVersion = "42.7.5"

plugins {
    application
}

dependencies {
    api("com.github.navikt:rapids-and-rivers:$rapidsAndRiversVersion")
    api("org.flywaydb:flyway-database-postgresql:$flywayVersion")
    api("com.github.seratch:kotliquery:1.9.0")
    api("com.zaxxer:HikariCP:$hikariCPVersion")
    api("org.postgresql:postgresql:$postgresqlVersion")
}
