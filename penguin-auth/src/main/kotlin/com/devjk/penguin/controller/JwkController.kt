package com.devjk.penguin.controller

import com.devjk.penguin.utils.JwtHelper.Companion.KID
import com.devjk.penguin.utils.JwtHelper.Companion.KTY
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/.well-known")
class JwkController {

    @GetMapping("openid-configuration")
    fun openIdConfiguration(): Map<String, Any> {
        return mapOf(
            "issuer" to "https://auth.devjk.me",
            "jwks_uri" to "https://auth.devjk.me/.well-known/jwks.json"
        )
    }

    @GetMapping("jwks.json")
    fun jwks(): Map<String, Any> {
        return mapOf(
            "keys" to listOf(
                mapOf(
                    "kty" to KTY,
                    "use" to "sig",
                    "kid" to KID,
                    "alg" to "RS256",
                    "n" to "60R-06a4QY_P5TREIJ9vnLSl01ijhOEq7aD5_NYInn2JOUEOQtsrILLgTP2Uz3YjMTpl7R7DGshcH0Ec9Hb4dlT6fQYgCKyRFH3ZRd75TmjOntcf85IwP1RxOLfq3jAKt-XUsqdT5gtq1cWjmeZ-FqoanB6HpJs27bFw4qbKrkP84IvV2A9BqSXmv3C9cEUNtDvn4mrM1hFQ-NmbSPkNX0JrvrYtq4Wm75kTHqsOO0vCRBGVYBvzzY77xyOD9SNphy1TCo33KPYAvqwzcZY9i65m5BdHYkOJtYKRjY8IJB3-_N0iwyHCQRgCGcJS5q8Jr5aTSpH7K_rPSOxak3YOow",
                    "e" to "AQAB"
                )
            )
        )
    }
}