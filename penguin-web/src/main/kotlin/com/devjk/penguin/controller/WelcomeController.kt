package com.devjk.penguin.controller

import com.devjk.penguin.domain.oidc.AuthUser
import com.devjk.penguin.domain.oidc.Role
import com.devjk.penguin.framework.annotation.PenguinUser
import com.devjk.penguin.service.ProjectService
import com.devjk.penguin.utils.UrlUtils
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class WelcomeController(
    private val projectService: ProjectService
) {

    @GetMapping("/")
    fun welcome(
        @PenguinUser(min = Role.GUEST) user: AuthUser,
        model: Model
    ): String {
        model.addAttribute("title", "PenuingTribe in JJD [정자동 펭귄마을]")
        model.addAttribute("message", "또히는 일해요!")

        if (user.authenticated()) {
            model.addAttribute("user", user)
        } else {
            model.addAttribute("user", AuthUser.ofGuest())
        }

        model.addAttribute("oidcs", projectService.getUserOidcProjects(user))
        model.addAttribute("loginUrl", UrlUtils.loginUrl())
        model.addAttribute("logoutUrl", UrlUtils.logoutUrl() + "?rd=" + UrlUtils.serverHome())
        model.addAttribute("serverHomeUrl", UrlUtils.serverHome())
        model.addAttribute("projectStartUrl", UrlUtils.projectStartUrl())
        return "index"
    }

    @GetMapping("/errors")
    fun error(
        @PenguinUser(min = Role.GUEST) user: AuthUser,
        message: String?,
        model: Model
    ): String {
        model.addAttribute("title", "PenuingTribe in JJD [정자동 펭귄마을]")
        model.addAttribute("message", "알송달송한 또히에요!")
        model.addAttribute("errorTitle", "오류가 발생했어요.")
        model.addAttribute("errorMessage", message ?: "알 수 없는 오류에요. 다시 시도해보아요.")

        if (user.authenticated()) {
            model.addAttribute("user", user)
        } else {
            model.addAttribute("user", AuthUser.ofGuest())
        }

        model.addAttribute("loginUrl", UrlUtils.loginUrl())
        model.addAttribute("logoutUrl", UrlUtils.logoutUrl() + "?rd=" + UrlUtils.serverHome())
        model.addAttribute("serverHomeUrl", UrlUtils.serverHome())
        return "errors"
    }
}
