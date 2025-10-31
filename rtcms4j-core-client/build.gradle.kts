dependencies {
    api(project(":rtcms4j-core-api"))

    api("org.springframework.cloud:spring-cloud-starter-openfeign")
}

tasks.bootJar {
    enabled = false
}

tasks.jar {
    enabled = true
}
