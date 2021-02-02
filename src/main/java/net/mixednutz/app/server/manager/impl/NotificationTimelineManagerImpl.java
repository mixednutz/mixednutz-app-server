package net.mixednutz.app.server.manager.impl;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.mixednutz.api.core.model.Notification;
import net.mixednutz.api.core.model.PageBuilder;
import net.mixednutz.api.core.model.PageRequest;
import net.mixednutz.api.model.INotification;
import net.mixednutz.api.model.IPage;
import net.mixednutz.api.model.IPageRequest.Direction;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.post.AbstractCommentNotification;
import net.mixednutz.app.server.entity.post.AbstractNotification;
import net.mixednutz.app.server.entity.post.AbstractReactionNotification;
import net.mixednutz.app.server.entity.post.Post;
import net.mixednutz.app.server.manager.ApiManager;
import net.mixednutz.app.server.manager.NotificationTimelineManager;
import net.mixednutz.app.server.repository.PostNotificationRepository;
import net.mixednutz.app.server.repository.UserRepository;

@Service
public class NotificationTimelineManagerImpl implements NotificationTimelineManager {

	@Autowired
	PostNotificationRepository postNotificationRepository;
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	ApiManager apiManager;
		
	@Override
	public IPage<? extends INotification, Instant> getNotifications(User user, PageRequest<String> paging) {
		List<AbstractNotification> contents = null;
		final net.mixednutz.api.core.model.PageRequest<Instant> pageRequest = net.mixednutz.api.core.model.PageRequest
				.convert(paging, Instant.class, (str) -> {
					return ZonedDateTime.parse(str).toInstant();
				});
		if (paging.getStart()==null) {
			
			contents = postNotificationRepository.getMyNotificationsLessThan(user.getUserId(), ZonedDateTime.now(), 
					org.springframework.data.domain.PageRequest.of(0, paging.getPageSize()));
		} else {
			ZonedDateTime start = pageRequest.getStart().atZone(ZoneId.systemDefault());
			if (paging.getDirection()==Direction.LESS_THAN) {
				contents = postNotificationRepository.getMyNotificationsLessThan(user.getUserId(), start, 
						org.springframework.data.domain.PageRequest.of(0, paging.getPageSize()));
			} else {
				contents = postNotificationRepository.getMyNotificationsGreaterThan(user.getUserId(), start, 
						org.springframework.data.domain.PageRequest.of(0, paging.getPageSize()));
			}
		}
		
		List<Notification> results = new ArrayList<>();
		contents.stream()
			// Group By Class
			.collect(Collectors.groupingBy(AbstractNotification::getClass))
		.entrySet().stream()
			// Map to List of API objects
			.map((e)->{
				if (AbstractCommentNotification.class.isAssignableFrom(e.getKey())) {
					
					List<AbstractCommentNotification<?,?>> notifications = e.getValue().stream()
						.map((n)->{
							return (AbstractCommentNotification<?,?>)n;
						}).collect(Collectors.toList());
					
					return getCommentNotificationItems(notifications);
				}
				if (AbstractReactionNotification.class.isAssignableFrom(e.getKey())) {
									
					List<AbstractReactionNotification<?,?,?>> notifications = e.getValue().stream()
						.map((n)->{
							return (AbstractReactionNotification<?,?,?>)n;
						}).collect(Collectors.toList());
					
					return getReactionNotificationItems(notifications);
				}
				return null;
			})
			// Remove nulls
			.filter((l)->l!=null)
			// Dump list into Results list
			.forEach(results::addAll);
		
		return new PageBuilder<Notification, Instant>()
				.addItems(results)
				.setPageRequest(pageRequest)
				.setTrimToPageSize(true)
				.setReSortComparator((o1, o2) -> {
						return -o1.getDateNotified().compareTo(o2.getDateNotified());
					})
				.setTokenCallback((item) -> {
						return item.getDateNotified().toInstant();
					})
				.build();
	}
		
	protected List<Notification> getCommentNotificationItems(List<AbstractCommentNotification<?, ?>> notifications) {
		if (notifications==null) {
			return Collections.emptyList();
		}
		
		Map<Post<?>, List<AbstractCommentNotification<?, ?>>> groupByPost = 
			notifications.stream()
				.collect(Collectors.groupingBy(AbstractCommentNotification::getPost));
		
		//create NotificationItems
		final List<Notification> items = new ArrayList<>();
		for (Entry<Post<?>, List<AbstractCommentNotification<?, ?>>> entry: groupByPost.entrySet()) {
			items.add(getCommentNotificationItem(entry));
		}
		return items;
	}
	
	protected Notification getCommentNotificationItem(Entry<Post<?>, List<AbstractCommentNotification<?, ?>>> entry) {
		
		Set<Long> authors = new LinkedHashSet<>();
		for (AbstractCommentNotification<?, ?> notification: entry.getValue()) {
			authors.add(notification.getAuthorId());
		}
		final String firstAuthor = this.getAuthorName(authors.iterator().next() 
	//			entry.getKey().getPrivateGroupId()
				);
		final String firstAuthorPicture = this.getAuthorPicture(authors.iterator().next());
		final AbstractCommentNotification<?, ?> firstNotification = entry.getValue().get(0);
		final AbstractCommentNotification<?, ?> lastNotification = entry.getValue().get(entry.getValue().size()-1);
		HtmlStringBuffer buffer = new HtmlStringBuffer();
		buffer.html("<span class=\"author\">")
			.append(firstAuthor);
		if (authors.size()>1) {
			buffer.append(" and ")
				.append(authors.size()-1);
			if (authors.size()>2) {
				buffer.append(" others");
			} else {
				buffer.append(" other");
			}
		}
		buffer.html("</span>")
			.append(" replied to your ")
			.append(firstNotification.getPostTypeDisplayName())
			.append(" ");
					
		if (firstNotification.getPostSubject()!=null) {
			buffer.html("<span class=\"title\">")
				.append(firstNotification.getPostSubject())
				.html("</span> ");
		}
		
		//Collection notification IDs
		List<Long> notificationIds = entry.getValue().stream()
			.map((i)->{
				return i.getId();
			})
			.collect(Collectors.toList());
		
		return new Notification(
				firstNotification.getType(),
				firstAuthorPicture,
				buffer.toPlainString(),
				buffer.toHtmlString(),
				firstNotification.getUri(), 
				lastNotification.getCommentDateCreated(),
				notificationIds);
	}
	
	protected List<Notification> getReactionNotificationItems(List<AbstractReactionNotification<?, ?, ?>> notifications) {
		if (notifications==null) {
			return Collections.emptyList();
		}
		
		Map<Post<?>, List<AbstractReactionNotification<?, ?, ?>>> groupByPost = 
			notifications.stream()
				.collect(Collectors.groupingBy(AbstractReactionNotification::getReactedTo));

		//create NotificationItems
		final List<Notification> items = new ArrayList<>();
		for (Entry<Post<?>, List<AbstractReactionNotification<?, ?, ?>>> entry: groupByPost.entrySet()) {
			items.add(getReactionNotificationItem(entry));
		}
		return items;
	}
	
	protected Notification getReactionNotificationItem(Entry<Post<?>, List<AbstractReactionNotification<?, ?, ?>>> entry) {
		
		Set<Long> authors = new LinkedHashSet<>();
		for (AbstractReactionNotification<?, ?, ?> notification: entry.getValue()) {
			authors.add(notification.getReactorId());
		}
		final String firstAuthor = this.getAuthorName(authors.iterator().next());
		final String firstAuthorPicture = this.getAuthorPicture(authors.iterator().next());
		final AbstractReactionNotification<?, ?, ?> firstNotification = entry.getValue().get(0);
		final AbstractReactionNotification<?, ?, ?> lastNotification = entry.getValue().get(entry.getValue().size()-1);
		HtmlStringBuffer buffer = new HtmlStringBuffer();
		buffer.html("<span class=\"author\">")
			.append(firstAuthor);
		if (authors.size()>1) {
			buffer.append(" and ")
				.append(authors.size()-1);
			if (authors.size()>2) {
				buffer.append(" others");
			} else {
				buffer.append(" other");
			}
		}
		buffer.html("</span>")
			.append(" reacted to your ")
			.append(firstNotification.getReactedToTypeDisplayName())
			.append(" ");
		
		if (firstNotification.getReactedToSubject()!=null) {
			buffer.html("<span class=\"title\">")
				.append(firstNotification.getReactedToSubject())
				.html("</span> ");
		}
		
		//Collection notification IDs
		List<Long> notificationIds = entry.getValue().stream()
			.map((i)->{
				return i.getId();
			})
			.collect(Collectors.toList());
		
		return new Notification(
				firstNotification.getType(),
				firstAuthorPicture,
				buffer.toPlainString(),
				buffer.toHtmlString(),
				firstNotification.getUri(), 
				lastNotification.getDateCreated(),
				notificationIds);
	}
		
	
	private String getAuthorName(Long userId 
	//		Integer fgroupId
			) {
		Optional<User> firstAuthor = userRepository.findById(userId);
		if (firstAuthor.isPresent()) {
			return firstAuthor.get().getUsername();
		}
	//	if (fgroupId!=0) {
	//		FriendGroupNonAccount nonaccount = friendGroupNonAccountManager.get(accountId, 
	//				fgroupId);
	//		return nonaccount.getDisplayName()!=null?nonaccount.getDisplayName():nonaccount.getEmailAddress().getDisplayName();
	//	} 
		return "Former Member";
	}
	
	private String getAuthorPicture(Long userId) {
		Optional<User> firstAuthor = userRepository.findById(userId);
		if (firstAuthor.isPresent()) {
			apiManager.getAvatarUri(firstAuthor.get().getAvatarFilename());
		}
		return apiManager.getAvatarUri(null);
	}

	private static class HtmlStringBuffer {
		
		private StringBuffer plain = new StringBuffer();
		private StringBuffer html = new StringBuffer();
		
		public synchronized HtmlStringBuffer html(String str) {
			html.append(str);
			return this;
		}
		
		public synchronized HtmlStringBuffer append(String str) {
			html.append(str);
			plain.append(str);
			return this;
		}
		
		public synchronized HtmlStringBuffer append(int i) {
			html.append(i);
			plain.append(i);
			return this;
		}
		
		public synchronized String toPlainString() {
			return plain.toString();
		}
		
		public synchronized String toHtmlString() {
			return html.toString();
		}
		
	}

	
	

}