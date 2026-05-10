package ru.enzhine.rtcms4j.core

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.retry.annotation.EnableRetry
import org.springframework.scheduling.annotation.EnableScheduling
import ru.enzhine.rtcms4j.core.config.props.Anchor

@ConfigurationPropertiesScan(basePackageClasses = [Anchor::class])
@EnableRetry
@EnableScheduling
@SpringBootApplication
class SpringApplication

fun main(args: Array<String>) {
    runApplication<SpringApplication>(*args)
}
