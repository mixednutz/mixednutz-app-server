package net.mixednutz.app.server.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;

import net.mixednutz.app.server.controller.exception.ResourceMovedPermanentlyException;
import net.mixednutz.app.server.controller.exception.ResourceNotFoundException;
import net.mixednutz.app.server.controller.exception.UserNotFoundException;
import net.mixednutz.app.server.entity.ExternalFeeds.AbstractFeed;
import net.mixednutz.app.server.entity.InternalTimelineElement;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.VisibilityType;
import net.mixednutz.app.server.entity.post.journal.Journal;
import net.mixednutz.app.server.entity.post.journal.JournalComment;
import net.mixednutz.app.server.entity.post.journal.JournalFactory;
import net.mixednutz.app.server.entity.post.journal.JournalTag;
import net.mixednutz.app.server.entity.post.journal.ScheduledJournal;
import net.mixednutz.app.server.format.HtmlFilter;
import net.mixednutz.app.server.manager.ApiManager;
import net.mixednutz.app.server.manager.ExternalFeedManager;
import net.mixednutz.app.server.manager.NotificationManager;
import net.mixednutz.app.server.manager.ReactionManager;
import net.mixednutz.app.server.manager.TagManager;
import net.mixednutz.app.server.manager.post.journal.JournalManager;
import net.mixednutz.app.server.repository.EmojiRepository;
import net.mixednutz.app.server.repository.ExternalFeedRepository;
import net.mixednutz.app.server.repository.JournalCommentRepository;
import net.mixednutz.app.server.repository.JournalRepository;
import net.mixednutz.app.server.repository.ReactionRepository;
import net.mixednutz.app.server.repository.UserProfileRepository;
import net.mixednutz.app.server.repository.UserRepository;

public class BaseJournalController {
	
	@Autowired
	protected JournalRepository journalRepository;
	
	@Autowired
	private JournalManager journalManager;
	
	@Autowired
	protected JournalCommentRepository journalCommentRepository;
	
	@Autowired
	private UserProfileRepository profileRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private List<HtmlFilter> htmlFilters;
	
	@Autowired
	protected TagManager tagManager;
	
	@Autowired
	protected ReactionManager reactionManager;
	
	@Autowired
	protected ReactionRepository reactionRepository;
	
	@Autowired
	protected JournalFactory journalFactory;
	
	@Autowired
	protected EmojiRepository emojiRepository;
	
	@Autowired
	protected NotificationManager notificationManager;
	
	@Autowired
	private ExternalFeedRepository externalFeedRepository;
	
	@Autowired
	private ExternalFeedManager externalFeedManager;
	
	@Autowired
	private ApiManager apiManager;
	
	protected Journal get(String username, int year, int month, int day, @PathVariable String subjectKey) {
		User profileUser = userRepository.findByUsername(username)
				.orElseThrow(new Supplier<UserNotFoundException>(){
					@Override
					public UserNotFoundException get() {
						throw new UserNotFoundException("User "+username+" not found");
					}});
		
		return journalRepository
				.findByOwnerAndPublishDateKeyAndSubjectKey(profileUser, LocalDate.of(year, month, day), subjectKey)
				.orElseThrow(new Supplier<ResourceNotFoundException>() {
					@Override
					public ResourceNotFoundException get() {
						throw new ResourceNotFoundException("Journal not found");
					}
				});
	}
	
	protected String getJournal(final Journal journal, Authentication auth, Model model) {		
		if (auth==null &&
				!VisibilityType.WORLD.equals(journal.getVisibility().getVisibilityType())) {
			throw new AuthenticationCredentialsNotFoundException("This is not a public journal.");
		} else if (auth!=null) {
//			if (!journalManager.isVisible(journal, (User) auth.getPrincipal())) {
//				throw new NotAuthorizedException("User does not have permission to view this journal.");
//			}
		}
		
		model.addAttribute("journal", journal);
		User user = auth!=null?(User) auth.getPrincipal():null;
		
		//HTML Filter
		String filteredHtml = journal.getBody();
		for (HtmlFilter htmlFilter: htmlFilters) {
			filteredHtml = htmlFilter.filter(filteredHtml);
		}
		journal.setFilteredBody(filteredHtml);
			
		//Settings
//		Mysettings settings = mysettingsManager.get(journal.getOwner());
//		if (settings==null) {
//			settings = mysettingsManager.getDefaults();
//		}
		
		if (user!=null) {
			journalManager.incrementViewCount(journal, user);
			notificationManager.markAsRead(user, journal);
		} 
		
		model.addAttribute("tagScores", tagManager.getTagScores(journal.getTags(), journal.getAuthor(), user));
		model.addAttribute("reactionScores", reactionManager.getReactionScores(journal.getReactions(), journal.getAuthor(), user));
		if (journal.getOwner()!=null) {
			model.addAttribute("profile", profileRepository.findById(journal.getOwner().getUserId()).orElse(null));
		}
		model.addAttribute("authors", journalManager.loadCommentAuthors(journal.getComments()));
		
		//Side bar stuff
		model.addAttribute("recentPosts", journalManager.getUserJournals(
				journal.getOwner(), user, 5));
		if (!journal.getTags().isEmpty()) {
			model.addAttribute("tagPosts", journalManager.getJournalsForTag(user, 
					tagManager.getTagsArray(journal.getTags()), journal.getId()));
		}
		
		//New Comment Form
		journalFactory.newCommentForm(model);
		
		//Tags String
		model.addAttribute("tagsString", tagManager.getTagsString(journal.getTags()));
		
//		if (auth!=null) {
//			//external feeds compatible for journal crossposting
//			Map<FeedType, List<AbstractFeed>> journalCrosspostFeeds = 
//					externalFeedManager.compatibableFeedsToCrosspostJournal(user);
//			model.addAttribute("journalCrosspostFeeds", journalCrosspostFeeds);
//			
//			//Friend Groups
//			List<FriendGroup> fgroups = friendGroupManager.getGroups((User) auth.getPrincipal(), false);
//			model.addAttribute("fgroups", fgroups);
//		}
//				
//		//UBBCOdes
//		model.addAttribute(UbbCodesHolder.getUbbCodes());
//		
//		//Stuff for javascript editing
//		journal.calculateVisibility();
		
		// Publish Date
		if (journal.getScheduled()!=null) {
			model.addAttribute("localPublishDate", 
					journal.getScheduled().getPublishDate().toLocalDateTime());
		}	
				
		return "journal/view";
	}
	
	protected Journal save(Journal journal, 
//			Integer friendGroupId, 
			Long groupId, 
			Long[] externalFeedId, String tagsString, boolean emailFriendGroup, 
			LocalDateTime localPublishDate,
			User user) {
		if (user==null) {
			throw new AuthenticationCredentialsNotFoundException("You have to be logged in to do that");
		}
		
		journal.setAuthor(user);
		journal.setAuthorId(user.getUserId());
		journal.setTags(new HashSet<>());
		if (journal.getSubject()==null || journal.getSubject().trim().length()==0) {
			journal.setSubject("(No Subject)");
		}
		if (localPublishDate!=null) {
			journal.setScheduled(new ScheduledJournal());
			journal.getScheduled()
				.setPublishDate(ZonedDateTime.of(localPublishDate, ZoneId.systemDefault()));
			journal.getScheduled().setExternalFeedId(externalFeedId);
			journal.getScheduled().setEmailFriendGroup(emailFriendGroup);
		} else {
			journal.setDatePublished(ZonedDateTime.now());
		}
		String[] tagArray = tagManager.splitTags(tagsString);
		for (String tagString : tagArray) {
			journal.getTags().add(new JournalTag(journal, tagString));
		}
		
		if (groupId!=null) {
			journal.setOwnerId(groupId);
			journal.setOwner(userRepository.findById(groupId)
					.orElseThrow(new Supplier<UserNotFoundException>(){
						@Override
						public UserNotFoundException get() {
							throw new UserNotFoundException("User or Group with ID "+groupId+" not found");
						}}));
		} else {
			journal.setOwner(user);
			journal.setOwnerId(user.getUserId());
		}
		
//		journal.parseVisibility(user, friendGroupId, groupId);
		
		journal = journalRepository.save(journal);
		
		//Feed Actions
		if (journal.getScheduled()==null) {
			InternalTimelineElement exportableEntity = apiManager.toTimelineElement(journal, null);
			if (externalFeedId!=null) {
				for (Long feedId: externalFeedId) {
					AbstractFeed feed= externalFeedRepository.findById(feedId).get();
					externalFeedManager.crosspost(feed, 
							exportableEntity.getTitle(), 
							exportableEntity.getUrl(), 
							tagArray);
				}
			}
		}
		
//		if (journal.getPrivateGroupId()>0) {
//			journal = journalManager.get(journalId);
//			FriendGroup friendGroup = friendGroupManager.get(journal.getPrivateGroupId());
//			
//			//Email all Category members here
//			if (emailFriendGroup) {
//				List<FriendGroupMember> members = friendGroupMemberManager.getMembers(friendGroup, null);
//				for (FriendGroupMember member: members) {
//					if (member.getMember()!=null) {
//						AbstractEmailMessage msg = journalManager.getNotificationEmail(journal, emailFormatter, member.getMember());
//						friendGroupMemberManager.sendEmail(member, msg);
//					}
//				}
//			}
//			
//			//Email non members here
//			List<FriendGroupNonAccount> nonAccounts = friendGroupNonAccountManager.getNonAccounts(friendGroup, null);
//			for (FriendGroupNonAccount invite: nonAccounts) {
//				AbstractEmailMessage msg = journalManager.getNotificationEmail(journal, emailFormatter, invite);
//				friendGroupNonAccountManager.sendEmail(invite, msg);
//			}
//		}
		
		return journal;
	}
	
	protected Journal update(Journal form, Long journalId, 
//			Integer friendGroupId, 
			Integer groupId, String tagsString, 
			LocalDateTime localPublishDate,
			User user) {
		if (user==null) {
			throw new AuthenticationCredentialsNotFoundException("You have to be logged in to do that");
		}
		Journal entity = journalRepository.findById(journalId).orElseThrow(()->{
			return new ResourceNotFoundException("");
		});
		if (!entity.getAuthor().equals(user)) {
			throw new AccessDeniedException("Journal #"+journalId+" - That's not yours to edit!");
		}
		
		entity.setSubject(form.getSubject());
		if (form.getSubject()==null || form.getSubject().trim().length()==0) {
			entity.setSubject("(No Subject)");
		}
		if (localPublishDate!=null) {
			if (entity.getScheduled()==null) {
				entity.setScheduled(new ScheduledJournal());
			}
			entity.getScheduled()
				.setPublishDate(ZonedDateTime.of(localPublishDate, ZoneId.systemDefault()));
		} 
		entity.setSubjectKey(form.getSubjectKey());
		entity.setDescription(form.getDescription());
		entity.setBody(form.getBody());
		
//		journal.parseVisibility(user, friendGroupId, groupId);
		
//		String[] tagArray = tagManager.splitTags(tagsString);
//		mergeTags(tagArray, journal);
		
		return journalRepository.save(entity);
	}
	
	protected void incrementHitCount(Journal entity) {
		entity.incrementHitCount();
		journalRepository.save(entity);
	}
	
	protected void delete(Long journalId, User user) {
		Journal entity = journalRepository.findById(journalId).orElseThrow(()->{
			return new ResourceNotFoundException("");
		});
		if (!entity.getAuthor().equals(user)) {
			throw new AccessDeniedException("Journal #"+journalId+" - That's not yours to edit!");
		}
		
		journalRepository.delete(entity);
	}
	
	protected JournalComment getComment(Long commentId) {
		return journalCommentRepository.findById(commentId)
			.orElseThrow(new Supplier<ResourceNotFoundException>() {
				@Override
				public ResourceNotFoundException get() {
					throw new ResourceNotFoundException("Comment not found");
				}
			});
	}
	
	protected JournalComment saveComment(JournalComment form, Journal journal, User user) {
		form.setJournal(journal);
		form.setAuthor(user);
		
		JournalComment comment = journalCommentRepository.save(form);
		notificationManager.notifyNewComment(journal, comment);
		
		return comment;
	}
	
	@ExceptionHandler(ResourceMovedPermanentlyException.class)
	public String handleException(final ResourceMovedPermanentlyException e) {
	    return "redirect:"+e.getRedirectUri();
	}

}
