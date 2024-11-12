import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
val junitJupiterVersion = "5.11.3"

plugins {
    kotlin("jvm") version "2.0.21"
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
        }
    }

    repositories {
        mavenCentral()
        maven("https://jitpack.io")
    }
}
