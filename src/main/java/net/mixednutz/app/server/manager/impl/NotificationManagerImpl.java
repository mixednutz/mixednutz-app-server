package net.mixednutz.app.server.manager.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.post.AbstractNotification;
import net.mixednutz.app.server.entity.post.AbstractPostComment;
import net.mixednutz.app.server.entity.post.CommentReplyNotification;
import net.mixednutz.app.server.entity.post.GroupedPosts;
import net.mixednutz.app.server.entity.post.Post;
import net.mixednutz.app.server.entity.post.PostComment;
import net.mixednutz.app.server.entity.post.PostNotification;
import net.mixednutz.app.server.entity.post.PostReaction;
import net.mixednutz.app.server.manager.NotificationManager;
import net.mixednutz.app.server.repository.PostNotificationRepository;
import net.mixednutz.app.server.repository.UserRepository;

@Service
public class NotificationManagerImpl implements NotificationManager {
	
	@Autowired
	List<PostNotificationFactory<?,?,?>> postNotifications;
	
	@Autowired
	PostNotificationRepository notificationRepository;
	
	@Autowired
	UserRepository userRepository;

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
//			commentManager.sendEmail(comment);
//			webPushService.send(notification.getAccountId(), getCommentNotificationItem(
//					dequeEntry(replyTo, notification)));
		}
		if (comment instanceof AbstractPostComment) {
			AbstractPostComment apComment = (AbstractPostComment) comment;
			if (apComment.getInReplyTo()!=null) {
				notifyNewCommentReply(apComment.getInReplyTo(), apComment);
			}
		}
	}
	
	@Override
	public void notifyNewCommentReply(AbstractPostComment replyTo, AbstractPostComment comment) {
		if (!replyTo.getAuthor().getUserId().equals(comment.getAuthor().getUserId())) {
			//Don't make notification when author replies to own post
			
			PostNotification notification = new CommentReplyNotification(replyTo.getAuthor().getUserId(), replyTo, comment);
			notificationRepository.save((AbstractNotification) notification);
//			commentManager.sendEmail(comment);
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
