package net.mixednutz.app.server.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;

import net.mixednutz.app.server.controller.exception.ResourceNotFoundException;
import net.mixednutz.app.server.controller.exception.UserNotFoundException;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.VisibilityType;
import net.mixednutz.app.server.entity.post.journal.Journal;
import net.mixednutz.app.server.entity.post.journal.JournalComment;
import net.mixednutz.app.server.format.HtmlFilter;
import net.mixednutz.app.server.manager.ReactionManager;
import net.mixednutz.app.server.manager.TagManager;
import net.mixednutz.app.server.manager.post.journal.JournalManager;
import net.mixednutz.app.server.repository.JournalRepository;
import net.mixednutz.app.server.repository.UserRepository;

public class BaseJournalController {
	
	@Autowired
	private JournalRepository journalRepository;
	
	@Autowired
	private JournalManager journalManager;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private List<HtmlFilter> htmlFilters;
	
	@Autowired
	protected TagManager tagManager;
	
	@Autowired
	protected ReactionManager reactionManager;
	
	
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
//			notificationManager.markAsRead(user, journal);
		} 
		
		model.addAttribute("tagScores", tagManager.getTagScores(journal.getTags(), journal.getAuthor(), user));
		model.addAttribute("reactionScores", reactionManager.getReactionScores(journal.getReactions(), journal.getAuthor(), user));
//		if (journal.getOwner()!=null) {
//			model.addAttribute("profile", myprofileManager.get(journal.getOwner().getId()));
//		}
//		model.addAttribute("authors", accountManager.loadCommentAuthorsById(journal));
		
		//Side bar stuff
		model.addAttribute("recentPosts", journalManager.getUserJournals(
				journal.getOwner(), user, 5));
		if (!journal.getTags().isEmpty()) {
			model.addAttribute("tagPosts", journalManager.getJournalsForTag(user, 
					tagManager.getTagsArray(journal.getTags()), journal.getId()));
		}
		
		//New Comment
		model.addAttribute("newComment", new JournalComment());
		
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
				
		return "journal/view";
	}
	
	protected Journal save(Journal journal, 
//			Integer friendGroupId, 
			Long groupId, 
			Integer[] externalFeedId, String tagsString, boolean emailFriendGroup, User user) {
		if (user==null) {
			throw new AuthenticationCredentialsNotFoundException("You have to be logged in to do that");
		}
		
		journal.setAuthor(user);
		journal.setAuthorId(user.getUserId());
//		journal.setTags(new HashSet<JournalTag>());
		if (journal.getSubject()==null || journal.getSubject().trim().length()==0) {
			journal.setSubject("(No Subject)");
		}
//		String[] tagArray = tagManager.splitTags(tagsString);
//		for (String tagString : tagArray) {
//			journal.getTags().add(new JournalTag(journal, tagString));
//		}
		
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
//		if (externalFeedId!=null) {
//			Set<ExternalContent> crossposts = new LinkedHashSet<ExternalContent>();
//			for (Integer feedId: externalFeedId) {
//				AbstractFeed feed= externalFeedManager.get(feedId);
//				SocialContent content = externalFeedManager.crosspostItem(feed, journal);
//				crossposts.add(content.toPersistableExternalContent());
//			}
//			journal.setCrossposts(crossposts);
//			journalManager.save(journal);
//		}
		
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

}
