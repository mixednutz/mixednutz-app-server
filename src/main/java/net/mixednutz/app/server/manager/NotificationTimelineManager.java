package net.mixednutz.app.server.manager;

import java.time.Instant;

import net.mixednutz.api.core.model.PageRequest;
import net.mixednutz.api.model.INotification;
import net.mixednutz.api.model.IPage;
import net.mixednutz.app.server.entity.User;

public interface NotificationTimelineManager {
	
	IPage<? extends INotification,Instant> getNotifications(User user, PageRequest<String> paging);
	
}
