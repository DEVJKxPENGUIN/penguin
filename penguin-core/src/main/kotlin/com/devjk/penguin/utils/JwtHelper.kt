package com.devjk.penguin.utils

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

@Component
class JwtHelper(
    @Value("\${jwt-private-key}")
    private val privateKey: String,
    @Value("\${jwt-public-key}")
    private val publicKey: String,
) {
    companion object {
        const val KID = "penguin-key-1"
        const val AUD = "penguintribe"
        const val KTY = "RSA"
    }

    fun create(email: String, role: String, nickname: String): String {
        val key = privateKey
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace("\\s".toRegex(), "")
        val keyFactory = KeyFactory.getInstance(KTY)
        val keySpec = PKCS8EncodedKeySpec(Base64.getDecoder().decode(key))
        val privateKey = keyFactory.generatePrivate(keySpec) as PrivateKey

        val now = Instant.now()
        return Jwts.builder()
            .subject(email)
            .header()
            .add("kid", KID)
            .and()
            .claim("nickname", nickname)
            .claim("aud", AUD)
            .claim("iss", UrlUtils.serverAuth())
            .claim("role", role)
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plus(1, ChronoUnit.HOURS)))
            .signWith(privateKey)
            .compact()
    }

    fun getClaimsWithVerify(idToken: String): Claims? {
        val key = publicKey
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replace("\\s".toRegex(), "")
        val keyFactory = KeyFactory.getInstance(KTY)
        val keySpec = X509EncodedKeySpec(Base64.getDecoder().decode(key))
        val publicKey = keyFactory.generatePublic(keySpec) as PublicKey

        try {
            val jwt = Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(idToken)

            return jwt.payload
        } catch (e: Exception) {
            return null
        }
    }

}