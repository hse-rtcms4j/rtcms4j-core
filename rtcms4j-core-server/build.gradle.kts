import org.springframework.boot.gradle.tasks.bundling.BootJar

apply {
    plugin("org.springframework.boot")
}

dependencies {
//    api(project(":frame-voice-api"))

    runtimeOnly("org.springdoc:springdoc-openapi-starter-webmvc-ui")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.liquibase:liquibase-core")

    implementation("org.springframework.kafka:spring-kafka")
    implementation("org.springframework:spring-context-support")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-rest")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.liquibase:liquibase-core")

    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")

    runtimeOnly("org.postgresql:postgresql")
}

tasks.getByName<BootJar>("bootJar") {
    enabled = true
}

tasks.getByName<Jar>("jar") {
    enabled = false
}

tasks.withType<PublishToMavenRepository> {
    enabled = false
}
