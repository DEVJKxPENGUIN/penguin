package com.devjk.penguin.controller

import com.devjk.penguin.domain.oidc.AuthUser
import com.devjk.penguin.domain.oidc.Role
import com.devjk.penguin.framework.annotation.PenguinUser
import com.devjk.penguin.framework.error.ErrorCode
import com.devjk.penguin.framework.error.exception.BaseException
import com.devjk.penguin.service.ProjectService
import com.devjk.penguin.utils.HostUtils
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import java.net.URI

@Controller
class ProjectController(
    private val projectService: ProjectService
) {

    @GetMapping("/project/start")
    fun startProject(
        @PenguinUser(min = Role.NORMAL, redirectLoginPage = true) user: AuthUser,
        model: Model
    ): String {

        model.addAttribute("title", "PenuingTribe in JJD [정자동 펭귄마을]")
        model.addAttribute("message", "또히와 함께 프로젝트 시작해봐요!")
        model.addAttribute("serverHomeUrl", HostUtils.serverHome())
        model.addAttribute("projectCreateUrl", HostUtils.projectCreateUrl())
        return "project-start"
    }

    @PostMapping("/project")
    fun createProject(
        @PenguinUser(min = Role.NORMAL, redirectLoginPage = true) user: AuthUser,
        request: ProjectCreateRequest,
        redirectAttributes: RedirectAttributes
    ): String {

        try {
            request.validate()

            val (oidcUser, clientSecret) = projectService.createOidcProject(
                user,
                request.projectName,
                request.redirectUrl.split(",")
            )

            redirectAttributes.addFlashAttribute("clientSecret", clientSecret)
            redirectAttributes.addFlashAttribute("isCreated", true)

            return "redirect:${HostUtils.projectUrl(oidcUser.id)}"
        } catch (e: BaseException) {
            return "redirect:${HostUtils.errorUrl(e.detailMessage)}"
        }
    }

    @GetMapping("/project/{oidcId}")
    fun showProjectByOidc(
        @PenguinUser(min = Role.NORMAL) user: AuthUser,
        @PathVariable("oidcId") oidcId: Long,
        model: Model
    ): String {
        if (!model.containsAttribute("clientSecret")) {
            model.addAttribute("clientSecret", "*********")
        }

        if (!model.containsAttribute("isCreated")) {
            model.addAttribute("isCreated", false)
        }

        val oidc = projectService.getUserOidcProject(user, oidcId)

        model.addAttribute("title", "PenuingTribe in JJD [정자동 펭귄마을]")
        model.addAttribute("message", "또히와 좋은 프로젝트 만들어봐요!")
        model.addAttribute("serverHomeUrl", HostUtils.serverHome())
        model.addAttribute("oidc", oidc)
        return "project"
    }
}

data class ProjectCreateRequest(
    val projectName: String,
    val redirectUrl: String
) {

    fun validate() {
        if (projectName.isBlank()) {
            throw BaseException(ErrorCode.INVALID_PROJECT_CREATION, "프로젝트 이름을 입력해주세요.")
        }

        if (!projectName.matches(Regex("^[a-zA-Z0-9_-]+$"))) {
            throw BaseException(
                ErrorCode.INVALID_PROJECT_CREATION,
                "띄어쓰기와 특수문자(-, _ 제외)는 사용할 수 없습니다."
            )
        }

        if (redirectUrl.isBlank()) {
            throw BaseException(ErrorCode.INVALID_PROJECT_CREATION, "Redirect URL을 입력해주세요.")
        }

        try {
            URI.create(redirectUrl)
        } catch (e: Exception) {
            throw BaseException(ErrorCode.INVALID_PROJECT_CREATION, "유효한 URL 형식이 아닙니다.")
        }
    }
}