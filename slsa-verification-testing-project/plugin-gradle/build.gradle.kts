group = "io.github.adamkrocz"
version = "0.0.1"

plugins {
    `java-gradle-plugin`
    `maven-publish`
}

repositories {
    mavenCentral()
}

gradlePlugin {
    plugins {
        create("slsaVerificationPlugin") {
            id = "slsa-verification-plugin"
            implementationClass = "io.github.adamkrocz.SlsaVerificationPlugin"
        }
    }
}

publishing {
    repositories {
        maven {
            url = uri("./repo")
        }
    }
}
