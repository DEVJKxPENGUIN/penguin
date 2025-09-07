package com.devjk.penguin.controller

import com.devjk.penguin.PenguinWebTester
import com.devjk.penguin.db.entity.UserOidcProvision
import com.devjk.penguin.domain.oidc.OidcProvisionStatus
import com.devjk.penguin.framework.error.ErrorCode
import com.epages.restdocs.apispec.HeaderDescriptorWithType
import com.epages.restdocs.apispec.ResourceDocumentation.resource
import com.epages.restdocs.apispec.ResourceSnippetParameters
import com.epages.restdocs.apispec.Schema
import com.epages.restdocs.apispec.SimpleType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

class OAuth2ControllerTest : PenguinWebTester() {

    private val redirectUri = "http://localhost/callback"
    private lateinit var userToken: String

    @BeforeEach
    fun setupLogin() {
        userToken =
            jwtHelper.create(testUser.id, testUser.email!!, testUser.role.name, testUser.nickName)
    }

    @Test
    @DisplayName("[AUTH-001] 성공: 최초 정보 제공 동의")
    fun authorizeSuccessFirstTime() {
        mockMvc.perform(
            get("/oauth2/authorize")
                .header("Authorization", "Bearer $userToken")
                .param("clientId", testOidcProject.clientId)
                .param("redirectUri", redirectUri)
                .param("scope", "openid")
                .param("state", "test-state")
                .session(session)
        )
            .andExpect(status().isOk)
            .andExpect(view().name("consent"))
            .andExpect(model().attribute("projectName", testOidcProject.projectName))
            .andDo(
                document(
                    "성공",
                    resource(
                        ResourceSnippetParameters.builder()
                            .summary("사용자 정보 제공 동의 요청")
                            .description("사용자가 아직 정보 제공에 동의하지 않은 경우, 동의 페이지를 보여줘요.")
                            .queryParameters(
                                parameterWithName("clientId").description("Client ID"),
                                parameterWithName("redirectUri").description("Redirect URI"),
                                parameterWithName("scope").description("Scope"),
                                parameterWithName("state").description("State")
                            )
                            .responseSchema(Schema.schema("동의 화면 페이지"))
                            .build()
                    )
                )
            )
    }

    @Test
    @DisplayName("[AUTH-002] 성공: 이미 정보 제공에 동의한 경우")
    fun authorizeSuccessAlreadyProvided() {
        // given
        userOidcProvisionRepository.save(
            UserOidcProvision(
                userId = testUser.id,
                projectId = testOidcProject.id,
                code = "dummy-code",
                status = OidcProvisionStatus.ACTIVE
            )
        )

        // when & then
        mockMvc.perform(
            get("/oauth2/authorize")
                .header("Authorization", "Bearer $userToken")
                .param("clientId", testOidcProject.clientId)
                .param("redirectUri", redirectUri)
                .param("scope", "openid")
                .param("state", "test-state")
                .session(session)
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("http://localhost:8081/oauth2/consent/agree"))
    }

    @Test
    @DisplayName("[AUTH-003] 실패: clientId가 존재하지 않는 경우")
    fun authorizeFailInvalidClientId() {
        // when & then
        mockMvc.perform(
            get("/oauth2/authorize")
                .header("Authorization", "Bearer $userToken")
                .param("clientId", "invalid-client-id")
                .param("redirectUri", redirectUri)
                .param("scope", "openid")
                .param("state", "test-state")
                .session(session)
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    @DisplayName("[AUTH-004] 실패: redirectUri가 일치하지 않는 경우")
    fun authorizeFailInvalidRedirectUri() {
        // when & then
        mockMvc.perform(
            get("/oauth2/authorize")
                .header("Authorization", "Bearer $userToken")
                .param("clientId", testOidcProject.clientId)
                .param("redirectUri", "http://invalid-uri.com")
                .param("scope", "openid")
                .param("state", "test-state")
                .session(session)
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    @DisplayName("[AUTH-005] 실패: 필수 파라미터(clientId) 누락")
    fun authorizeFailMissingClientId() {
        // when & then
        mockMvc.perform(
            get("/oauth2/authorize")
                .header("Authorization", "Bearer $userToken")
                .param("redirectUri", redirectUri)
                .param("scope", "openid")
                .param("state", "test-state")
                .session(session)
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    @DisplayName("[AUTH-006] 실패: 로그인하지 않은 사용자의 요청")
    fun authorizeFailNotLoggedIn() {
        // when & then
        mockMvc.perform(
            get("/oauth2/authorize")
                .param("clientId", testOidcProject.clientId)
                .param("redirectUri", redirectUri)
                .param("scope", "openid")
                .param("state", "test-state")
        )
            .andExpect(status().isInternalServerError)
    }

    @Test
    @DisplayName("[AGREE-001] 성공: 신규 정보 제공 동의")
    fun consentAgreeSuccessNew() {
        // given
        val request = OAuth2AuthorizeRequest(
            clientId = testOidcProject.clientId,
            redirectUri = redirectUri,
            scope = "openid",
            state = "test-state"
        )
        session.setAttribute("authorize", request)

        // when & then
        mockMvc.perform(
            get("/oauth2/consent/agree")
                .header("Authorization", "Bearer $userToken")
                .session(session)
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrlPattern("$redirectUri?status=0&message=ok&code=*&state=test-state"))

        // then
        val provision =
            userOidcProvisionRepository.findByUserIdAndProjectId(testUser.id, testOidcProject.id)
        assert(provision != null)
    }

    @Test
    @DisplayName("[AGREE-002] 성공: 이미 정보 제공에 동의한 경우")
    fun consentAgreeSuccessAlreadyProvided() {
        // given
        userOidcProvisionRepository.save(
            UserOidcProvision(
                userId = testUser.id,
                projectId = testOidcProject.id,
                code = "dummy-code",
                status = OidcProvisionStatus.ACTIVE
            )
        )
        val request = OAuth2AuthorizeRequest(
            clientId = testOidcProject.clientId,
            redirectUri = redirectUri,
            scope = "openid",
            state = "test-state"
        )
        session.setAttribute("authorize", request)

        // when & then
        mockMvc.perform(
            get("/oauth2/consent/agree")
                .header("Authorization", "Bearer $userToken")
                .session(session)
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(
                header().string(
                    "Location",
                    "$redirectUri?status=-1&message=already%20provided"
                )
            )
    }

    @Test
    @DisplayName("[AGREE-003] 실패: 세션 정보가 없는 비정상적인 접근")
    fun consentAgreeFailNoSession() {
        // when & then
        mockMvc.perform(
            get("/oauth2/consent/agree")
                .header("Authorization", "Bearer $userToken")
                .session(session)
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    @DisplayName("[DISAGREE-001] 성공: 정보 제공 거부")
    fun consentDisagreeSuccess() {
        // given
        val request = OAuth2AuthorizeRequest(
            clientId = testOidcProject.clientId,
            redirectUri = redirectUri,
            scope = "openid",
            state = "test-state"
        )
        session.setAttribute("authorize", request)

        // when & then
        mockMvc.perform(
            get("/oauth2/consent/disagree")
                .header("Authorization", "Bearer $userToken")
                .session(session)
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(header().string("Location", "$redirectUri?status=-2&message=user%20denied"))
    }

    @Test
    @DisplayName("[DISAGREE-002] 실패: 세션 정보가 없는 비정상적인 접근")
    fun consentDisagreeFailNoSession() {
        // when & then
        mockMvc.perform(
            get("/oauth2/consent/disagree")
                .header("Authorization", "Bearer $userToken")
                .session(session)
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    @DisplayName("[TOKEN-001] 성공: 유효한 code로 토큰 발급")
    fun tokenSuccess() {
        // given
        val provision = UserOidcProvision.create(testUser.id, testOidcProject.id)
        userOidcProvisionRepository.save(provision)

        val request = TokenRequest(
            clientId = testOidcProject.clientId,
            clientSecret = testClientSecret,
            code = provision.code
        )

        // when & then
        mockMvc.perform(
            post("/oauth2/token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id_token").exists())
            .andExpect(jsonPath("$.access_token").exists())
            .andDo(
                document(
                    "성공",
                    resource(
                        ResourceSnippetParameters.builder()
                            .summary("토큰 발급")
                            .description("인증 코드를 사용하여 access_token과 id_token을 발급받아요.")
                            .requestFields(
                                fieldWithPath("client_id").description("Client ID"),
                                fieldWithPath("client_secret").description("Client Secret"),
                                fieldWithPath("code").description("Authorization Code")
                            )
                            .responseFields(
                                fieldWithPath("id_token").description("ID Token"),
                                fieldWithPath("access_token").description("Access Token"),
                                fieldWithPath("token_type").description("Token Type"),
                                fieldWithPath("expires_in").description("Expires In")
                            )
                            .build()
                    )
                )
            )
    }

    @Test
    @DisplayName("[TOKEN-002] 실패: code가 유효하지 않은 경우")
    fun tokenFailInvalidCode() {
        // given
        val request = TokenRequest(
            clientId = testOidcProject.clientId,
            clientSecret = testClientSecret,
            code = "${testUser.id}-invalid-code"
        )

        // when & then
        mockMvc.perform(
            post("/oauth2/token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    @DisplayName("[TOKEN-003] 실패: clientSecret이 일치하지 않는 경우")
    fun tokenFailInvalidClientSecret() {
        // given
        val provision = UserOidcProvision.create(testUser.id, testOidcProject.id)
        userOidcProvisionRepository.save(provision)

        val request = TokenRequest(
            clientId = testOidcProject.clientId,
            clientSecret = "invalid-secret",
            code = provision.code
        )

        // when & then
        mockMvc.perform(
            post("/oauth2/token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value(ErrorCode.INVALID_OIDC_CLIENT.value))
    }

    @Test
    @DisplayName("[TOKEN-004] 실패: 필수 파라미터(code) 누락")
    fun tokenFailMissingCode() {
        // given
        val request = mapOf(
            "client_id" to testOidcProject.clientId,
            "client_secret" to testClientSecret
        )

        // when & then
        mockMvc.perform(
            post("/oauth2/token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    @DisplayName("[TOKEN-005] 실패: 이미 사용된 code를 재사용하는 경우")
    fun tokenFailCodeAlreadyUsed() {
        // given
        val provision = UserOidcProvision.create(testUser.id, testOidcProject.id)
        userOidcProvisionRepository.save(provision)

        val request = TokenRequest(
            clientId = testOidcProject.clientId,
            clientSecret = testClientSecret,
            code = provision.code
        )

        // first call - success
        mockMvc.perform(
            post("/oauth2/token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)

        // second call - fail
        mockMvc.perform(
            post("/oauth2/token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value(ErrorCode.INVALID_REQUEST.value))
    }
}
