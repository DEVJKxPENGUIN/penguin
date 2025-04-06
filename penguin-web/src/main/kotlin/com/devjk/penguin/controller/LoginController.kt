package com.devjk.penguin.controller

import com.devjk.penguin.domain.auth.AuthUser
import com.devjk.penguin.domain.auth.Role
import com.devjk.penguin.framework.annotation.PenguinUser
import com.devjk.penguin.utils.UrlUtils
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/user")
class LoginController {

    @GetMapping("/login")
    fun login(
        @PenguinUser(min = Role.GUEST) user: AuthUser,
        rd: String?,
        model: Model
    ): String {
        if (user.authenticated()) {
            return "redirect:${rd ?: UrlUtils.serverHome()}"
        }

        model.addAttribute("title", "PenuingTribe in JJD [정자동 펭귄마을]")
        model.addAttribute("message", "또히는 일해요!")
        model.addAttribute("user", AuthUser.ofGuest())
        model.addAttribute("serverHomeUrl", UrlUtils.serverHome())
        model.addAttribute("googleLoginUrl", UrlUtils.startOidcProviderUrl("google", rd))
        model.addAttribute("githubLoginUrl", UrlUtils.startOidcProviderUrl("github", rd))
        return "login"
    }

}