package com.devjk.penguin.framework.config

import com.devjk.penguin.framework.custom.PenguinUserArgumentResolver
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig(
    private val penguinUserArgumentResolver: PenguinUserArgumentResolver
) : WebMvcConfigurer {

    override fun addArgumentResolvers(resolvers: MutableList<org.springframework.web.method.support.HandlerMethodArgumentResolver>) {
        resolvers.add(penguinUserArgumentResolver)
    }

}