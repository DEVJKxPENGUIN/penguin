package com.devjk.penguin.controller

import com.devjk.penguin.utils.JwtHelper.Companion.KID
import com.devjk.penguin.utils.JwtHelper.Companion.KTY
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/.well-known")
class JwkController {

    @GetMapping("openid-configuration")
    fun openIdConfiguration(): ResponseEntity<*> {
        return ResponseEntity.ok(
            OpenIdConfigurationResponse(
                issuer = "https://auth.devjk.me",
                jwks_uri = "https://auth.devjk.me/.well-known/jwks.json",
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
                        n = "60R-06a4QY_P5TREIJ9vnLSl01ijhOEq7aD5_NYInn2JOUEOQtsrILLgTP2Uz3YjMTpl7R7DGshcH0Ec9Hb4dlT6fQYgCKyRFH3ZRd75TmjOntcf85IwP1RxOLfq3jAKt-XUsqdT5gtq1cWjmeZ-FqoanB6HpJs27bFw4qbKrkP84IvV2A9BqSXmv3C9cEUNtDvn4mrM1hFQ-NmbSPkNX0JrvrYtq4Wm75kTHqsOO0vCRBGVYBvzzY77xyOD9SNphy1TCo33KPYAvqwzcZY9i65m5BdHYkOJtYKRjY8IJB3-_N0iwyHCQRgCGcJS5q8Jr5aTSpH7K_rPSOxak3YOow",
                        e = "AQAB"
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
