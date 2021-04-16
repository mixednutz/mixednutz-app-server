package net.mixednutz.app.server.entity.post.journal;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.springframework.stereotype.Component;

import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.post.AbstractCommentNotification;
import net.mixednutz.app.server.entity.post.AbstractCommentReplyNotification;
import net.mixednutz.app.server.entity.post.AbstractPostComment;
import net.mixednutz.app.server.entity.post.AbstractReactionNotification;
import net.mixednutz.app.server.entity.post.PostNotification;
import net.mixednutz.app.server.manager.NotificationManager.PostNotificationFactory;
import net.mixednutz.app.server.manager.impl.BaseNotificationFactory;

@Component
public class JournalNotificationFactory extends BaseNotificationFactory implements PostNotificationFactory<Journal, JournalComment, JournalReaction> {
	
	@Override
	public boolean canConvert(Class<?> postEntityClazz) {
		return Journal.class.isAssignableFrom(postEntityClazz);
	}

	@Override
	public PostNotification createCommentNotification(Journal replyTo, JournalComment comment) {
		return new JournalCommentNotification(replyTo.getAuthorId(), replyTo, comment);
	}

	@Override
	public PostNotification createReactionNotification(Journal reactedTo, JournalReaction reaction) {
		return new JournalReactionNotification(reactedTo.getAuthorId(), reactedTo, reaction);
	}
	
	@Override
	public Iterable<? extends AbstractCommentReplyNotification<? extends AbstractPostComment>> lookupCommentReplyNotifications(
			User user, Journal post) {
		return lookupCommentReplyNotifications(user, post.getComments());
	}
	
	@Override
	public Iterable<? extends AbstractCommentNotification<Journal, JournalComment>> lookupCommentNotifications(
			User user, Journal post) {
		return notificationRepository.loadNotifications((criteriaBuilder, itemRoot) ->{
			return criteriaBuilder.and(
					criteriaBuilder.equal(itemRoot.get("journalId"), post.getId()),
					criteriaBuilder.equal(itemRoot.get("userId"), user.getUserId()));
		}, JournalCommentNotification.class);
	}
	
	@Override
	public Iterable<? extends AbstractReactionNotification<Journal, JournalComment, JournalReaction>> lookupReactionNotifications(
			User user, Journal reactedTo) {
		return notificationRepository.loadNotifications((criteriaBuilder, itemRoot) ->{
			return criteriaBuilder.and(
					criteriaBuilder.equal(itemRoot.get("journalId"), reactedTo.getId()),
					criteriaBuilder.equal(itemRoot.get("userId"), user.getUserId()));
		}, JournalReactionNotification.class);
	}



	@Entity
	@DiscriminatorValue(JournalCommentNotification.TYPE)
	public static class JournalCommentNotification extends AbstractCommentNotification<Journal, JournalComment> {
		
		public static final String TYPE = "NewJournalComment";
		private static final String TYPE_DISPLAY = "journal";
		
		public JournalCommentNotification(Long userId, Journal journal, JournalComment comment) {
			super(TYPE, userId, journal, comment);
		}

		public JournalCommentNotification() {
			super(TYPE);
		}

		@ManyToOne
		@JoinColumn(name="comment_id", insertable=false, updatable=false)
		public JournalComment getComment() {
			return comment;
		}
		@ManyToOne
		@JoinColumn(name="journal_id", insertable=false, updatable=false)
		public Journal getJournal() {
			return post;
		}
		@Column(name="journal_id", insertable=true, updatable=false)
		public Long getJournalId() {
			return postId;
		}
		

		public void setJournal(Journal journal) {
			this.post = journal;
		}
		public void setJournalId(Long journalId) {
			this.postId = journalId;
		}

		@Transient
		public String getPostTypeDisplayName() {
			return TYPE_DISPLAY;
		}
		@Transient
		public String getPostSubject() {
			return post.getSubject();
		}
		
	}
	
	@Entity
	@DiscriminatorValue(JournalReactionNotification.TYPE)
	public static class JournalReactionNotification extends AbstractReactionNotification<Journal, JournalComment, JournalReaction> {
		
		public static final String TYPE = "JournalReaction";
		private static final String TYPE_DISPLAY = "journal";
		
		public JournalReactionNotification(Long userId, Journal journal, JournalReaction reaction) {
			super(TYPE, userId, journal, reaction);
		}

		public JournalReactionNotification() {
			super(TYPE);
		}
		
		@ManyToOne()
		@JoinColumn(name="reaction_id", insertable=false, updatable=false)
		public JournalReaction getReaction() {
			return reaction;
		}
		@Column(name="journal_id", insertable=true, updatable=false)
		public Long getJournalId() {
			return reactedToId;
		}
		@ManyToOne()
		@JoinColumn(name="journal_id", insertable=false, updatable=false)
		public Journal getJournal() {
			return reactedTo;
		}
		
		public void setJournal(Journal journal) {
			this.reactedTo = journal;
		}
		public void setJournalId(Long journalId) {
			this.reactedToId = journalId;
		}

		@Transient
		public String getReactedToTypeDisplayName() {
			return TYPE_DISPLAY;
		}
		@Transient
		public String getReactedToSubject() {
			return reactedTo.getSubject();
		}
		
	}

}
