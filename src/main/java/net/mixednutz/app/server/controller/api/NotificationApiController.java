package net.mixednutz.app.server.controller.api;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import net.mixednutz.api.core.model.PageRequest;
import net.mixednutz.api.model.INotification;
import net.mixednutz.api.model.IPage;
import net.mixednutz.api.model.IPageRequest.Direction;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.manager.NotificationTimelineManager;


@Controller
@RequestMapping(value={"/api/notification","/internal/notification"})
public class NotificationApiController {
	
	public static final String PAGE_SIZE_STR = "20";
	public static final int PAGE_SIZE = Integer.parseInt(PAGE_SIZE_STR);
	
	@Autowired
	private NotificationTimelineManager notificationTimelineManager;
	
	
	@RequestMapping(method = RequestMethod.GET)
	public @ResponseBody IPage<? extends INotification,Instant> getNotificationTimeline(
			@AuthenticationPrincipal User user) {
		return getNotificationTimeline(user, PAGE_SIZE);
	}
	
	@RequestMapping(value="/nextpage", method = RequestMethod.GET)
	public @ResponseBody IPage<? extends INotification,Instant> getNotificationTimeline(
			@AuthenticationPrincipal User user,
			@RequestParam(value="pageSize", defaultValue=PAGE_SIZE_STR) int pageSize) {
		if (user != null) {
			return notificationTimelineManager.getNotifications(user, 
					PageRequest.first(pageSize, Direction.LESS_THAN, String.class));
		}
		return null;
	}

}
