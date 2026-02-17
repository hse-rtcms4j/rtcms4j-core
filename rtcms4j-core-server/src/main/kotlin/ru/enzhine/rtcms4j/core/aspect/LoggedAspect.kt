package ru.enzhine.rtcms4j.core.aspect

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Aspect
@Component
class LoggedAspect {
    private val loggers = ConcurrentHashMap<Class<*>, Logger>()

    private fun getLogger(targetClass: Class<*>): Logger =
        loggers.computeIfAbsent(targetClass) {
            LoggerFactory.getLogger(it)
        }

    @Around("@annotation(Logged)")
    fun logMethodInvocation(joinPoint: ProceedingJoinPoint): Any? {
        val logger = getLogger(joinPoint.target.javaClass)
        val methodName = joinPoint.signature.name

        MDC.put("method", methodName)
        MDC.put("request", joinPoint.args.joinToString(", "))

        try {
            val result = joinPoint.proceed()
            when (result) {
                is ResponseEntity<*> -> logger.info("{}: {}", methodName, result.statusCode)
                else -> logger.info("{}", methodName)
            }

            MDC.put("response", result?.toString() ?: "null")
            return result
        } catch (ex: Throwable) {
            logger.info("{}: {}", methodName, ex.toString())
            MDC.put("error", ex.toString())
            throw ex
        }
    }
}
