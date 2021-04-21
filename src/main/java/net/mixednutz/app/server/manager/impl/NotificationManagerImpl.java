package net.mixednutz.app.server.manager.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import net.mixednutz.api.core.model.NetworkInfo;
import net.mixednutz.app.server.entity.InternalTimelineElement;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.UserEmailAddress;
import net.mixednutz.app.server.entity.post.AbstractNotification;
import net.mixednutz.app.server.entity.post.AbstractPostComment;
import net.mixednutz.app.server.entity.post.CommentReplyNotification;
import net.mixednutz.app.server.entity.post.GroupedPosts;
import net.mixednutz.app.server.entity.post.Post;
import net.mixednutz.app.server.entity.post.PostComment;
import net.mixednutz.app.server.entity.post.PostNotification;
import net.mixednutz.app.server.entity.post.PostReaction;
import net.mixednutz.app.server.manager.ApiManager;
import net.mixednutz.app.server.manager.EmailMessageManager;
import net.mixednutz.app.server.manager.EmailMessageManager.EmailMessage;
import net.mixednutz.app.server.manager.NotificationManager;
import net.mixednutz.app.server.repository.PostNotificationRepository;
import net.mixednutz.app.server.repository.UserEmailAddressRepository;
import net.mixednutz.app.server.repository.UserRepository;

@Service
public class NotificationManagerImpl implements NotificationManager {
	
	@Autowired
	List<PostNotificationFactory<?,?,?>> postNotifications;
	
	@Autowired
	PostNotificationRepository notificationRepository;
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	ApiManager apiManager;
	
	@Autowired
	private UserEmailAddressRepository emailAddressRepository;
	
	@Autowired
	private EmailMessageManager emailManager;
	
	@Value("${mixednutz.email.display-name}")
	private String siteEmailName;
	
	@Autowired
	private NetworkInfo networkInfo;

	@Override
	public <P extends Post<C>, C extends PostComment> void notifyNewComment(P replyTo, 
			C comment) {
		if (!replyTo.getAuthor().getUserId().equals(comment.getAuthor().getUserId())) {
			//Don't make notification when author replies to own post
			
			PostNotification notification = null;
			for (PostNotificationFactory<?, ?, ?> iface: postNotifications) {
				if (iface.canConvert(replyTo.getClass())) {
					@SuppressWarnings("unchecked")
					PostNotificationFactory<P, C, ?> castedIface = (PostNotificationFactory<P, C, ?>) iface;
					notification = castedIface.createCommentNotification(replyTo, comment);
					break;
				}
			}
			
			notificationRepository.save((AbstractNotification) notification);
//			webPushService.send(notification.getAccountId(), getCommentNotificationItem(
//					dequeEntry(replyTo, notification)));
		}
		if (comment instanceof AbstractPostComment) {
			AbstractPostComment apComment = (AbstractPostComment) comment;
			sendCommentEmail(apComment);
			if (apComment.getInReplyTo()!=null) {
				notifyNewCommentReply(apComment.getInReplyTo(), apComment);
			}
		}
	}
	
	protected void sendCommentEmail(AbstractPostComment comment) {
		List<UserEmailAddress> emailAddressesToSend = new ArrayList<>();
		//Post Author
		emailAddressRepository.findByUserAndPrimaryTrue(
				comment.getPost().getAuthor())
				.ifPresent(emailAddress->emailAddressesToSend.add(emailAddress));
		//If Replied to Comment
		if (comment.getInReplyTo()!=null && 
				!comment.getInReplyTo().getAuthor().getUserId().equals(
					comment.getAuthor().getUserId())) {
			emailAddressRepository.findByUserAndPrimaryTrue(
					comment.getInReplyTo().getAuthor())
					.ifPresent(emailAddress->emailAddressesToSend.add(emailAddress));
		}
		emailAddressesToSend.forEach((emailAddress->sendCommentEmail(comment, emailAddress)));
	}
	
	protected void sendCommentEmail(AbstractPostComment comment, UserEmailAddress userEmailAddress) {
		EmailMessage msg = new EmailMessage();
		InternalTimelineElement element = apiManager.toTimelineElement(comment.getPost(), null);
		msg.setTo(Collections.singleton(userEmailAddress));
		msg.setSubject("New Comment for "+element.getTitle());
		
		Map<String, Object> model = new HashMap<>();
		model.put("entity", element);
		model.put("siteEmailName", siteEmailName);
		model.put("commentType", "comment");		
		model.put("commentUrl", networkInfo.getBaseUrl()+comment.getUri());
				
		emailManager.send("html/newcomment", msg, model);
	}
	
	public void notifyNewCommentReply(AbstractPostComment replyTo, AbstractPostComment comment) {
		if (!replyTo.getAuthor().getUserId().equals(comment.getAuthor().getUserId())) {
			//Don't make notification when author replies to own post
			
			PostNotification notification = new CommentReplyNotification(replyTo.getAuthor().getUserId(), replyTo, comment);
			notificationRepository.save((AbstractNotification) notification);
//			webPushService.send(notification.getAccountId(), getCommentNotificationItem(
//					dequeEntry(replyTo, notification)));
		}
	}



	@Override
	public <P extends Post<C>, C extends PostComment, R extends PostReaction> void notifyNewReaction(P reactedTo,
			R reaction) {
		if (!reactedTo.getAuthor().getUserId().equals(reaction.getReactorId())) {
			//Don't make notification when author replies to own post
			
			PostNotification notification = null;
			for (PostNotificationFactory<?, ?, ?> iface: postNotifications) {
				if (iface.canConvert(reactedTo.getClass())) {
					@SuppressWarnings("unchecked")
					PostNotificationFactory<P, ?, R> castedIface = (PostNotificationFactory<P, ?, R>) iface;
					notification = castedIface.createReactionNotification(reactedTo, reaction);
					break;
				}
			}
			
			notificationRepository.save((AbstractNotification) notification);
//			commentManager.sendEmail(comment);
//			webPushService.send(notification.getAccountId(), getCommentNotificationItem(
//					dequeEntry(replyTo, notification)));
		}
	}

	@Override
	public <P extends Post<C>, C extends PostComment> void markAsRead(User user, P post) {
		List<AbstractNotification> notifications = new ArrayList<>();
		for (PostNotificationFactory<?, ?, ?> iface: postNotifications) {
			if (iface.canConvert(post.getClass())) {
				@SuppressWarnings("unchecked")
				PostNotificationFactory<P, C, ?> castedIface = (PostNotificationFactory<P, C, ?>) iface;
				for (AbstractNotification notif: castedIface.lookupCommentNotifications(user, post)) {
					notifications.add(notif);
				}
				for (AbstractNotification notif: castedIface.lookupCommentReplyNotifications(user, post)) {
					notifications.add(notif);
				}
				for (AbstractNotification notif: castedIface.lookupReactionNotifications(user, post)) {
					notifications.add(notif);
				}
				break;
			}
		}
		notificationRepository.deleteAll(notifications);
	}

	@Override
	public <G extends GroupedPosts<P, C>, P extends Post<C>, C extends PostComment> void notifyNewAddition(G group,
			P post) {
		//TODO this requires subscriptions 
		
	}
		
}
