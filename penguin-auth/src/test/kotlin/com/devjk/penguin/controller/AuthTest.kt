package com.devjk.penguin.controller

import com.devjk.penguin.PenguinTester
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
    @DisplayName("로그인 되어있는 유저가 /auth 요청 시 성공응답과 함께 Authorization jwt 를 받는다.")
    fun auth1() {
        testLogin(testUser)

        val result = mockMvc.perform(
            MockMvcRequestBuilders
                .get("/auth")
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.header().exists("Authorization"))
            .andReturn()

        println("mvcResult : ${result.response.getHeaders("Authorization")}")

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