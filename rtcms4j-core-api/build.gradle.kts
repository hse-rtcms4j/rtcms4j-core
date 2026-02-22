import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import java.time.LocalDateTime

apply {
    plugin("org.openapi.generator")
}

dependencies {
    api("org.springframework:spring-web")
    api("org.springframework:spring-context")
    api("org.springframework.data:spring-data-commons")
    api("com.fasterxml.jackson.core:jackson-annotations")
    api("jakarta.validation:jakarta.validation-api")
    api("jakarta.annotation:jakarta.annotation-api")
    api("jakarta.servlet:jakarta.servlet-api")
}

fun extractPagedModelComponents(
    dtoPath: String,
    openApiFile: File,
): Map<String, String> {
    val mapper = ObjectMapper(YAMLFactory())
    val spec = mapper.readValue(openApiFile, Map::class.java)

    val components = (spec["components"] as? Map<*, *>)?.get("schemas") as? Map<*, *>
    val mappings = mutableMapOf<String, String>()

    components?.forEach { (key, _) ->
        val componentName = key.toString()

        when {
            componentName.startsWith("PagedModel") -> {
                // Extract the inner type from PagedModelComponentName
                val innerType = componentName.removePrefix("PagedModel")
                mappings[componentName] = "org.springframework.data.web.PagedModel<$dtoPath.$innerType>"
            }
        }
    }

    return mappings
}

val projectBuildDir = layout.buildDirectory.get()

tasks.openApiGenerate {
    // DOCS: https://openapi-generator.tech/docs/generators/spring/
    generatorName = "spring"

    outputDir = "$projectBuildDir/generated"
    inputSpec = "$projectDir/src/main/resources/static/openapi/core-api.yaml"
    modelPackage = "ru.enzhine.rtcms4j.core.api.dto"
    apiPackage = "ru.enzhine.rtcms4j.core.api"

    configOptions.set(
        mapOf(
            "interfaceOnly" to "true",
            "useSpringBoot3" to "true",
            "useTags" to "true",
            "skipDefaultInterface" to "true",
            "documentationProvider" to "none",
            "useSwaggerUI" to "false",
            "useResponseEntity" to "true",
            "requestMappingMode" to "none",
            "openApiNullable" to "false",
        ),
    )

    val mappings = extractPagedModelComponents(modelPackage.get(), file(inputSpec.get()))
    schemaMappings.putAll(
        mapOf(
            "PageMetadata" to "org.springframework.data.web.PagedModel.PageMetadata",
            "Pageable" to "org.springframework.data.domain.Pageable",
        ) + mappings,
    )

    openapiGeneratorIgnoreList = listOf("**/ApiUtil.java", "**/EnumConverterConfiguration.java")
}

sourceSets {
    main {
        java {
            srcDir("$projectBuildDir/generated/src/main/java")
        }
    }
}

plugins.withId("com.vanniktech.maven.publish") {
    afterEvaluate {
        tasks
            .findByName("sourcesJar")
            ?.dependsOn("openApiGenerate")
    }
}

tasks {
    compileKotlin {
        dependsOn(openApiGenerate)
    }

    runKtlintCheckOverMainSourceSet {
        enabled = false
    }

    bootJar {
        enabled = false
    }

    jar {
        enabled = true
    }
}

mavenPublishing {
    publishToMavenCentral(automaticRelease = true)
    signAllPublications()
}

val groupId: String by rootProject

val versionIdNumber: String by rootProject
val versionIdStatus: String by rootProject
val versionId: String = if (versionIdStatus.isEmpty()) versionIdNumber else "$versionIdNumber-$versionIdStatus"

mavenPublishing {
    val rootName = rootProject.name
    val projectName = project.name
    coordinates(groupId, projectName, versionId)

    pom {
        name.set(projectName)
        description.set(rootProject.description)
        inceptionYear.set(LocalDateTime.now().year.toString())
        url.set("https://github.com/hse-rtcms4j/$rootName")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("Enzhine")
                name.set("Onar")
                url.set("https://github.com/enzhine/")
            }
        }
        scm {
            url.set("https://github.com/hse-rtcms4j/$rootName")
            connection.set("scm:git:git://github.com/hse-rtcms4j/$rootName.git")
            developerConnection.set("scm:git:ssh://git@github.com/hse-rtcms4j/$rootName.git")
        }
    }
}
