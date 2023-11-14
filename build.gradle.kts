import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val jvmTarget = "17"
plugins {
    kotlin("jvm") version "1.9.10"
}


allprojects {
    group = "no.nav.helse"
    apply(plugin = "org.jetbrains.kotlin.jvm")

    java {
        sourceCompatibility = JavaVersion.toVersion(jvmTarget)
        targetCompatibility = JavaVersion.toVersion(jvmTarget)
    }

    tasks {
        withType<KotlinCompile> {
            kotlinOptions.jvmTarget = jvmTarget
        }

        named<KotlinCompile>("compileTestKotlin") {
            kotlinOptions.jvmTarget = jvmTarget
        }

        withType<Test> {
            useJUnitPlatform()
        }
    }

    repositories {
        mavenCentral()
        maven("https://jitpack.io")
    }
}
