package net.mixednutz.app.server.controller.web;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import net.mixednutz.app.server.entity.ComponentSettings;
import net.mixednutz.app.server.entity.ExternalFeeds.AbstractFeed;
import net.mixednutz.app.server.entity.MenuItem;
import net.mixednutz.app.server.entity.SiteSettings;
import net.mixednutz.app.server.entity.SiteSettings.Page;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.UserSettings;
import net.mixednutz.app.server.entity.VisibilityType;
import net.mixednutz.app.server.manager.SiteSettingsManager;
import net.mixednutz.app.server.repository.ExternalFeedRepository;
import net.mixednutz.app.server.repository.MenuItemRepository;
import net.mixednutz.app.server.repository.SiteSettingsRepository;
import net.mixednutz.app.server.repository.UserSettingsRepository;

@Controller
public class UserSettingsController {
	
	@Autowired
	private UserSettingsRepository settingsRepository;
	
	@Autowired
	private ExternalFeedRepository externalFeedRepository;
	
	@Autowired
	SiteSettingsRepository siteSettingsRepository;
	
	@Autowired
	SiteSettingsManager siteSettingsManager;
	
	@Autowired
	MenuItemRepository menuItemRepository;
	
	@Autowired(required=false)
	List<ComponentSettings> componentSettings;
		
	private String settingsForm(Model model) {
		SiteSettings siteSettings = siteSettingsManager.getSiteSettings();
		
		SettingsForm form = new SettingsForm();
		form.setIndexPage(siteSettings.getIndexPage());
		form.setCommentsAllowedDefault(siteSettings.getCommentsAllowedDefault());
		model.addAttribute("form", form);
		
		// Menu
		MenuForm menuForm = new MenuForm();
		menuItemRepository.findAll().forEach(menuForm.menuItems::add);
		model.addAttribute("menuForm", menuForm);
		model.addAttribute("newMenuItem", new MenuItem());
		
		//Additional Settings not found here
		List<String> fragments = new ArrayList<>();
		List<String> scriptFragments = new ArrayList<>();
		for (ComponentSettings compSettings: componentSettings) {
			for (Entry<String,?> entry: compSettings.getSettings().entrySet()) {
				model.addAttribute(entry.getKey(), entry.getValue());
			}
			if (compSettings.includeHtmlFragment()) {
				fragments.add(compSettings.includeHtmlFragmentName());
			}
			if (compSettings.includeScriptFragment()) {
				scriptFragments.add(compSettings.includeScriptFragmentName());
			}
		}
		model.addAttribute("componentFragments", fragments);
		model.addAttribute("componentScriptFragments", scriptFragments);
		
		return "settings/settings";
	}
	
	@RequestMapping(value="/settings", method=RequestMethod.GET)
	public String editSettings(@AuthenticationPrincipal User user, Model model) {
//		model.addAttribute(new PushSubscription());
				
		return settingsForm(model);
	}
	
	@RequestMapping(value="/settings", method=RequestMethod.POST)
	public String saveSettings(SettingsForm form, Errors errors,
			@AuthenticationPrincipal User user, Model model) {
		
		if (errors.hasErrors()) {
			return settingsForm(model);
		}
		
		//This siteSettings is from the cache
		SiteSettings siteSettings = siteSettingsManager.getSiteSettings();
		if (user.equals(siteSettings.getAdminUser())) {
			//Get the non-cached version so we can save it
			siteSettings = siteSettingsRepository.findById(user.getUserId()).get();
			siteSettings.setIndexPage(form.getIndexPage());
			siteSettings.setCommentsAllowedDefault(form.isCommentsAllowedDefault());
			siteSettingsManager.save(siteSettings);
		}
		
		UserSettings settings = settingsRepository.findById(user.getUserId()).orElseGet(
				()->new UserSettings(user));
		settings.setShowCombinedExternalFeedsOnProfile(form.isShowCombinedExternalFeedsOnProfile());
		settingsRepository.save(settings);
		
		List<AbstractFeed> feeds = externalFeedRepository.findByUser(user);
		for (AbstractFeed feed: feeds) {
			if (form.visibilityMap().containsKey(feed.getFeedId())) {
				feed.setVisibility(form.visibilityMap().get(feed.getFeedId()));
			}
		}
		externalFeedRepository.saveAll(feeds);		
		
		return "redirect:/settings";
	}
	
	@RequestMapping(value="/settings/menu/new", method=RequestMethod.POST)
	public String addMenuItem(MenuItem form, Errors errors,
			@AuthenticationPrincipal User user, Model model) {
		
		if (errors.hasErrors()) {
			return settingsForm(model);
		}
		
		menuItemRepository.save(form);
		
		return "redirect:/settings";
	}
	
	@RequestMapping(value="/settings/menu/update", method=RequestMethod.POST)
	public String updateMenuItems(MenuForm form, Errors errors,
			@AuthenticationPrincipal User user, Model model) {
		
		if (errors.hasErrors()) {
			return settingsForm(model);
		}
		
		menuItemRepository.saveAll(form.getMenuItems());
		
		return "redirect:/settings";
	}

	public static class SettingsForm {
		boolean showCombinedExternalFeedsOnProfile;
		Page indexPage;
		Long[] feedId;
		VisibilityType[] visibility;
		boolean commentsAllowedDefault;
		
		public Map<Long, VisibilityType> visibilityMap() {
			Map<Long, VisibilityType> map = new LinkedHashMap<>();
			for (int i=0; i<feedId.length; i++) {
				map.put(feedId[i], visibility[i]);
			}
			return map;
		}
		public boolean isShowCombinedExternalFeedsOnProfile() {
			return showCombinedExternalFeedsOnProfile;
		}
		public void setShowCombinedExternalFeedsOnProfile(boolean showCombinedExternalFeedsOnProfile) {
			this.showCombinedExternalFeedsOnProfile = showCombinedExternalFeedsOnProfile;
		}
		public Page getIndexPage() {
			return indexPage;
		}
		public void setIndexPage(Page indexPage) {
			this.indexPage = indexPage;
		}
		public Long[] getFeedId() {
			return feedId;
		}
		public void setFeedId(Long[] feedId) {
			this.feedId = feedId;
		}
		public VisibilityType[] getVisibility() {
			return visibility;
		}
		public void setVisibility(VisibilityType[] visibility) {
			this.visibility = visibility;
		}
		public boolean isCommentsAllowedDefault() {
			return commentsAllowedDefault;
		}
		public void setCommentsAllowedDefault(boolean commentsAllowedDefault) {
			this.commentsAllowedDefault = commentsAllowedDefault;
		}
	}
	
	public static class MenuForm {
		List<MenuItem> menuItems = new ArrayList<>();

		public List<MenuItem> getMenuItems() {
			return menuItems;
		}

		public void setMenuItems(List<MenuItem> menuItems) {
			this.menuItems = menuItems;
		}
		
	}
}
