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
    repositories {
        maven {
            name = "scalaGradleRelease"
            url = uri("https://artifactory.scala.gg/gradle-release")
            credentials(PasswordCredentials::class)
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            groupId = "gg.scala"
            artifactId = "weblink"
            version = "1.0.0"
            from(components["java"])
        }
    }
}