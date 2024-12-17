plugins {
    kotlin("jvm") version "2.0.21"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    `maven-publish`
}

group = "gg.scala"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        name = "scalaGradleRelease"
        url = uri("https://artifactory.scala.gg/gradle-release")
    }
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

publishing {
    repositories.configureScalaRepository(
        dev = version.toString().endsWith("-dev")
    )

    publications {
        register(
            name = "mavenJava",
            type = MavenPublication::class,
            configurationAction = shadow::component
        )
    }
}

tasks.getByName("build").dependsOn(
    "shadowJar",
    "publishMavenJavaPublicationToScalaRepository"
)

fun RepositoryHandler.configureScalaRepository(dev: Boolean = false) {
    maven("https://artifactory.scala.gg/gradle-release") {
        name = "scalaGradleRelease"
        credentials {
            username = "luna"
            password = "boPSPzO08WyBhNdoVY0KoR327XieVHr480v0Oip/FkXCrm4pk5dHS/SV5tPNX1FB"
        }
    }
}
