package com.devjk.penguin.utils

import com.devjk.penguin.framework.aop.ApplicationContextProvider
import com.fasterxml.jackson.databind.ObjectMapper

class JsonHelper {

    companion object {

        private fun mapper(): ObjectMapper {
            return ApplicationContextProvider.getApplicationContext()
                ?.getBean(ObjectMapper::class.java)!!
        }

        fun toJson(obj: Any): String {
            return mapper().writeValueAsString(obj)
        }

        fun <T> fromJson(str: String, clazz: Class<T>): T {
            return mapper().readValue(str, clazz)
        }
    }

}