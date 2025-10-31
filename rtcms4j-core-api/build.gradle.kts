import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory

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
    // DOCS: https://openapi-generator.tech/docs/generators/kotlin-spring/
    generatorName = "kotlin-spring"

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
            "exceptionHandler" to "false",
            "useSwaggerUI" to "false",
            "useResponseEntity" to "false",
            "requestMappingMode" to "none",
        ),
    )

    val mappings = extractPagedModelComponents(modelPackage.get(), file(inputSpec.get()))
    schemaMappings.putAll(
        mapOf(
            "PageMetadata" to "org.springframework.data.web.PagedModel.PageMetadata",
            "Pageable" to "org.springframework.data.domain.Pageable",
        ) + mappings,
    )

    openapiGeneratorIgnoreList = listOf("**/ApiUtil.kt")
}

tasks.runKtlintCheckOverMainSourceSet {
    enabled = false
}

tasks.compileKotlin {
    dependsOn(tasks.openApiGenerate)
}

sourceSets {
    main {
        java {
            srcDir("$projectBuildDir/generated/src/main/kotlin")
        }
    }
}

tasks.bootJar {
    enabled = false
}

tasks.jar {
    enabled = true
}
