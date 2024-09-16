package com.devjk.penguin.utils

import com.devjk.penguin.framework.aop.ApplicationContextProvider

class Profiles {

    companion object {
        fun isLocal(): Boolean {
            return getActiveProfile() == "local"
        }

        fun isProd(): Boolean {
            return getActiveProfile() == "prod"
        }

        fun getActiveProfile(): String {
            val env = ApplicationContextProvider.getApplicationContext()?.environment
            return env?.activeProfiles?.get(0) ?: ""
        }
    }
}