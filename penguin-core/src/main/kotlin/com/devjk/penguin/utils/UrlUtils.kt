package com.devjk.penguin.utils

class UrlUtils {

    companion object {

        fun loginUrl(): String {
            return "${serverAuth()}/start"
        }

        fun logoutUrl(): String {
            return "${serverAuth()}/logout"
        }

        fun redirectUrl(): String {
            return "${serverAuth()}/callback"
        }

        fun serverAuth(): String {
            return if (Profiles.isLocal()) {
                "http://localhost:8082"
            } else {
                "https://auth.devjk.me"
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