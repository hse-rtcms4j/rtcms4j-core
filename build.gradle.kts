import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    kotlin("jvm") apply false
    kotlin("plugin.spring") apply false
    id("org.springframework.boot") apply false
    id("org.jlleitschuh.gradle.ktlint") apply false
    id("org.openapi.generator") apply false
    id("io.spring.dependency-management")
    id("com.google.cloud.tools.jib") apply false
    id("maven-publish")
    id("signing")
}

subprojects {
    apply {
        plugin("org.jetbrains.kotlin.jvm")
        plugin("org.jetbrains.kotlin.plugin.spring")
        plugin("io.spring.dependency-management")
        plugin("org.jlleitschuh.gradle.ktlint")
        plugin("org.openapi.generator")
        plugin("org.springframework.boot")
        plugin("maven-publish")
        plugin("com.google.cloud.tools.jib")
        plugin("signing")
    }

    val groupId: String by project
    val versionIdNumber: String by project
    val versionIdStatus: String by project

    group = groupId
    val versionId: String = if (versionIdStatus.isEmpty()) versionIdNumber else "$versionIdNumber-$versionIdStatus"
    version = versionId

    dependencyManagement {
        imports {
            val springBootVersion: String by project
//            val springCloudVersion: String by project
            val embeddedPostgresBinariesBomVersion: String by project

            mavenBom("org.springframework.boot:spring-boot-dependencies:$springBootVersion")
//            mavenBom("org.springframework.cloud:spring-cloud-dependencies:$springCloudVersion")
            mavenBom("io.zonky.test.postgres:embedded-postgres-binaries-bom:$embeddedPostgresBinariesBomVersion")
        }

        dependencies {
            val springDocVersion: String by project
            dependency("org.springdoc:springdoc-openapi-starter-webmvc-ui:$springDocVersion")

            val embeddedPostgresVersion: String by project
            val embeddedDatabaseSpringTestVersion: String by project
            dependency("io.zonky.test:embedded-postgres:$embeddedPostgresVersion")
            dependency("io.zonky.test:embedded-database-spring-test:$embeddedDatabaseSpringTestVersion")

            val embeddedRedisVersion: String by project
            dependency("com.github.codemonstur:embedded-redis:$embeddedRedisVersion")

            val keycloakAdminClientVersion: String by project
            dependency("org.keycloak:keycloak-admin-client:$keycloakAdminClientVersion")

            val jsonSchemaValidatorVersion: String by project
            dependency("com.networknt:json-schema-validator:$jsonSchemaValidatorVersion")

            val cucumberVersion: String by project
            dependency("io.cucumber:cucumber-jvm:$cucumberVersion")
            dependency("io.cucumber:cucumber-spring:$cucumberVersion")
            dependency("io.cucumber:cucumber-junit-platform-engine:$cucumberVersion")

            val mockitoKotlin: String by project
            dependency("org.mockito.kotlin:mockito-kotlin:$mockitoKotlin")
        }
    }

    repositories {
        mavenLocal()
        mavenCentral()
    }

    publishing {
        publications {
            create<MavenPublication>("maven") {
                this.groupId = groupId
                this.artifactId = project.name
                this.version = versionId
                from(components["java"])

                pom {
                    name.set(project.name)
                    description.set("RTCMS4J Core-Api library")
                    url.set("https://github.com/hse-rtcms4j/rtcms4j-core")

                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }

                    developers {
                        developer {
                            id.set("Enzhine")
                            name.set("Onar")
                            email.set("shamaevonar@gmail.com")
                        }
                    }

                    scm {
                        connection.set("scm:git:git://github.com/hse-rtcms4j/rtcms4j-core.git")
                        developerConnection.set("scm:git:ssh://github.com/hse-rtcms4j/rtcms4j-core.git")
                        url.set("https://github.com/hse-rtcms4j/rtcms4j-core")
                    }
                }
            }
        }
        repositories {
            maven {
                name = "MavenCentral"
                url = uri("https://central.sonatype.com/api/v1/publisher")
                credentials {
                    username = System.getenv("OSSRH_USERNAME") ?: findProperty("ossrhUsername") as String?
                    password = System.getenv("OSSRH_PASSWORD") ?: findProperty("ossrhPassword") as String?
                }
            }
        }
    }

    signing {
        useInMemoryPgpKeys(
            System.getenv("GPG_KEY_ID") ?: findProperty("signing.keyId") as String?,
            System.getenv("GPG_PRIVATE_KEY") ?: findProperty("signing.privateKey") as String?,
            System.getenv("GPG_PASSPHRASE") ?: findProperty("signing.password") as String?
        )
        sign(publishing.publications["maven"])
    }
}

tasks.withType<KotlinJvmCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
        freeCompilerArgs.add("-Xjsr305=strict")
    }
}
