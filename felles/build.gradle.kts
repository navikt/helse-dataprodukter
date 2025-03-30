val rapidsAndRiversVersion = "2025033014191743337188.2f9d6b08d096"
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
