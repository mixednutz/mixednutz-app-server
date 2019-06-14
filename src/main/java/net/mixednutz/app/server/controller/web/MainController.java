package net.mixednutz.app.server.controller.web;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import net.mixednutz.api.model.INetworkInfoSmall;
import net.mixednutz.app.server.controller.exception.UserNotFoundException;
import net.mixednutz.app.server.entity.ExternalCredentials.ExternalAccountCredentials;
import net.mixednutz.app.server.entity.SiteSettings;
import net.mixednutz.app.server.entity.SiteSettings.Page;
import net.mixednutz.app.server.entity.post.journal.Journal;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.manager.ExternalFeedManager;
import net.mixednutz.app.server.manager.SiteSettingsManager;
import net.mixednutz.app.server.repository.UserRepository;


@Controller
public class MainController {
	
	private static final String ROOT_TEMPLATE = "root";
	private static final String LOGIN_TEMPLATE = "login/login";
	private static final String MAIN_TEMPLATE = "main/main";
	private static final String PROFILE_TEMPLATE = "profile/profile";
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	SetupController setupController;
	
	@Autowired
	ExternalFeedManager externalFeedManager;
	
	@Autowired
	SiteSettingsManager siteSettingsManager;
	
	@RequestMapping(value="/", method = RequestMethod.GET)
	public String root(@AuthenticationPrincipal User user, Model model) {
		
		if (setupController.isFirstTime()) {
			return setupController.firstTime(model);
		}
		
		SiteSettings siteSettings = siteSettingsManager.getSiteSettings();
		model.addAttribute("isRoot", true);
		if (Page.SPLASH.equals(siteSettings.getIndexPage())) {
			return ROOT_TEMPLATE;
		}
		if (Page.USER_PROFILE.equals(siteSettings.getIndexPage())) {
			return this.profile(siteSettings.getAdminUser().getUsername(), 
					user, model);
		}
		
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
	
	private void addNewPostForms(Model model, User owner) {
		//New Journal post
		final Journal journal = new Journal();
		model.addAttribute("newpost", journal);
		journal.setOwnerId(owner!=null?owner.getUserId():null);
		//New External Feed
		final ExternalAccountCredentials credentials = new ExternalAccountCredentials();
		model.addAttribute(NewExternalCredentialsController.CREDENTIALS_SESSION_NAME, credentials);
	}
	
	@RequestMapping(value="/main", method = RequestMethod.GET)
	public String main(
			Model model
			) {
		
		if (setupController.isFirstTime()) {
			return setupController.firstTime(model);
		}
		
		/*
		 * Progressive Web Application.
		 * 
		 * Only non-user reference data can be in the model
		 */
		addNewPostForms(model, null);	
				
		return MAIN_TEMPLATE;
	}
	
	@RequestMapping(value="/{username}", method = RequestMethod.GET)
	public String profile(@PathVariable String username, 
			@AuthenticationPrincipal User authenticatedUser, Model model) {
		
		if (setupController.isFirstTime()) {
			return setupController.firstTime(model);
		}
		
		// Load user
		Optional<User> profileUser = userRepository.findByUsername(username);
		if (!profileUser.isPresent()) {
			throw new UserNotFoundException("User "+username+" not found");
		}
		model.addAttribute("profileUser", profileUser.get());
		
		/*
		 * Progressive Web Application.
		 * 
		 * Only non-user reference data can be in the model
		 */
		addNewPostForms(model, profileUser.get());	
		
		return PROFILE_TEMPLATE;
	}
	
	@ModelAttribute("accountTypes")
	public Map<String, String> accountTypes() {
		Map<String, String> types = new TreeMap<String, String>();
		for (Entry<String, INetworkInfoSmall> entry: externalFeedManager.getProviders().entrySet()) {
			types.put(entry.getKey(), entry.getValue().getDisplayName());
		}
		return types;
	}

}
