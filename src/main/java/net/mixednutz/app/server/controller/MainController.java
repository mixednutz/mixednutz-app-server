package net.mixednutz.app.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.repository.UserRepository;

@Controller
public class MainController {
	
	@Autowired
	UserRepository userRespository;
	
	@Autowired
	SetupController setupController;
		
	@RequestMapping("/")
	public String root(Model model) {
		
		if (setupController.isFirstTime()) {
			return setupController.firstTime(model);
		}
		
		//TODO Find configured landing page from configuration
		
		return "root";
	}
	
	@RequestMapping(value="/login")
	public String login(@AuthenticationPrincipal User user, Model model) {
		if (setupController.isFirstTime()) {
			return setupController.firstTime(model);
		}
		
		if (user != null) {
		    /* The user is already logged in */
		    return "redirect:/main";
		}
		return "login/login";
	}
	

}
