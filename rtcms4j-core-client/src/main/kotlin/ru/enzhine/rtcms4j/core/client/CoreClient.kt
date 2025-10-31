package ru.enzhine.rtcms4j.core.client

import org.springframework.cloud.openfeign.FeignClient
import ru.enzhine.rtcms4j.core.api.CoreApi

@FeignClient(name = "rtcms4j-core-client")
interface CoreClient : CoreApi
