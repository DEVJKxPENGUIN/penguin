package com.devjk.penguin.controller;

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class WelcomeController {

    @GetMapping("/")
    fun welcome(model: Model): String {
        model.addAttribute("title", "정자동 펭귄마을")
        model.addAttribute("message", "일한다또히")
        return "index"
    }
}
