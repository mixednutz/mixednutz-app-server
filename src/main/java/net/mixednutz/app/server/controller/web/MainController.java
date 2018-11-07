package net.mixednutz.app.server.controller.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.repository.UserRepository;

@Controller
public class MainController {
	
	private static final String ROOT_TEMPLATE = "root";
	private static final String LOGIN_TEMPLATE = "login/login";
	private static final String MAIN_TEMPLATE = "main/main";
	private static final String PROFILE_TEMPLATE = "profile/profile_new";
	
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
		
		return ROOT_TEMPLATE;
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
		return LOGIN_TEMPLATE;
	}
	
	private void addNewPostForms(Model model) {
		
	}
	
	@RequestMapping(value="/main", method = RequestMethod.GET)
	public String main(
			Model model
			) {
		
		/*
		 * Progressive Web Application.
		 * 
		 * Only non-user reference data can be in the model
		 */
		addNewPostForms(model);	
				
		return MAIN_TEMPLATE;
	}
	
	@RequestMapping(value="/{username}", method = RequestMethod.GET)
	public String profile(@PathVariable String username, 
			@AuthenticationPrincipal User user, Model model) {
		
		/*
		 * Progressive Web Application.
		 * 
		 * Only non-user reference data can be in the model
		 */
		
		return PROFILE_TEMPLATE;
	}

}
