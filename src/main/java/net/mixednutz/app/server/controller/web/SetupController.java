package net.mixednutz.app.server.controller.web;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.support.SessionStatus;

import net.mixednutz.app.server.entity.SiteSettings;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.manager.SiteSettingsManager;
import net.mixednutz.app.server.manager.UserService;
import net.mixednutz.app.server.repository.UserRepository;

/**
 * This controller is used to set up the administrator of the site
 * 
 * @author apfesta
 *
 */
@Controller
public class SetupController {

	private static final String ADMIN_SIGNUP_FORM_VIEW = "signup/admin_new";
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	UserService userService;
	
	@Autowired
	SiteSettingsManager siteSettingsManager;
		
	@PersistenceContext
    EntityManager entityManager;
	
//	@Autowired
//    protected AuthenticationManager authenticationManager;
	
	private static Long userCountAtStartup;
	
	public boolean isFirstTime() {
		if (userCountAtStartup==null) {
			userCountAtStartup = userRepository.count();
		}
		return userCountAtStartup==0;
	}
	
	/**
	 * The first time the site is launched.  This has no mapping because
	 * the main page will redirect here.
	 * 
	 * @return
	 */
	public String firstTime(Model model) {
		User user = new User();
		user.setUsername("admin");
		model.addAttribute(user);
		return ADMIN_SIGNUP_FORM_VIEW;
	}
	
	
	@RequestMapping(value={"/admin_signup"}, method=RequestMethod.POST)
	public String save(@Valid User user, BindingResult result, 
			@RequestParam(value="inviteKey", defaultValue="") String inviteKey,
			HttpServletRequest request,
			SessionStatus sessionStatus) {
		
		if (isFirstTime()) {
			userService.encryptPassword(user);
			user.setEnabled(true);
			user = userRepository.save(user);
			
			SiteSettings siteSettings = siteSettingsManager.createSiteSettings(user);;
			siteSettingsManager.save(siteSettings);
			
			//login(user, request);
			userCountAtStartup=null; //Reset this to refresh user count
		    return "redirect:/main";
		}
		//TODO throw exception here
		return "";
	} 
	
//	protected void login(User user, HttpServletRequest request) {
//		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
//	            user.getUsername(), user.getPasswordRaw());
//
//	    // generate session if one doesn't exist
//	    request.getSession();
//	    
//	    //clear this so that when login occurs a fresh User object is created
//	    entityManager.clear();
//	
//	    token.setDetails(new WebAuthenticationDetails(request));
//	    Authentication authenticatedUser = authenticationManager.authenticate(token);
//	
//	    SecurityContextHolder.getContext().setAuthentication(authenticatedUser);
//	}
	
}
