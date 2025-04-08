package com.devjk.penguin.domain.oidc

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
data class ProviderUserInfo(
    var id: String,
    var email: String? = "",
) : Serializable
