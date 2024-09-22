package com.devjk.penguin.utils

class UrlUtils {

    companion object {

        fun redirectUrl(): String {
            return if (Profiles.isLocal()) {
                "http://localhost:8082/callback"
            } else {
                "https://auth.devjk.com/callback"
            }
        }

        fun serverHome(): String {
            return if (Profiles.isLocal()) {
                "http://localhost:8081"
            } else {
                "https://devjk.com"
            }
        }

    }

}