plugins {
    kotlin("jvm") version "1.7.10"
    application
}

group = "no.nav.helse"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("org.flywaydb:flyway-core:9.10.2")
    implementation("org.postgresql:postgresql:42.5.1")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}