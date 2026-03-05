val junitJupiterVersion = "5.12.1"

plugins {
    kotlin("jvm") version "2.2.21"
}


allprojects {
    group = "no.nav.helse"
    apply(plugin = "org.jetbrains.kotlin.jvm")

    kotlin {
        jvmToolchain {
            languageVersion.set(JavaLanguageVersion.of("21"))
        }
    }

    dependencies {
        testImplementation("org.junit.jupiter:junit-jupiter:$junitJupiterVersion")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    }

    tasks {
        withType<Test> {
            useJUnitPlatform()

            val parallellDisabled = System.getenv("CI" ) == "true"
            systemProperty("junit.jupiter.execution.parallel.enabled", parallellDisabled.not().toString())
            systemProperty("junit.jupiter.execution.parallel.mode.default", "concurrent")
            systemProperty("junit.jupiter.execution.parallel.config.strategy", "fixed")
            systemProperty("junit.jupiter.execution.parallel.config.fixed.parallelism", "8")
        }
    }

    repositories {
        // Sett opp repositories basert på om vi kjører i CI eller ikke
        // Jf. https://github.com/navikt/utvikling/blob/main/docs/teknisk/Konsumere%20biblioteker%20fra%20Github%20Package%20Registry.md
        repositories {
            mavenCentral()
            if (providers.environmentVariable("GITHUB_ACTIONS").orNull == "true") {
                maven {
                    url = uri("https://maven.pkg.github.com/navikt/maven-release")
                    credentials {
                        username = "token"
                        password = providers.environmentVariable("GITHUB_TOKEN").orNull!!
                    }
                }    } else {
                maven("https://repo.adeo.no/repository/github-package-registry-navikt/")
            }
        }
    }
}
