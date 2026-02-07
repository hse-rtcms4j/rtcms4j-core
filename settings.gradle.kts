pluginManagement {
    plugins {
        val jvmPluginVersion: String by settings
        val springBootVersion: String by settings
        val springDependencyManagementVersion: String by settings
        val ktlintVersion: String by settings
        val openapiGeneratorVersion: String by settings

        kotlin("jvm") version jvmPluginVersion
        kotlin("plugin.spring") version jvmPluginVersion
        id("org.springframework.boot") version springBootVersion
        id("io.spring.dependency-management") version springDependencyManagementVersion
        id("org.jlleitschuh.gradle.ktlint") version ktlintVersion
        id("org.openapi.generator") version openapiGeneratorVersion
    }

    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

include("${rootProject.name}-api")
include("${rootProject.name}-server")
