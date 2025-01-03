plugins {
    kotlin("jvm") version "1.8.21"
    `java-library`
}

group = "com.systema.kotlin"
version = "1.2-SNAPSHOT"

apply(plugin = "configure-nexus-publication")  // publish to the systema maven repository

repositories {
    mavenCentral()
    mavenLocal()
    gradlePluginPortal()
}

dependencies {
    implementation("org.slf4j:slf4j-api:2.0.13")
    testImplementation(kotlin("test"))
    testImplementation("io.kotest:kotest-assertions-core-jvm:5.8.0")
    testImplementation("io.kotest:kotest-property-jvm:5.8.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.1")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}

