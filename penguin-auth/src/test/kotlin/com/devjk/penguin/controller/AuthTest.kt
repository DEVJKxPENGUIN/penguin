package com.devjk.penguin.controller

import com.devjk.penguin.PenguinTester
import com.devjk.penguin.controller.AuthController.Companion.AUTH_VALUE
import com.devjk.penguin.db.entity.User
import com.devjk.penguin.domain.auth.Role
import com.devjk.penguin.framework.error.ErrorCode
import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.time.LocalDateTime


/**
 *
 *  todo -> 이거 테스트 코드 작성해야함.
 *  1. 로그인 시나리오 작성한다.
 *  2. 내부 도메인 / 외부도메인
 *  3. 권한 별 작동 테스트케이스 시나리오 먼저 정리
 *  4. 로컬에서 테스트
 *  5. DB 연결방안 찾을 것.
 *. 6. 리얼에서 테스트
 *
 */
class AuthTest : PenguinTester() {

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

}