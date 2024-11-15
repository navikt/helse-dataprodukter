val rapidsAndRiversVersion = "2024111509181731658731.11009b44c672"
plugins {
    application
}

dependencies {
    api("com.github.navikt:rapids-and-rivers:$rapidsAndRiversVersion")
    api("org.flywaydb:flyway-database-postgresql:10.8.1")
    api("com.github.seratch:kotliquery:1.9.0")
    api("com.zaxxer:HikariCP:5.1.0")
    api("org.postgresql:postgresql:42.7.2")
}