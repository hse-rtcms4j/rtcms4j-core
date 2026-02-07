package ru.enzhine.rtcms4j.core

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import ru.enzhine.rtcms4j.core.config.props.Anchor

@ConfigurationPropertiesScan(basePackageClasses = [Anchor::class])
@SpringBootApplication
class SpringApplication

fun main(args: Array<String>) {
    runApplication<SpringApplication>(*args)
}
