package net.mixednutz.app.server.controller.web;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import net.mixednutz.app.server.entity.ExternalFeeds.AbstractFeed;
import net.mixednutz.app.server.entity.UserSettings.Page;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.UserSettings;
import net.mixednutz.app.server.entity.Visibility;
import net.mixednutz.app.server.repository.ExternalFeedRepository;
import net.mixednutz.app.server.repository.UserSettingsRepository;

@Controller
public class UserSettingsController {
	
	@Autowired
	private UserSettingsRepository settingsRepository;
	
	@Autowired
	private ExternalFeedRepository externalFeedRepository;
	
	private String settingsForm(Model model) {
		model.addAttribute("form", new SettingsForm());
		
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
		
		UserSettings settings = settingsRepository.findById(user.getUserId()).orElseGet(
				new NewUserSettingsSupplier(user));
		settings.setShowCombinedExternalFeedsOnProfile(form.isShowCombinedExternalFeedsOnProfile());
		settings.setIndexPage(form.getIndexPage());
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

	static class NewUserSettingsSupplier implements Supplier<UserSettings> {

		final User user;
		
		public NewUserSettingsSupplier(User user) {
			this.user = user;
		}

		@Override
		public UserSettings get() {
			UserSettings settings = new UserSettings();
			settings.setUserId(user.getUserId());
			settings.setUser(user);
			return settings;
		}
		
	}
	
	public static class SettingsForm {
		boolean showCombinedExternalFeedsOnProfile;
		Page indexPage;
		Long[] feedId;
		Visibility[] visibility;
		
		public Map<Long, Visibility> visibilityMap() {
			Map<Long, Visibility> map = new LinkedHashMap<>();
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
		public Visibility[] getVisibility() {
			return visibility;
		}
		public void setVisibility(Visibility[] visibility) {
			this.visibility = visibility;
		}
	}
}
