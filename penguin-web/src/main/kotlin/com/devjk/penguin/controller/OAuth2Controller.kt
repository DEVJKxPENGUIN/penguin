package com.devjk.penguin.controller

import com.devjk.penguin.domain.OAuth2AuthorizeStatus
import com.devjk.penguin.domain.oidc.AuthUser
import com.devjk.penguin.domain.oidc.Role
import com.devjk.penguin.framework.annotation.PenguinUser
import com.devjk.penguin.framework.error.ErrorCode
import com.devjk.penguin.framework.error.exception.BaseException
import com.devjk.penguin.service.OAuth2Service
import com.devjk.penguin.utils.UrlUtils
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpSession
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import java.io.Serializable

@Controller
@RequestMapping("/oauth2")
class OAuth2Controller(
    private val oAuth2Service: OAuth2Service,
    private val session: HttpSession
) {
    private val log = LoggerFactory.getLogger(this.javaClass)

    @GetMapping("/authorize")
    fun authorize(
        @PenguinUser(Role.NORMAL, redirectLoginPage = true) user: AuthUser,
        @BindParam @Valid request: OAuth2AuthorizeRequest,
        model: Model
    ): String {
        val oidc = oAuth2Service.getMatchedOidcProject(request)
        session.setAttribute("authorize", request)

        if (oAuth2Service.alreadyProvided(user, oidc)) {
            return "redirect:${UrlUtils.oauthConsentAgreeUrl()}"
        }

        model.addAttribute("title", "PenuingTribe in JJD [정자동 펭귄마을]")
        model.addAttribute("message", "또히는 일해요!")
        model.addAttribute("serverHomeUrl", UrlUtils.serverHome())
        model.addAttribute("agreeUrl", UrlUtils.oauthConsentAgreeUrl())
        model.addAttribute("disagreeUrl", UrlUtils.oauthConsentDisagreeUrl())
        model.addAttribute("projectName", oidc.projectName)

        return "consent"
    }

    @GetMapping("/consent/agree")
    fun consentAgree(
        @PenguinUser(Role.NORMAL) user: AuthUser,
        model: Model
    ): String {
        val request =
            session.getAttribute("authorize") as OAuth2AuthorizeRequest? ?: throw BaseException(
                ErrorCode.INVALID_REQUEST,
                "invalid access"
            )

        val oidc = oAuth2Service.getMatchedOidcProject(request)
        if (oAuth2Service.alreadyProvided(user, oidc)) {
            return oAuth2Service.createRedirectUri(request, OAuth2AuthorizeStatus.ALREADY_PROVIDED)
        }

        val code = oAuth2Service.createOidcProvide(request, user)

        return oAuth2Service.createRedirectUri(request, OAuth2AuthorizeStatus.SUCCESS, code)
    }

    @GetMapping("/consent/disagree")
    fun consentDisagree(
        @PenguinUser(Role.NORMAL) user: AuthUser,
        model: Model
    ): String {
        val request =
            session.getAttribute("authorize") as OAuth2AuthorizeRequest? ?: throw BaseException(
                ErrorCode.INVALID_REQUEST,
                "invalid access"
            )

        return oAuth2Service.createRedirectUri(request, OAuth2AuthorizeStatus.USER_DENIED)
    }

    @ResponseBody
    @PostMapping("/token")
    fun token(
        @RequestBody @Valid request: TokenRequest
    ): ResponseEntity<*> {
        try {
            val response = oAuth2Service.provideOpenIdConnect(request)
            return ResponseEntity.ok(response)
        } catch (e: BaseException) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body<Any>(
                mapOf(
                    "error" to e.errorCode.value,
                    "error_description" to e.message
                )
            )
        }
    }
}

data class OAuth2AuthorizeRequest(
    @field:NotBlank(message = "Client ID must not be blank")
    val clientId: String?,
    @field:NotBlank(message = "Redirect URI must not be blank")
    val redirectUri: String?,
    @field:NotBlank(message = "Scope must not be blank")
    val scope: String?,
    @field:NotBlank(message = "State must not be blank")
    val state: String?
) : Serializable

data class TokenRequest(
    @JsonProperty("client_id")
    @field:NotBlank(message = "Client ID must not be blank")
    val clientId: String?,
    @JsonProperty("client_secret")
    @field:NotBlank(message = "Client Secret must not be blank")
    val clientSecret: String?,
    @field:NotBlank(message = "Code must not be blank")
    val code: String?
) : Serializable {
}

data class TokenResponse(
    @JsonProperty("id_token")
    val idToken: String,
    @JsonProperty("token_type")
    val tokenType: String = "bearer",
    @JsonProperty("expires_in")
    val expiresIn: Long = 3600,
    @JsonProperty("access_token")
    val accessToken: String,
) : Serializable