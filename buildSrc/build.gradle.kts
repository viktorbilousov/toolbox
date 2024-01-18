plugins {
    `kotlin-dsl`
    `kotlin-dsl-precompiled-script-plugins`
    kotlin("jvm") version "1.8.20"
}

repositories {
    mavenCentral()
}

val javaVersion = 8

// set kotlin and java toolchain jvm version
kotlin {
    jvmToolchain (javaVersion)
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.9.2")
}

tasks.test {
    useJUnitPlatform()
}


