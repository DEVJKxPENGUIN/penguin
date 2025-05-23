package com.devjk.penguin

import com.devjk.penguin.config.TestConfig
import com.devjk.penguin.controller.AuthController.Companion.AUTH_VALUE
import com.devjk.penguin.db.entity.User
import com.devjk.penguin.db.repository.UserRepository
import com.devjk.penguin.domain.oidc.OidcProvider
import com.devjk.penguin.domain.oidc.Role
import com.devjk.penguin.external.GoogleApiHelper
import com.devjk.penguin.utils.JwtHelper
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito.reset
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.mock.web.MockHttpSession
import org.springframework.test.web.servlet.MockMvc

@SpringBootTest
@Import(TestConfig::class)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class PenguinAuthTester {

    @Autowired
    lateinit var session: MockHttpSession

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var jwtHelper: JwtHelper

    @Autowired
    lateinit var mapper: ObjectMapper

    @Autowired
    lateinit var googleApiHelper: GoogleApiHelper

    lateinit var testUser: User

    lateinit var testSuperUser: User

    @BeforeEach
    fun setup() {
        testUser =
            createTestUser(
                "devjk_localtest",
                "devjk_localtest@penguintribe.net",
                "100828347037604660700",
                Role.NORMAL
            )
        testSuperUser =
            createTestUser("devjk_supertest", "devjk_supertest@penguintribe.net", "", Role.SUPER)
        userRepository.save(testUser)
        userRepository.save(testSuperUser)
    }

    @AfterEach
    fun tearDown() {
        userRepository.deleteAll()
        reset(googleApiHelper)
        session.clearAttributes()
    }

    fun createTestUser(nickName: String, email: String, providerId: String, role: Role): User {
        val user = userRepository.save(
            User(
                nickName = nickName,
                provider = OidcProvider.google,
                providerId = providerId,
                email = email,
                role = role,
            )
        )
        val jwt = jwtHelper.create(user.id, email, role.name, nickName)
        user.idToken = jwt
        return user
    }

    fun testLogin(user: User) {
        user.renewSession()
        session.setAttribute(AUTH_VALUE, user)
    }
}
