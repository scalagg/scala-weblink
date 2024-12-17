import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    `maven-publish`
    kotlin("jvm") version "1.9.20"
    kotlin("kapt") version "1.9.20"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("org.ajoberstar.grgit") version "4.1.1"
}

group = "gg.scala"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    repositories.configureScalaRepository()
}

dependencies {
    compileOnly("gg.scala.spigot:server:1.1.0")
    compileOnly("gg.scala.lemon:bukkit:1.8.3")
    compileOnly("gg.scala.cloudsync:spigot:1.0.1")
    kapt("gg.scala.commons:bukkit:3.4.5")
    compileOnly("gg.scala.commons:bukkit:3.4.5")
    implementation("io.github.devrawr:honey:1.0-SNAPSHOT")
    compileOnly("gg.scala.store:spigot:0.1.8")
    compileOnly("gg.scala.basics.plugin:scala-basics-plugin:1.0.5")
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
            username = property("artifactory_user").toString()
            password = property("artifactory_password").toString()
        }
    }
}
