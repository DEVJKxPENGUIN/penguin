package com.devjk.penguin.controller

import com.devjk.penguin.utils.JwtUtils
import com.devjk.penguin.utils.JwtUtils.Companion.KID
import com.devjk.penguin.utils.JwtUtils.Companion.KTY
import com.devjk.penguin.utils.HostUtils
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/.well-known")
class JwkController(
    private val jwtUtils: JwtUtils,
) {

    @GetMapping("openid-configuration")
    fun openIdConfiguration(): ResponseEntity<*> {
        return ResponseEntity.ok(
            OpenIdConfigurationResponse(
                issuer = HostUtils.serverAuth(),
                jwks_uri = "${HostUtils.serverAuth()}/.well-known/jwks.json",
                id_token_signing_alg_values_supported = listOf("RS256")
            )
        )
    }

    @GetMapping("jwks.json")
    fun jwks(): ResponseEntity<*> {
        return ResponseEntity.ok(
            JwksJsonResponse(
                keys = listOf(
                    Jwk(
                        kty = KTY,
                        kid = KID,
                        use = "sig",
                        alg = "RS256",
                        n = jwtUtils.getJwksN(),
                        e = jwtUtils.getJwksE()
                    )
                )
            )
        )
    }
}

data class OpenIdConfigurationResponse(
    val issuer: String,
    val jwks_uri: String,
    val id_token_signing_alg_values_supported: List<String>
)

data class JwksJsonResponse(
    val keys: List<Jwk>
)

data class Jwk(
    val kty: String,
    val kid: String,
    val use: String,
    val alg: String,
    val n: String,
    val e: String
)
