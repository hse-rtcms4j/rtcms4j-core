dependencies {
    api(project(":rtcms4j-core-client"))
}

tasks.bootJar {
    enabled = false
}

tasks.jar {
    enabled = true
}
