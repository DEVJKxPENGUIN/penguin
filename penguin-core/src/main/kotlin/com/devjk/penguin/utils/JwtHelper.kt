package com.devjk.penguin.utils

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

@Component
class JwtHelper(
    @Value("\${jwt-key}")
    private val secretKey: String
) {

    fun create(email: String, role: String, nickname: String): String {
        val key = Keys.hmacShaKeyFor(secretKey.toByteArray())
        val now = Instant.now()
        return Jwts.builder()
            .subject(email)
            .claim("nickname", nickname)
            .claim("role", role)
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plus(1, ChronoUnit.HOURS)))
            .signWith(key)
            .compact()
    }

    fun getClaimsWithVerify(idToken: String): Claims? {
        val key = Keys.hmacShaKeyFor(secretKey.toByteArray())

        try {
            val jwt = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(idToken)

            return jwt.payload
        } catch (e: Exception) {
            return null
        }
    }

}