plugins {
    application
}

dependencies {
    api("com.github.navikt:rapids-and-rivers:2023101613431697456627.0cdd93eb696f")
    api("org.flywaydb:flyway-database-postgresql:10.8.1")
    api("com.github.seratch:kotliquery:1.9.0")
    api("com.zaxxer:HikariCP:5.1.0")
    api("org.postgresql:postgresql:42.7.2")
}