val tbdLibsVersion = "2026.01.22-09.16-1d3f6039"
val rapidsAndRiversVersion = "2026011411051768385145.e8ebad1177b4"
val flywayVersion = "11.5.0"
val hikariCPVersion = "6.3.0"
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

    testImplementation("com.github.navikt.tbd-libs:rapids-and-rivers-test:$tbdLibsVersion")
    testImplementation("com.github.navikt.tbd-libs:postgres-testdatabaser:$tbdLibsVersion")
}

tasks {
    test {
        // Ikke kjør testene i parallell fordi V5 migrasjonen tryner når flere
        // test oppsett gjør det samtidig. Få tester, så det går fint.
        systemProperty("junit.jupiter.execution.parallel.enabled", "false")
    }

    jar {

        archiveFileName.set("app.jar")

        manifest {
            attributes["Main-Class"] = "no.nav.helse.MainKt"
            attributes["Class-Path"] = configurations.runtimeClasspath.get().joinToString(separator = " ") {
                it.name
            }
        }

        doLast {
            configurations.runtimeClasspath.get()
                .filter { it.name != "app.jar" }
                .forEach {
                    val file = File("${layout.buildDirectory.get()}/libs/${it.name}")
                    if (!file.exists()) it.copyTo(file)
                }
        }
    }
}
