apply {
    plugin("org.springframework.boot")
    plugin("com.google.cloud.tools.jib")
}

dependencies {
    api(project(":rtcms4j-core-api"))

    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework:spring-tx")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.liquibase:liquibase-core")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.keycloak:keycloak-admin-client")
    implementation("com.networknt:json-schema-validator")
    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.cucumber:cucumber-jvm")
    testImplementation("io.cucumber:cucumber-spring")
    testImplementation("io.cucumber:cucumber-junit-platform-engine")
    testImplementation("org.junit.platform:junit-platform-suite")
    testImplementation("io.zonky.test:embedded-postgres")
    testImplementation("io.zonky.test:embedded-database-spring-test")
    testImplementation("com.github.codemonstur:embedded-redis")
    testImplementation("org.mockito:mockito-core")
    testImplementation("org.mockito.kotlin:mockito-kotlin")
}

tasks {
    bootJar {
        enabled = true
    }

    jar {
        enabled = false
    }

    publishToMavenCentral {
        enabled = false
    }

    test {
        // junit fix
        useJUnitPlatform()
        // test verbose logging
        testLogging { exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL }
        // mockito jvm arg
        jvmArgs.add("-XX:+EnableDynamicAgentLoading")
    }
}

jib {
    from {
        image = "eclipse-temurin:21-jre-alpine"
//        image = "docker://eclipse-temurin:21-jre-alpine"
    }

    to {
        image = "ghcr.io/hse-rtcms4j/${project.name.lowercase()}"
//        image = "local/${project.name.lowercase()}"
        tags =
            setOf(
                project.version.toString(),
                "latest",
            )

        auth {
            username = System.getenv("GITHUB_ACTOR") ?: ""
            password = System.getenv("GITHUB_TOKEN") ?: ""
        }
    }

    setAllowInsecureRegistries(true)
}
