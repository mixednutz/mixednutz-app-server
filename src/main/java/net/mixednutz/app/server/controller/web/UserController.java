package net.mixednutz.app.server.controller.web;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.support.SessionStatus;

import net.mixednutz.app.server.controller.BaseUserController;
import net.mixednutz.app.server.entity.User;

@Controller
public class UserController extends BaseUserController {
	
	private static final String USER_SIGNUP_FORM_VIEW = "signup/user_new";
		
	@RequestMapping(value={"/signup"}, method=RequestMethod.GET)
	public String signup(Model model) {
		User user = new User();
		model.addAttribute(user);
		return USER_SIGNUP_FORM_VIEW;
	}
	
	@RequestMapping(value={"/signup"}, method=RequestMethod.POST)
	public String saveNew(@Valid User user, BindingResult result, 
			@RequestParam(value="inviteKey", defaultValue="") String inviteKey,
			HttpServletRequest request,
			SessionStatus sessionStatus) {
		
		user = save(user);
		
		return "redirect:/main"; 
	} 

}
