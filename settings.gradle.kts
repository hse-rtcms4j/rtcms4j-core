pluginManagement {
    plugins {
        val jvmPluginVersion: String by settings
        val springBootVersion: String by settings
        val springDependencyManagementVersion: String by settings
        val ktlintVersion: String by settings
        val openapiGeneratorVersion: String by settings
        val jibVersion: String by settings
        val mavenPublishVersion: String by settings

        kotlin("jvm") version jvmPluginVersion
        kotlin("plugin.spring") version jvmPluginVersion
        id("org.springframework.boot") version springBootVersion
        id("io.spring.dependency-management") version springDependencyManagementVersion
        id("org.jlleitschuh.gradle.ktlint") version ktlintVersion
        id("org.openapi.generator") version openapiGeneratorVersion
        id("com.google.cloud.tools.jib") version jibVersion
        id("com.vanniktech.maven.publish") version mavenPublishVersion
    }

    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

include("${rootProject.name}-api")
include("${rootProject.name}-server")
