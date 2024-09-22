package com.devjk.penguin.utils

class UrlUtils {

    companion object {

        fun loginUrl(): String {
            return if (Profiles.isLocal()) {
                "http://localhost:8082/start"
            } else {
                "https://auth.devjk.me/start"
            }
        }

        fun logoutUrl(): String {
            return if (Profiles.isLocal()) {
                "http://localhost:8082/logout"
            } else {
                "https://auth.devjk.me/logout"
            }
        }

        fun redirectUrl(): String {
            return if (Profiles.isLocal()) {
                "http://localhost:8082/callback"
            } else {
                "https://auth.devjk.me/callback"
            }
        }

        fun serverHome(): String {
            return if (Profiles.isLocal()) {
                "http://localhost:8081"
            } else {
                "https://devjk.me"
            }
        }

    }

}