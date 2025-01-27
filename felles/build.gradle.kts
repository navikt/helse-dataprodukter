val rapidsAndRiversVersion = "2025012712551737978926.de930d8e0feb"
val flywayVersion = "10.21.0"
val hikariCPVersion = "6.1.0"
val postgresqlVersion = "42.7.4"

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
