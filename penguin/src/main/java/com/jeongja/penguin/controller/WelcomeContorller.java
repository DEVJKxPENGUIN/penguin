package com.jeongja.penguin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WelcomeContorller {
	
	@GetMapping("/")
	public String goWelcome() {
		return "welcome";
	}

}
