package com.devjk.penguin.framework.custom

import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.stereotype.Component

@Component
class ApplicationContextProvider : ApplicationContextAware {

    companion object {
        private var context: ApplicationContext? = null
        fun getApplicationContext(): ApplicationContext? {
            return context
        }
    }

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        context = applicationContext
    }
}