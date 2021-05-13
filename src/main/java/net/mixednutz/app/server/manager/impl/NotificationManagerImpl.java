package net.mixednutz.app.server.manager.impl;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import net.mixednutz.api.core.model.NetworkInfo;
import net.mixednutz.api.model.IUserSmall;
import net.mixednutz.app.server.entity.InternalTimelineElement;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.UserEmailAddress;
import net.mixednutz.app.server.entity.post.AbstractNotification;
import net.mixednutz.app.server.entity.post.AbstractPostComment;
import net.mixednutz.app.server.entity.post.AbstractReaction;
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
	
	private static final Logger LOG = LoggerFactory.getLogger(NotificationManagerImpl.class);
	
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
		LOG.info("Sending email notification for comment {}", comment.getUri());
		Set<UserEmailAddress> emailAddressesToSend = new LinkedHashSet<>();
		//Post Author - exclude author of new comment
		if (!comment.getPost().getAuthor().getUserId().equals(
				comment.getAuthor().getUserId())) {
			emailAddressRepository.findByUserAndPrimaryTrue(
					comment.getPost().getAuthor())
					.ifPresent(emailAddress->emailAddressesToSend.add(emailAddress));
		}
		
		//If Replied to Comment - exclude author of new comment
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
		LOG.info("Sending email notification for comment {} to {}", comment.getUri(), userEmailAddress.getEmailAddress());
		EmailMessage msg = new EmailMessage();
		InternalTimelineElement element = apiManager.toTimelineElement(comment.getPost(), null);
		IUserSmall user = apiManager.toUser(comment.getAuthor());
		msg.setTo(Collections.singleton(userEmailAddress));
		msg.setSubject("New Comment for "+element.getTitle());
		
		Map<String, Object> model = new HashMap<>();
		model.put("entity", element);
		model.put("user", user);
		model.put("siteEmailName", siteEmailName);
		model.put("commentType", "comment");	
		String url = UriComponentsBuilder
				.fromUri(URI.create(networkInfo.getBaseUrl()+comment.getUri()))
				.queryParam("utm_source","comment_email")
				.queryParam("utm_medium","email")
				.build().toUriString();
		model.put("commentUrl", url);
				
		emailManager.send("html/newcomment", msg, model);
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
//			webPushService.send(notification.getAccountId(), getCommentNotificationItem(
//					dequeEntry(replyTo, notification)));
		}
		if (reaction instanceof AbstractReaction) {
			AbstractReaction aReaction = (AbstractReaction) reaction;
			sendReactionEmail(aReaction);
		}
	}
	
	protected void sendReactionEmail(AbstractReaction reaction) {
		LOG.info("Sending email notification for comment {}", reaction.getParentUri());
		Set<UserEmailAddress> emailAddressesToSend = new LinkedHashSet<>();
		//Post Author - exclude author of new comment
		if (!reaction.getPost().getAuthor().getUserId().equals(
				reaction.getReactorId())) {
			emailAddressRepository.findByUserAndPrimaryTrue(
					reaction.getPost().getAuthor())
					.ifPresent(emailAddress->emailAddressesToSend.add(emailAddress));
		}
		
		emailAddressesToSend.forEach((emailAddress->sendReactionEmail(reaction, emailAddress)));
	}
	
	protected void sendReactionEmail(AbstractReaction reaction, UserEmailAddress userEmailAddress) {
		LOG.info("Sending email notification for reaction {} to {}", reaction.getParentUri(), userEmailAddress.getEmailAddress());
		EmailMessage msg = new EmailMessage();
		InternalTimelineElement element = apiManager.toTimelineElement(reaction.getPost(), null);
		IUserSmall user = apiManager.toUser(reaction.getReactor());
		msg.setTo(Collections.singleton(userEmailAddress));
		msg.setSubject("New Reaction for "+element.getTitle());
		
		Map<String, Object> model = new HashMap<>();
		model.put("entity", element);
		model.put("user", user);
		model.put("siteEmailName", siteEmailName);	
		String url = UriComponentsBuilder
				.fromHttpUrl(networkInfo.getBaseUrl()+reaction.getParentUri())
				.queryParam("utm_source","reaction_email")
				.queryParam("utm_medium","email")
				.build().toUriString();
		model.put("reactionUrl", url);
				
		emailManager.send("html/newreaction", msg, model);
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
