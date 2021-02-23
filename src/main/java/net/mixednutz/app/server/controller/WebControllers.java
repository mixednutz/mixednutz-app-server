package net.mixednutz.app.server.controller;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import net.mixednutz.api.model.INotification;
import net.mixednutz.api.model.IPage;
import net.mixednutz.app.server.controller.api.NotificationApiController;
import net.mixednutz.app.server.entity.ComponentSettings;
import net.mixednutz.app.server.entity.Emoji;
import net.mixednutz.app.server.entity.EmojiCategory;
import net.mixednutz.app.server.entity.MenuItem;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.format.FormattingUtils;
import net.mixednutz.app.server.format.FormattingUtilsImpl;
import net.mixednutz.app.server.manager.EmojiManager;
import net.mixednutz.app.server.repository.MenuItemRepository;

@ControllerAdvice(basePackages={"net.mixednutz.app.server.controller.web"})
public class WebControllers {
	
	@Autowired
	private NotificationApiController notificationController;
	
	@Autowired
	private EmojiManager emojiManager;
	
	@Autowired
	private MenuItemRepository menuItemRepository;
	
	@Autowired(required=false)
	protected List<ComponentSettings> componentSettings;
	
	@ModelAttribute("notifications")
	public List<? extends INotification> getNotificationItems(@AuthenticationPrincipal User user) {
		IPage<? extends INotification, Instant> page = 
				notificationController.getNotificationTimeline(user);
		if (page!=null) {
			return page.getItems();
		}
		return Collections.emptyList();
	}
	
	@ModelAttribute("formatter")
	public FormattingUtils formatter(HttpServletRequest request) {
		return new FormattingUtilsImpl(request);
	}
	
	@ModelAttribute("emojiByCategory")
	public Map<EmojiCategory, List<Emoji>> emojiByCategory() {
		return emojiManager.findOrganizeByCategory();
	}
	
	@ModelAttribute("customMenu")
	public Iterable<MenuItem> customMenu() {
		return menuItemRepository.getTopMenu();
	}
	
	@ModelAttribute("componentCss")
	public Iterable<String> componentCss() {
		List<String> cssFiles = new ArrayList<>();
		for (ComponentSettings compSettings: componentSettings) {
			if (compSettings.css()) {
				cssFiles.add(compSettings.cssHref());
			}
		}
		return cssFiles;
	}

}
