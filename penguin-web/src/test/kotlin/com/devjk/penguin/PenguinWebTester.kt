package com.devjk.penguin

import com.devjk.penguin.config.TestConfig
import com.devjk.penguin.db.entity.OidcProject
import com.devjk.penguin.db.entity.User
import com.devjk.penguin.db.repository.OidcProjectRepository
import com.devjk.penguin.db.repository.UserOidcProvisionRepository
import com.devjk.penguin.db.repository.UserRepository
import com.devjk.penguin.domain.oidc.OidcProvider
import com.devjk.penguin.domain.oidc.Role
import com.devjk.penguin.utils.JwtHelper
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.mock.web.MockHttpSession
import org.springframework.restdocs.RestDocumentationContextProvider
import org.springframework.restdocs.RestDocumentationExtension
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.filter.CharacterEncodingFilter
import java.security.MessageDigest

@SpringBootTest
@Import(TestConfig::class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ExtendWith(RestDocumentationExtension::class)
class PenguinWebTester {

    @Autowired
    lateinit var session: MockHttpSession

    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var oidcProjectRepository: OidcProjectRepository

    @Autowired
    lateinit var userOidcProvisionRepository: UserOidcProvisionRepository

    @Autowired
    lateinit var jwtHelper: JwtHelper

    @Autowired
    lateinit var mapper: ObjectMapper

    lateinit var testUser: User
    lateinit var testOidcProject: OidcProject
    lateinit var testClientSecret: String

    @BeforeEach
    fun setup(
        webApplicationContext: WebApplicationContext,
        restDocumentation: RestDocumentationContextProvider
    ) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
            .addFilters<DefaultMockMvcBuilder>(CharacterEncodingFilter("UTF-8", true))
            .apply<DefaultMockMvcBuilder>(MockMvcRestDocumentation.documentationConfiguration(restDocumentation))
            .build()

        testUser = createTestUser(
            "test-user",
            "test-user@test.com",
            "google-provider-id",
            Role.NORMAL
        )
        createTestOidcProject("http://localhost/callback")
    }

    @AfterEach
    fun tearDown() {
        userOidcProvisionRepository.deleteAll()
        oidcProjectRepository.deleteAll()
        userRepository.deleteAll()
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
        return userRepository.save(user)
    }

    fun createTestOidcProject(redirectUri: String) {
        val clientId = "test-client"
        val clientSecret = "test-secret"
        val hashedSecret = MessageDigest.getInstance("SHA-256").apply {
            update(clientSecret.toByteArray())
        }.digest().joinToString("") {
            "%02x".format(it)
        }

        val oidcProject = OidcProject(
            projectName = "test-project",
            clientId = clientId,
            clientSecret = hashedSecret,
            redirectUris = redirectUri.replace("http://", ""),
            ownerId = testUser.id
        )
        this.testOidcProject = oidcProjectRepository.save(oidcProject)
        this.testClientSecret = clientSecret
    }
}
