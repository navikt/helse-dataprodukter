import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val target = "21"
plugins {
    kotlin("jvm") version "2.0.21"
}


allprojects {
    group = "no.nav.helse"
    apply(plugin = "org.jetbrains.kotlin.jvm")

    java {
        sourceCompatibility = JavaVersion.toVersion(target)
        targetCompatibility = JavaVersion.toVersion(target)
    }

    tasks {
        withType<KotlinCompile> {
            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_21)
            }
        }

        named<KotlinCompile>("compileTestKotlin") {
            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_21)
            }
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
