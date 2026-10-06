import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    id("java")
    id("application")
    id("io.ktor.plugin") version "3.3.0"
    kotlin("jvm") version "2.2.20"
}

repositories {
    mavenCentral()
}

java.sourceCompatibility = JavaVersion.toVersion(26)
java.targetCompatibility = JavaVersion.toVersion(26)

group = "com.balatro"

dependencies {
    implementation(project(":perkeo"))

    compileOnly("org.jetbrains:annotations:26.0.2")
    implementation("com.fasterxml.jackson.core:jackson-core:2.18.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.18.2")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.18.2")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.18.2")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")

}

application {
    mainClass.set("com.balatro.Main")
}

tasks.withType<Test> {
    useJUnitPlatform()

    testLogging {
        events = mutableSetOf(
            TestLogEvent.FAILED,
            TestLogEvent.PASSED,
            TestLogEvent.SKIPPED,
            TestLogEvent.STANDARD_OUT,
            TestLogEvent.STANDARD_ERROR
        )
        showExceptions = true
        exceptionFormat = FULL
        showCauses = true
        showStackTraces = true
        showStandardStreams = true
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(26))
    }
}

ktor {
    fatJar {
        archiveFileName.set("balatro4j-ui.jar")
    }
}
