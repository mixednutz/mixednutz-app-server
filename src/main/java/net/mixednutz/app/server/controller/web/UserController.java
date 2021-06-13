package net.mixednutz.app.server.controller.web;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.support.SessionStatus;

import net.mixednutz.app.server.controller.BaseUserController;
import net.mixednutz.app.server.entity.ResetPasswordToken;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.UserEmailAddress;
import net.mixednutz.app.server.manager.ResetPasswordTokenManager;
import net.mixednutz.app.server.repository.ResetPasswordTokenRepository;
import net.mixednutz.app.server.repository.UserEmailAddressRepository;


@Controller
public class UserController extends BaseUserController {
	
	private static final String USER_SIGNUP_FORM_VIEW = "signup/user_new";
	
	private static final String FORGOTPW_FORM_VIEW = "forgotpw/forgotpw";
	private static final String FORGOTPW_SUCCESS_VIEW = "forgotpw/resetpwEmailSent";
	
	private static final String RESETPW_FORM_VIEW = "forgotpw/resetpw";
	private static final String RESETPW_EXPIRED_VIEW = "forgotpw/resetpwExpired";
	
	public static final String RESET_PASSWORD_URL = "/resetpw";
	private static final String RESETPW_SUCCESS_VIEW = "forgotpw/resetpwSuccess";
	
	@Autowired
	protected UserEmailAddressRepository userEmailAddressRepository;
	
	@Autowired
	protected ResetPasswordTokenManager resetPasswordTokenManager;
	@Autowired
	protected ResetPasswordTokenRepository resetPasswordTokenRepository;
		
	@RequestMapping(value={"/signup"}, method=RequestMethod.GET)
	public String signupForm(Model model) {
		User user = new User();
		model.addAttribute(user);
		return USER_SIGNUP_FORM_VIEW;
	}
	
	@RequestMapping(value={"/signup"}, method=RequestMethod.POST)
	public String signup(@Valid User user, BindingResult result, 
			@RequestParam(value="inviteKey", defaultValue="") String inviteKey,
			HttpServletRequest request,
			SessionStatus sessionStatus) {
		
		user = save(user);
		
		return "redirect:/"; 
	} 
	
	@RequestMapping(value={"/forgotpw"}, method=RequestMethod.GET)
	public String forgotPasswordForm(Model model) {
		model.addAttribute(new ForgotPasswordForm());
		return FORGOTPW_FORM_VIEW;
	}
	
	@RequestMapping(value={"/forgotpw"}, method=RequestMethod.POST)
	public String forgotPassword(@Valid ForgotPasswordForm form, Errors errors) {
		ResetPasswordToken token = createResetPasswordToken(form, errors);
		
		if (!errors.hasErrors() && token!=null) {
			try {
				resetPasswordTokenManager.send(token);
			} catch (MailException e) {
				errors.reject("internal_email_error", "Server Error. Unable to send e-mail.");
			}
		}
		
		if (errors.hasErrors()) {
			return FORGOTPW_FORM_VIEW;
		}
		return FORGOTPW_SUCCESS_VIEW;
	}
	
	@RequestMapping(value=RESET_PASSWORD_URL, method=RequestMethod.GET)
	public String resetPasswordForm(@RequestParam("token") String tokenString, 
			Model model) {
		
		ResetPasswordToken token = resetPasswordTokenManager.getVerificationToken(tokenString);
		
		if (token.isExpired()) {
			return RESETPW_EXPIRED_VIEW;
		}
		model.addAttribute(token);
		model.addAttribute(token.getUser());
		
		return RESETPW_FORM_VIEW;
	}
	
	@RequestMapping(value=RESET_PASSWORD_URL, method=RequestMethod.POST)
	public String resetPassword(@Valid User form, 
			@RequestParam("token") String tokenString,
			@RequestParam("username_confirm") String username,
			BindingResult result) {
		
		ResetPasswordToken token = resetPasswordTokenManager.getVerificationToken(tokenString);
		if (token.isExpired()) {
			return RESETPW_EXPIRED_VIEW;
		}
		if (!token.getUser().getUsername().equals(username)) {
			result.reject("incorrect_username", "Incorrect username");
		}
		if (!form.getPasswordRaw().equals(form.getPasswordConfirm())) {
			result.reject("passwords_not_match", "Passwords don't match");
		}
		if (result.hasErrors()) {	
			return RESETPW_FORM_VIEW;
		}
		
		User user = userService.loadUserByUsername(username);
		user.setPasswordRaw(form.getPasswordRaw());
		
		user = encryptPassword(user);
				
		resetPasswordTokenManager.delete(token);
		
		// Cleanup unused Tokens:
		for (ResetPasswordToken otherToken: resetPasswordTokenRepository.findByUser(user)) {
			resetPasswordTokenManager.delete(otherToken);
		}
		
		return RESETPW_SUCCESS_VIEW;

	}
	
	protected ResetPasswordToken createResetPasswordToken(ForgotPasswordForm form, Errors errors) {
		if (StringUtils.isEmpty(form.getUsername()) && StringUtils.isEmpty(form.getEmail())) {
			errors.reject("email_or_username_required","You must enter either a user name or an e-mail address.");
			return null;
		}
		
		Optional<User> user = loadUser(form);
		Optional<UserEmailAddress> emailAddress = loadUserEmailAddress(form);
		if (emailAddress.isEmpty() && user.isEmpty()) {
			errors.reject("account_not_found","Your account information could not be found.");
			return null;
		}
		User actualUser = user.get();
		if (emailAddress.isPresent()) {
			actualUser = emailAddress.get().getUser();
		}
		return resetPasswordTokenManager.createVerificationToken(actualUser);
	}
	
	protected Optional<User> loadUser(ForgotPasswordForm form) {
		if (form.getUsername()!=null && !form.getUsername().equals("")) {
			return userRepository.findByUsername(form.getUsername());
		} 
		return Optional.empty();
	}
	
	protected Optional<UserEmailAddress> loadUserEmailAddress(ForgotPasswordForm form) {
		if (form.getEmail()!=null && !form.getEmail().equals("")) {
			return userEmailAddressRepository.findByEmailAddress(form.getEmail());
		} 
		return Optional.empty();
	}
	
	public class ForgotPasswordForm {
		
		private String email;
		private String username;
		
		
		public String getEmail() {
			return email;
		}
		public void setEmail(String email) {
			this.email = email;
		}
		public String getUsername() {
			return username;
		}
		public void setUsername(String username) {
			this.username = username;
		}
		
		

	}

}
