package com.devjk.penguin.controller

import com.devjk.penguin.PenguinTester
import com.devjk.penguin.controller.AuthController.Companion.AUTH_VALUE
import com.devjk.penguin.db.entity.User
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers


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

        println("result : ${result.contentAsString}")
    }

    @Test
    fun start() {
    }

    @Test
    fun callback() {
    }

    @Test
    fun logout() {
    }

}