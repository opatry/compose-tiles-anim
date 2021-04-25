import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestExceptionFormat.SHORT
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    extra.apply {
        set("kotlinVersion", "1.4.31")
        set("junitVersion", "4.13.2")
    }
    repositories {
        // TODO: remove after new build is published
        mavenLocal()
        google()
        jcenter()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }

    dependencies {
        // __LATEST_COMPOSE_RELEASE_VERSION__
        classpath("org.jetbrains.compose:compose-gradle-plugin:0.3.2")
        classpath("com.android.tools.build:gradle:7.0.0-alpha12")
        // __KOTLIN_COMPOSE_VERSION__
        classpath(kotlin("gradle-plugin", version = "1.4.31"))

        classpath("com.squareup:kotlinpoet:1.8.0")
    }
}

plugins {
    id("idea")
}

subprojects {
    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }

    tasks.withType<Test> {
        testLogging {
            events = setOf(PASSED, SKIPPED, FAILED)
            exceptionFormat = SHORT

            debug {
                exceptionFormat = FULL
            }
        }
    }
}
