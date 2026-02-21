import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    id("java")
    id("application")
    id("maven-publish")
}

java.sourceCompatibility = JavaVersion.VERSION_21
java.targetCompatibility = JavaVersion.VERSION_21

group = "com.balatro"
version = "2.0.1"

repositories {
    mavenCentral()
}


dependencies {
    compileOnly("org.jetbrains:annotations:26.0.2")
    implementation("com.fasterxml.jackson.core:jackson-core:2.18.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.18.2")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.18.2")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.18.2")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

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

publishing {
    publications.create<MavenPublication>("maven") {
        groupId = "com.balatro"
        artifactId = "balatro4j"
        version = "2.0.1"

        from(components["java"])
    }

    repositories {
        maven {
            name = "Github"
            url = uri("https://maven.pkg.github.com/alex-cova/balatro4j")
            credentials {
                username = System.getenv("MAVEN_USER")
                password = System.getenv("MAVEN_SECRET")
            }
        }
    }
}