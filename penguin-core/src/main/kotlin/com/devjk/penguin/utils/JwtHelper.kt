package com.devjk.penguin.utils

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.security.KeyFactory
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
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
        val privateKey = loadRsaPrivateKey()
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
        try {
            val publicKey = loadRsaPublicKey()
            val jwt = Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(idToken)

            return jwt.payload
        } catch (e: Exception) {
            return null
        }
    }

    fun loadRsaPublicKey(): RSAPublicKey {
        val key = publicKey
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replace("\\s".toRegex(), "")
        val keyFactory = KeyFactory.getInstance(KTY)
        val keySpec = X509EncodedKeySpec(Base64.getDecoder().decode(key))
        return keyFactory.generatePublic(keySpec) as RSAPublicKey
    }

    fun loadRsaPrivateKey(): RSAPrivateKey {
        val key = privateKey
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace("\\s".toRegex(), "")
        val keyFactory = KeyFactory.getInstance(KTY)
        val keySpec = PKCS8EncodedKeySpec(Base64.getDecoder().decode(key))
        return keyFactory.generatePrivate(keySpec) as RSAPrivateKey
    }

    fun getJwksN(): String {
        val publicKey = loadRsaPublicKey()
        val nBytes = publicKey.modulus.toByteArray().let {
            if (it[0] == 0.toByte()) it.drop(1).toByteArray() else it
        }
        return Base64.getUrlEncoder().withoutPadding().encodeToString(nBytes)
    }

    fun getJwksE(): String {
        val publicKey = loadRsaPublicKey()
        val eBytes = publicKey.publicExponent.toByteArray().let {
            if (it[0] == 0.toByte()) it.drop(1).toByteArray() else it
        }
        return Base64.getUrlEncoder().withoutPadding().encodeToString(eBytes)
    }
}