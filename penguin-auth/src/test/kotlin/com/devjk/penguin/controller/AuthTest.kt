package com.devjk.penguin.controller

import com.devjk.penguin.PenguinAuthTester
import com.devjk.penguin.controller.AuthController.Companion.AUTH_REDIRECT
import com.devjk.penguin.controller.AuthController.Companion.AUTH_VALUE
import com.devjk.penguin.controller.AuthController.Companion.OAUTH_STATE
import com.devjk.penguin.controller.AuthController.Companion.OIDC_PROVIDER
import com.devjk.penguin.db.entity.User
import com.devjk.penguin.domain.oidc.AuthUser
import com.devjk.penguin.domain.oidc.OidcProvider
import com.devjk.penguin.domain.oidc.Role
import com.devjk.penguin.external.GoogleOpenId
import com.devjk.penguin.framework.error.ErrorCode
import com.devjk.penguin.utils.UrlUtils
import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.doReturn
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.net.URI
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime

class AuthTest : PenguinAuthTester() {

    @Test
    @DisplayName(
        """
        로그인 되어있는 유저가 /auth 요청 시 성공응답과 함께 Authorization jwt 를 받는다.
        - jwt 는 Bearer 로 시작한다.
        - jwt 는 user.idToken 과 동일하다.
    """
    )
    fun auth1() {
        testLogin(testUser)

        val result = mockMvc.perform(
            MockMvcRequestBuilders
                .get("/auth")
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()
            .response

        assertThat(result.getHeader("Authorization")).isNotBlank()
        val authorization = result.getHeader("Authorization")
        assertThat(authorization).startsWith("Bearer ")
        val jwt = authorization?.substring(7)
        assertThat(jwt).isNotBlank()
        assertThat(jwt).isEqualTo(testUser.idToken)
    }

    @Test
    @DisplayName(
        """
        로그인 되어있는 유저가 /auth 요청 시 성공응답과 함께 Authorization jwt 를 받는다.
        - 이때 유저의 세션값은 갱신된다 (lastLoginAt)
    """
    )
    fun auth2() {
        testLogin(testUser)

        val loginAt = testUser.lastLoginAt

        val result = mockMvc.perform(
            MockMvcRequestBuilders
                .get("/auth")
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()
            .response

        val sessionUser = session.getAttribute(AUTH_VALUE) as User
        assertThat(sessionUser.lastLoginAt).isAfter(loginAt)
    }

    @Test
    @DisplayName(
        """
        로그인 되어있는 NORMAL 유저가 /auth?role=SUPER 요청 시 <403> ErrorCode.NO_AUTHORIZED_ROLE 오류를 받는다.
    """
    )
    fun auth3() {
        testLogin(testUser)

        val result = mockMvc.perform(
            MockMvcRequestBuilders
                .get("/auth?role=SUPER")
        )
            .andExpect(MockMvcResultMatchers.status().is4xxClientError)
            .andExpect(MockMvcResultMatchers.status().isForbidden)
            .andReturn()
            .response

        val baseResponse = mapper.readValue<Map<String, Any>>(result.contentAsString)
        assertThat(baseResponse["code"]).isEqualTo(ErrorCode.NO_AUTHORIZED_ROLE.value.toString())
    }

    @Test
    @DisplayName(
        """
        로그인 되어있는 유저의 세션타임이 만료되면  /auth 요청 시 <401> ErrorCode.UNAUTHORIZED 오류를 받는다.
    """
    )
    fun auth4() {
        testLogin(testUser)
        testUser.lastLoginAt = LocalDateTime.now().minusSeconds(User.SESSION_TIME)
        session.setAttribute(AUTH_VALUE, testUser)

        val result = mockMvc.perform(
            MockMvcRequestBuilders
                .get("/auth")
        )
            .andExpect(MockMvcResultMatchers.status().is4xxClientError)
            .andExpect(MockMvcResultMatchers.status().isUnauthorized)
            .andReturn()
            .response

        val baseResponse = mapper.readValue<Map<String, Any>>(result.contentAsString)
        assertThat(baseResponse["code"]).isEqualTo(ErrorCode.UNAUTHORIZED.value.toString())
    }

    @Test
    @DisplayName(
        """
        로그인을 하지 않으면 /auth 요청 시 <401> ErrorCode.UNAUTHORIZED 오류를 받는다.
    """
    )
    fun auth5() {
        val result = mockMvc.perform(
            MockMvcRequestBuilders
                .get("/auth")
        )
            .andExpect(MockMvcResultMatchers.status().is4xxClientError)
            .andExpect(MockMvcResultMatchers.status().isUnauthorized)
            .andReturn()
            .response

        val baseResponse = mapper.readValue<Map<String, Any>>(result.contentAsString)
        assertThat(baseResponse["code"]).isEqualTo(ErrorCode.UNAUTHORIZED.value.toString())
    }

    @Test
    @DisplayName(
        """
        로그인을 하지 않았으나 /auth?alwaysSuccess=true 로 요청 시 <200> 과 빈 Authorized 값을 받는다.
    """
    )
    fun auth6() {
        val result = mockMvc.perform(
            MockMvcRequestBuilders
                .get("/auth?alwaysSuccess=true")
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()
            .response

        assertThat(result.getHeader("Authorization")).isNotBlank()
        val authorization = result.getHeader("Authorization")
        assertThat(authorization).startsWith("Bearer ")
        val jwt = authorization?.substring(7)
        assertThat(jwt).isBlank()
    }

    @Test
    @DisplayName(
        """
        NORMAL 유저로 로그인을 했지만 /auth?alwaysSuccess=true&role=SUPER 로 요청 시 <200> 과 빈 Authorized 값을 받는다.
    """
    )
    fun auth7() {
        testLogin(testUser)

        val result = mockMvc.perform(
            MockMvcRequestBuilders
                .get("/auth?alwaysSuccess=true&role=SUPER")
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()
            .response

        assertThat(result.getHeader("Authorization")).isNotBlank()
        val authorization = result.getHeader("Authorization")
        assertThat(authorization).startsWith("Bearer ")
        val jwt = authorization?.substring(7)
        assertThat(jwt).isBlank()
    }

    @Test
    @DisplayName(
        """
        SUPER 유저로 로그인을 하고 /auth?role=SUPER 로 요청 시 <200> 과 Authorized jwt 값을 받는다.
    """
    )
    fun auth8() {
        testUser.role = Role.SUPER
        testLogin(testUser)

        val result = mockMvc.perform(
            MockMvcRequestBuilders
                .get("/auth?role=SUPER")
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()
            .response

        assertThat(result.getHeader("Authorization")).isNotBlank()
        val authorization = result.getHeader("Authorization")
        assertThat(authorization).startsWith("Bearer ")
        val jwt = authorization?.substring(7)
        assertThat(jwt).isNotBlank()
        assertThat(jwt).isEqualTo(testUser.idToken)
    }

    @Test
    @DisplayName(
        """
        /start 호출 시 <302> 응답을 받고, Location 헤더에 Google Login URL 을 받는다.
        - 전달받은 url 의 state 값은 세션에 저장된 값과 동일하다.
    """
    )
    fun start1() {

        val result = mockMvc.perform(
            MockMvcRequestBuilders
                .get("/start")
        )
            .andExpect(MockMvcResultMatchers.status().isFound)
            .andReturn()
            .response

        assertThat(result.getHeader("Location")).isNotBlank()
        val location = result.getHeader("Location")
        assertThat(location).startsWith("https://accounts.google.com/o/oauth2/v2/auth?")

        val uri = URI(location!!)
        val queryParams = uri.query
            .split("&")
            .associate {
                val (key, value) = it.split("=")
                key to URLDecoder.decode(value, StandardCharsets.UTF_8)
            }

        assertThat(queryParams["scope"]).isEqualTo("openid email")
        assertThat(queryParams["response_type"]).isEqualTo("code")
        assertThat(queryParams["redirect_uri"]).isEqualTo("http://localhost:8082/callback")
        assertThat(queryParams["state"]).isNotBlank
        assertThat(queryParams["state"]).isEqualTo(session.getAttribute(OAUTH_STATE))
    }

    @Test
    @DisplayName(
        """
         /start 호출 시 <302> 응답을 받고, Location 헤더에 Google Login URL 을 받는다.
        - 이때 rd 값을 주면 세션에 redirect 값을 저장한다.
        """
    )
    fun start2() {
        val rd = "http://devjk-unittest-rd.com"

        val result = mockMvc.perform(
            MockMvcRequestBuilders
                .get("/start?rd=$rd")
        )
            .andExpect(MockMvcResultMatchers.status().isFound)
            .andReturn()
            .response

        assertThat(session.getAttribute(AUTH_REDIRECT) as String).isNotBlank
        assertThat(session.getAttribute(AUTH_REDIRECT) as String).isEqualTo(rd)
    }

    @Test
    @DisplayName(
        """
            /callback 시 code 와 state 값을 받아서 가입된 유저면 성공적으로 로그인을 처리한다.
        """
    )
    fun callback1() {
        val state = "test_state"
        val code = "test_code"

        // payload email : devjk_localtest@penguintribe.net
        val testIdToken =
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczovL2FjY291bnRzLmdvb2dsZS5jb20iLCJhenAiOiI0NDc4OTIzMTAyMjQtZ3FmaWZidDRkbDdjZm8yNDZubzRocnFuZmxqOWhhcTYuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJhdWQiOiI0NDc4OTIzMTAyMjQtZ3FmaWZidDRkbDdjZm8yNDZubzRocnFuZmxqOWhhcTYuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJzdWIiOiIxMDA4MjgzNDcwMzc2MDQ2NjA3MDAiLCJlbWFpbCI6ImRldmprX2xvY2FsdGVzdEBwZW5ndWludHJpYmUubmV0IiwiZW1haWxfdmVyaWZpZWQiOnRydWUsImF0X2hhc2giOiJobDEzQ2pqYjdLdU1hNzRaX3FXaG5RIiwiaWF0IjoxNzQzMTczNzQ3LCJleHAiOjE3NDMxNzczNDd9.cCgClHlKiFhL2N31XVgmtE6yhjXv-5N-1BJGEKeJaE8"

        val now = LocalDateTime.now()

        session.setAttribute(OAUTH_STATE, state)
        session.setAttribute(OIDC_PROVIDER, OidcProvider.google)
        doReturn(
            GoogleOpenId(
                "test_access_token",
                9999999999999,
                testIdToken,
                null,
                "https://www.googleapis.com/auth/userinfo.email openid",
                "Bearer"
            )
        ).`when`(googleApiHelper).verifyOAuthCode(anyString())

        val result = mockMvc.perform(
            MockMvcRequestBuilders
                .get("/callback")
                .param("state", state)
                .param("code", code)
        )
            .andExpect(MockMvcResultMatchers.status().isFound)
            .andReturn()
            .response

        // authorization 헤더를 응답하고 jwt 에 대한 claim 을 검증한다.
        assertThat(result.getHeader("Authorization")).isNotBlank
        val authorization = result.getHeader("Authorization")
        assertThat(authorization).startsWith("Bearer ")
        val jwt = authorization?.substring(7)
        assertThat(jwt).isNotBlank
        val claims = jwtHelper.getClaimsWithVerify(jwt!!)
        assertThat(claims).isNotNull
        assertThat(claims!!.subject).isEqualTo(testUser.id.toString())
        assertThat(claims["role"]).isEqualTo(testUser.role.toString())
        assertThat(claims["nickname"]).isEqualTo(testUser.nickName)

        // redirect 를 따로 넣어주지 않았을때 기본 url 을 리턴한다.
        assertThat(result.getHeader("Location")).isNotBlank
        val location = result.getHeader("Location")
        assertThat(location).isEqualTo("http://localhost:8081")

        // 유저의 이메일을 응답한다.
        val baseResponse = mapper.readValue<Map<String, Any>>(result.contentAsString)
        assertThat((baseResponse["data"] as Map<*, *>)["email"]).isEqualTo(testUser.email)

        // user 의 idtoken 이 갱신되고 세션 및 db 의 마지막 로그인 시간이 갱신된다.
        val user = userRepository.findById(testUser.id).get()
        assertThat(user.idToken).isEqualTo(jwt)
        assertThat(user.lastLoginAt).isAfter(now)
        assertThat((session.getAttribute(AUTH_VALUE) as User).lastLoginAt).isAfter(now)
    }

    @Test
    @DisplayName(
        """
            /callback 시 state 가 불일치 하면 <400> ErrorCode.INVALID_STATETOKEN 오류를 받는다.
        """
    )
    fun callback2() {
        val state = "test_state"
        val code = "test_code"

        session.setAttribute(OAUTH_STATE, state)
        session.setAttribute(OIDC_PROVIDER, OidcProvider.google)

        val result = mockMvc.perform(
            MockMvcRequestBuilders
                .get("/callback")
                .param("state", "invalid_state")
                .param("code", code)
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andReturn()
            .response

        val baseResponse = mapper.readValue<Map<String, Any>>(result.contentAsString)
        assertThat(baseResponse["code"]).isEqualTo(ErrorCode.INVALID_STATETOKEN.value.toString())
    }

    @Test
    @DisplayName(
        """
            /callback 시 oidc 인증유저가 가입되지 않은 유저면 <401> 302 로그인 화면 응답을 받는다.
        """
    )
    fun callback3() {
        val state = "test_state"
        val code = "test_code"

        val expectedLocation = "${UrlUtils.serverHome()}/user/register"

        // unregistered@penguintribe.net
        val testIdToken =
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczovL2FjY291bnRzLmdvb2dsZS5jb20iLCJhenAiOiI0NDc4OTIzMTAyMjQtZ3FmaWZidDRkbDdjZm8yNDZubzRocnFuZmxqOWhhcTYuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJhdWQiOiI0NDc4OTIzMTAyMjQtZ3FmaWZidDRkbDdjZm8yNDZubzRocnFuZmxqOWhhcTYuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJzdWIiOiIxMDA4MjgzNDcwMzc2MDQ2NjA3MDEiLCJlbWFpbCI6InVucmVnaXN0ZXJlZEBwZW5ndWludHJpYmUubmV0IiwiZW1haWxfdmVyaWZpZWQiOnRydWUsImF0X2hhc2giOiJobDEzQ2pqYjdLdU1hNzRaX3FXaG5RIiwiaWF0IjoxNzQzMTczNzQ3LCJleHAiOjE3NDMxNzczNDd9.ZjBsAOOckdqk5hettQoqj2_2etqYlp97iP4yNgs1Jmo"

        session.setAttribute(OAUTH_STATE, state)
        session.setAttribute(OIDC_PROVIDER, OidcProvider.google)

        doReturn(
            GoogleOpenId(
                "test_access_token",
                9999999999999,
                testIdToken,
                null,
                "https://www.googleapis.com/auth/userinfo.email openid",
                "Bearer"
            )
        ).`when`(googleApiHelper).verifyOAuthCode(anyString())

        val result = mockMvc.perform(
            MockMvcRequestBuilders
                .get("/callback")
                .param("state", state)
                .param("code", code)
        )
            .andExpect(MockMvcResultMatchers.status().isFound)
            .andReturn()
            .response

        assertThat(result.getHeader("Location")).isNotBlank
        val location = result.getHeader("Location")
        assertThat(location).startsWith(expectedLocation)
    }

    @Test
    @DisplayName(
        """
            /callback 시 로그인이 성공하면, /start?rd= 값으로 redirect 된다.
        """
    )
    fun callback4() {
        val state = "test_state"
        val code = "test_code"

        val expectedLocation = "http://penguintribe.net"
        session.setAttribute(AUTH_REDIRECT, expectedLocation)

        // payload email : devjk_localtest@penguintribe.net
        val testIdToken =
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczovL2FjY291bnRzLmdvb2dsZS5jb20iLCJhenAiOiI0NDc4OTIzMTAyMjQtZ3FmaWZidDRkbDdjZm8yNDZubzRocnFuZmxqOWhhcTYuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJhdWQiOiI0NDc4OTIzMTAyMjQtZ3FmaWZidDRkbDdjZm8yNDZubzRocnFuZmxqOWhhcTYuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJzdWIiOiIxMDA4MjgzNDcwMzc2MDQ2NjA3MDAiLCJlbWFpbCI6ImRldmprX2xvY2FsdGVzdEBwZW5ndWludHJpYmUubmV0IiwiZW1haWxfdmVyaWZpZWQiOnRydWUsImF0X2hhc2giOiJobDEzQ2pqYjdLdU1hNzRaX3FXaG5RIiwiaWF0IjoxNzQzMTczNzQ3LCJleHAiOjE3NDMxNzczNDd9.cCgClHlKiFhL2N31XVgmtE6yhjXv-5N-1BJGEKeJaE8"

        session.setAttribute(OAUTH_STATE, state)
        session.setAttribute(OIDC_PROVIDER, OidcProvider.google)
        doReturn(
            GoogleOpenId(
                "test_access_token",
                9999999999999,
                testIdToken,
                null,
                "https://www.googleapis.com/auth/userinfo.email openid",
                "Bearer"
            )
        ).`when`(googleApiHelper).verifyOAuthCode(anyString())

        val result = mockMvc.perform(
            MockMvcRequestBuilders
                .get("/callback")
                .param("state", state)
                .param("code", code)
        )
            .andExpect(MockMvcResultMatchers.status().isFound)
            .andReturn()
            .response

        // redirect 를 따로 넣어주지 않았을때 기본 url 을 리턴한다.
        assertThat(result.getHeader("Location")).isNotBlank
        val location = result.getHeader("Location")
        assertThat(location).isEqualTo(expectedLocation)
    }

    @Test
    @DisplayName(
        """
            NORMAL 로그인 + @PenguinUser 통과테스트
            - GUEST : O
            - NORMAL : O
            - SUPER : X
        """
    )
    fun authAdvice1() {
        testLogin(testUser)

        val authResult = mockMvc.perform(
            MockMvcRequestBuilders
                .get("/auth")
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()
            .response

        var result = mockMvc.perform(
            MockMvcRequestBuilders
                .get("/test/auth/advice/guest")
                .header("Authorization", authResult.getHeader("Authorization"))
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()
            .response

        var baseResponse = mapper.readValue<Map<String, Any>>(result.contentAsString)
        assertThat((baseResponse["data"] as Map<*, *>)["email"]).isEqualTo(testUser.email)
        assertThat((baseResponse["data"] as Map<*, *>)["role"]).isEqualTo(testUser.role.toString())
        assertThat((baseResponse["data"] as Map<*, *>)["nickname"]).isEqualTo(testUser.nickName)

        result = mockMvc.perform(
            MockMvcRequestBuilders
                .get("/test/auth/advice/normal")
                .header("Authorization", authResult.getHeader("Authorization"))
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()
            .response

        baseResponse = mapper.readValue<Map<String, Any>>(result.contentAsString)
        assertThat((baseResponse["data"] as Map<*, *>)["email"]).isEqualTo(testUser.email)
        assertThat((baseResponse["data"] as Map<*, *>)["role"]).isEqualTo(testUser.role.toString())
        assertThat((baseResponse["data"] as Map<*, *>)["nickname"]).isEqualTo(testUser.nickName)

        result = mockMvc.perform(
            MockMvcRequestBuilders
                .get("/test/auth/advice/super")
                .header("Authorization", authResult.getHeader("Authorization"))
        )
            .andExpect(MockMvcResultMatchers.status().isForbidden)
            .andReturn()
            .response

        baseResponse = mapper.readValue<Map<String, Any>>(result.contentAsString)
        assertThat(baseResponse["code"]).isEqualTo(ErrorCode.NO_AUTHORIZED_ROLE.value.toString())
    }

    @Test
    @DisplayName(
        """
            비로그인 상태 + @PenguinUser 통과테스트
            - GUEST : O
            - NORMAL : X
            - SUPER : X
        """
    )
    fun authAdvice2() {

        val authResult = mockMvc.perform(
            MockMvcRequestBuilders
                .get("/auth?alwaysSuccess=true")
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()
            .response

        var result = mockMvc.perform(
            MockMvcRequestBuilders
                .get("/test/auth/advice/guest")
                .header("Authorization", authResult.getHeader("Authorization"))
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()
            .response

        val guest = AuthUser.ofGuest()
        var baseResponse = mapper.readValue<Map<String, Any>>(result.contentAsString)
        assertThat((baseResponse["data"] as Map<*, *>)["email"]).isEqualTo(guest.email)
        assertThat((baseResponse["data"] as Map<*, *>)["role"]).isEqualTo(guest.role.toString())
        assertThat((baseResponse["data"] as Map<*, *>)["nickname"]).isEqualTo(guest.nickname)

        result = mockMvc.perform(
            MockMvcRequestBuilders
                .get("/test/auth/advice/normal")
                .header("Authorization", authResult.getHeader("Authorization"))
        )
            .andExpect(MockMvcResultMatchers.status().isForbidden)
            .andReturn()
            .response

        baseResponse = mapper.readValue<Map<String, Any>>(result.contentAsString)
        assertThat(baseResponse["code"]).isEqualTo(ErrorCode.NO_AUTHORIZED_ROLE.value.toString())

        result = mockMvc.perform(
            MockMvcRequestBuilders
                .get("/test/auth/advice/super")
                .header("Authorization", authResult.getHeader("Authorization"))
        )
            .andExpect(MockMvcResultMatchers.status().isForbidden)
            .andReturn()
            .response

        baseResponse = mapper.readValue<Map<String, Any>>(result.contentAsString)
        assertThat(baseResponse["code"]).isEqualTo(ErrorCode.NO_AUTHORIZED_ROLE.value.toString())
    }

    @Test
    @DisplayName(
        """
            SUPER 로그인 상태 + @PenguinUser 통과테스트
            - GUEST : O
            - NORMAL : O
            - SUPER : O
        """
    )
    fun authAdvice3() {
        testLogin(testSuperUser)

        val authResult = mockMvc.perform(
            MockMvcRequestBuilders
                .get("/auth")
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()
            .response

        var result = mockMvc.perform(
            MockMvcRequestBuilders
                .get("/test/auth/advice/guest")
                .header("Authorization", authResult.getHeader("Authorization"))
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()
            .response

        var baseResponse = mapper.readValue<Map<String, Any>>(result.contentAsString)
        assertThat((baseResponse["data"] as Map<*, *>)["email"]).isEqualTo(testSuperUser.email)
        assertThat((baseResponse["data"] as Map<*, *>)["role"]).isEqualTo(testSuperUser.role.toString())
        assertThat((baseResponse["data"] as Map<*, *>)["nickname"]).isEqualTo(testSuperUser.nickName)

        result = mockMvc.perform(
            MockMvcRequestBuilders
                .get("/test/auth/advice/normal")
                .header("Authorization", authResult.getHeader("Authorization"))
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()
            .response

        baseResponse = mapper.readValue<Map<String, Any>>(result.contentAsString)
        assertThat((baseResponse["data"] as Map<*, *>)["email"]).isEqualTo(testSuperUser.email)
        assertThat((baseResponse["data"] as Map<*, *>)["role"]).isEqualTo(testSuperUser.role.toString())
        assertThat((baseResponse["data"] as Map<*, *>)["nickname"]).isEqualTo(testSuperUser.nickName)

        result = mockMvc.perform(
            MockMvcRequestBuilders
                .get("/test/auth/advice/super")
                .header("Authorization", authResult.getHeader("Authorization"))
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()
            .response

        baseResponse = mapper.readValue<Map<String, Any>>(result.contentAsString)
        assertThat((baseResponse["data"] as Map<*, *>)["email"]).isEqualTo(testSuperUser.email)
        assertThat((baseResponse["data"] as Map<*, *>)["role"]).isEqualTo(testSuperUser.role.toString())
        assertThat((baseResponse["data"] as Map<*, *>)["nickname"]).isEqualTo(testSuperUser.nickName)
    }

}