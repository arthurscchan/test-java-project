/*
 * This file was generated by the Gradle 'init' task.
 */

plugins {
    `java-library`
    `maven-publish`
    `signing`
    id("jarfile-hash-plugin") version "0.0.1"
}

repositories {
    mavenLocal()
    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }
}

group = "io.github.adamkorcz"
version = "0.1.16"
description = "Adams test java project"
java.sourceCompatibility = JavaVersion.VERSION_1_8

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "test-java-project"
            from(components["java"])
            artifact (System.getenv("JAVADOC_PROVENANCE")) {
                classifier = "javadoc"
                extension = ".jar.intoto.sigstore"
            }
            artifact (System.getenv("SOURCES_PROVENANCE")) {
                classifier = "sources"
                extension = ".jar.intoto.sigstore"
            }
            artifact (System.getenv("BASE_PROVENANCE")) {
                classifier = ""
                extension = ".jar.intoto.sigstore"
            }
            versionMapping {
                usage("java-api") {
                    fromResolutionOf("runtimeClasspath")
                }
                usage("java-runtime") {
                    fromResolutionResult()
                }
            }
            pom {
                name.set("test-java-project")
                description.set("Adams test java project")
                url.set("https://github.com/AdamKorcz/test-java-project")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("http://www.opensource.org/licenses/mit-license.php")
                    }
                }
                developers {
                    developer {
                        id.set("adamkrocz")
                        name.set("Adam K")
                        email.set("Adam@adalogics.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/adamkorcz/test-java-project.git")
                    developerConnection.set("scm:git:ssh://github.com:simpligility/test-java-project.git")
                    url.set("http://github.com/adamkorcz/test-java-project/tree/main")
                }
            }
        }
    }
    repositories {
        maven {
            credentials {
                username = System.getenv("MAVEN_USERNAME")
                password = System.getenv("MAVEN_PASSWORD")
            }
            name = "test-java-project"
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
        }
    }
}

signing {
    useGpgCmd()
    sign(publishing.publications["maven"])
}
