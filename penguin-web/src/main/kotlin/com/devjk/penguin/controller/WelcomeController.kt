package com.devjk.penguin.controller;

import com.devjk.penguin.db.entity.User
import com.devjk.penguin.framework.annotation.PenguinUser
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class WelcomeController {

    @GetMapping("/")
    fun welcome(
        @PenguinUser user: User?,
        model: Model
    ): String {
        model.addAttribute("title", "정자동 펭귄마을")
        model.addAttribute("message", "또히는 일해요!")
        user?.let {
            model.addAttribute("user", it)
            model.addAttribute("isLogin", true)
        } ?: let {
            model.addAttribute("user", User(email = "guest"))
            model.addAttribute("isLogin", false)
        }
        return "index"
    }
}
