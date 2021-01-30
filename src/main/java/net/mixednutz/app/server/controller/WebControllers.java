package net.mixednutz.app.server.controller;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import net.mixednutz.api.core.model.Notification;
import net.mixednutz.app.server.entity.Emoji;
import net.mixednutz.app.server.entity.EmojiCategory;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.format.FormattingUtils;
import net.mixednutz.app.server.format.FormattingUtilsImpl;
import net.mixednutz.app.server.manager.EmojiManager;

@ControllerAdvice(basePackages={"net.mixednutz.app.server.controller.web"})
public class WebControllers {
	
//	@Autowired
//	private NotificationController notificationController;
//	
	@Autowired
	private EmojiManager emojiManager;
	
	@ModelAttribute("notifications")
	public List<Notification> getNotificationItems(@AuthenticationPrincipal User user) {
//		NotificationPage page = notificationController.getNotificationItems(user);
//		if (page!=null) {
//			return page.getItems();
//		}
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

}
