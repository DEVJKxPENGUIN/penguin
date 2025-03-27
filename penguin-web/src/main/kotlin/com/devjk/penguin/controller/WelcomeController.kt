package com.devjk.penguin.controller

import com.devjk.penguin.domain.auth.AuthUser
import com.devjk.penguin.domain.auth.Role
import com.devjk.penguin.framework.annotation.PenguinUser
import com.devjk.penguin.utils.UrlUtils
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class WelcomeController {

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

        model.addAttribute("loginUrl", UrlUtils.loginUrl())
        model.addAttribute("logoutUrl", UrlUtils.logoutUrl() + "?rd=" + UrlUtils.serverHome())
        return "index"
    }
}
