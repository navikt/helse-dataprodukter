val rapidsAndRiversVersion = "2026011411051768385145.e8ebad1177b4"
val flywayVersion = "11.5.0"
val hikariCPVersion = "7.0.2"
val postgresqlVersion = "42.7.8"

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
