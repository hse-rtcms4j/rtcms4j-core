apply {
    plugin("org.springframework.boot")
}

dependencies {
    // Main purposed
    api(project(":rtcms4j-core-api"))

    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
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

    // Test purposed
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

    withType<PublishToMavenRepository> {
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
