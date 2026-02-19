package net.mixednutz.app.server.controller.web;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.NativeWebRequest;
import org.w3c.activitystreams.model.ActivityImpl;

import net.mixednutz.api.activitypub.ActivityPubManager;
import net.mixednutz.app.server.controller.BaseJournalController;
import net.mixednutz.app.server.controller.exception.ResourceNotFoundException;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.post.journal.Journal;
import net.mixednutz.app.server.entity.post.journal.JournalComment;
import net.mixednutz.app.server.entity.post.journal.JournalFactory;


@Controller
public class JournalController extends BaseJournalController {
	
	@Autowired
	private ActivityPubManager activityPubManager;
	
	//------------
	// View Mappings
	//------------
	
	@RequestMapping(value="/journal/id/{journalId}", method = {RequestMethod.GET,RequestMethod.HEAD})
	public String getJournal(@PathVariable Long journalId, Authentication auth, Model model) {
		Journal journal = journalRepository.findById(journalId).orElseThrow(()->{
			return new ResourceNotFoundException("");
		});
		getJournal(journal, auth,model);
		incrementHitCount(journal);
		return "journal/view";
	}

	@RequestMapping(value="/{username}/journal/{year}/{month}/{day}/{subjectKey}", method = {RequestMethod.GET,RequestMethod.HEAD})
	public String getJournal(@PathVariable String username, 
			@PathVariable int year, @PathVariable int month, 
			@PathVariable int day, @PathVariable String subjectKey, Authentication auth, Model model) {
		Journal journal = get(username, year, month, day, subjectKey);
		getJournal(journal, auth,model);
		incrementHitCount(journal);
		return "journal/view";
	}
	
	@RequestMapping(value="/embed/{username}/journal/{year}/{month}/{day}/{subjectKey}", method = RequestMethod.GET)
	public String getJournalEmbed(@PathVariable String username, 
			@PathVariable int year, @PathVariable int month, 
			@PathVariable int day, @PathVariable String subjectKey, Authentication auth, Model model) {
		Journal journal = get(username, year, month, day, subjectKey);
		model.addAttribute("journal", journal);
		return "journal/embed";
	}

	//------------
	// Insert Mappings
	//------------
	
	@RequestMapping(value="/journal/new", method = RequestMethod.POST, params="submit")
	public String saveNew(@ModelAttribute(JournalFactory.MODEL_ATTRIBUTE) Journal journal, 
//			@RequestParam("fgroup_id") Integer friendGroupId, 
			@RequestParam(value="externalListId", required=false) String[] externalListId,
			@RequestParam(value="group_id",required=false) Long groupId,
			@RequestParam(value="externalFeedId", required=false) Long[] externalFeedId,
			@RequestParam(value="tagsString", defaultValue="") String tagsString,
			@RequestParam(value="email_fgroup", defaultValue="false") boolean emailFriendGroup,
			@DateTimeFormat(iso=ISO.DATE_TIME) @RequestParam(value="localPublishDate", required=false) LocalDateTime localPublishDate,
			@AuthenticationPrincipal User user, Model model, Errors errors,
			NativeWebRequest request) {
		journal = save(journal, externalListId, groupId, externalFeedId, 
				tagsString, emailFriendGroup, localPublishDate, user, request);

		return "redirect:"+journal.getUri();
	}	
	
	
	//------------
	// Update Mappings
	//------------
			
	@RequestMapping(value="/journal/id/{journalId}/edit", method = RequestMethod.POST, params="submit")
	public String updateModal(@ModelAttribute("journal") Journal journal, 
			@PathVariable Long journalId, 
//			@RequestParam("fgroup_id") Integer friendGroupId, 
			@RequestParam(value="externalListId", required=false) String[] externalListId,
			@RequestParam(value="group_id",required=false) Long groupId,
			@RequestParam(value="externalFeedId", required=false) Long[] externalFeedId,
			@RequestParam(value="tagsString", defaultValue="") String tagsString,
			@DateTimeFormat(iso=ISO.DATE_TIME) @RequestParam(value="localPublishDate", required=false) LocalDateTime localPublishDate,
			@AuthenticationPrincipal User user, Model model, Errors errors) {
		
		Journal savedJournal = update(journal, journalId, externalListId, groupId, 
				externalFeedId, tagsString, localPublishDate, user);
		
		return "redirect:"+savedJournal.getUri();
	}
	
	@RequestMapping(value="/journal/id/{journalId}/schedule_edit", method = RequestMethod.POST, params="submit")
	public String scheduleUpdateModal(@ModelAttribute("journal") Journal journal, 
			@PathVariable Long journalId, 
//			@RequestParam("fgroup_id") Integer friendGroupId, 
			@RequestParam(value="externalListId", required=false) String[] externalListId,
			@RequestParam(value="group_id",required=false) Long groupId,
			@RequestParam(value="externalFeedId", required=false) Long[] externalFeedId,
			@DateTimeFormat(iso=ISO.DATE_TIME) @RequestParam(value="localEffectiveDate") LocalDateTime localEffectiveDate,
			@AuthenticationPrincipal User user, Model model, Errors errors,
			NativeWebRequest request) {
		
		Journal savedJournal = scheduleUpdate(journal, journalId, externalListId, groupId, 
				externalFeedId, localEffectiveDate, user, request);
		
		return "redirect:"+savedJournal.getUri();
	}
	

	//------------
	// Delete Mappings
	//------------
	

	@RequestMapping(value="/journal/id/{journalId}/delete", method = RequestMethod.POST, params="confirm")
	public String deleteModal(@PathVariable Long journalId, 
			@AuthenticationPrincipal User user) {
		
		delete(journalId, user);
		
		return "redirect:/main";
	}
	

	//------------
	// Comments Mappings
	//------------
	
	@RequestMapping(value="/{username}/journal/{year}/{month}/{day}/{subjectKey}/comment/new", method = RequestMethod.POST, params="submit")
	public String comment(@ModelAttribute(JournalFactory.MODEL_ATTRIBUTE_COMMENT) JournalComment comment, 
			@PathVariable String username, 
			@PathVariable int year, @PathVariable int month, 
			@PathVariable int day, @PathVariable String subjectKey,
			@RequestParam(value="externalFeedId", required=false) Integer externalFeedId,
			@AuthenticationPrincipal User user, Model model, Errors errors) {
		if (user==null) {
			throw new AuthenticationCredentialsNotFoundException("You have to be logged in to do that");
		}
		
		Journal journal = get(username, year, month, day, subjectKey);
		comment = saveComment(comment, journal, user);
				
		return "redirect:"+comment.getUri();
	}
	
	@RequestMapping(value="/{username}/journal/{year}/{month}/{day}/{subjectKey}/comment/{inReplyToId}/reply", method = RequestMethod.POST, params="submit")
	public String commentReply(@ModelAttribute(JournalFactory.MODEL_ATTRIBUTE_COMMENT) JournalComment comment, 
			@PathVariable String username, 
			@PathVariable int year, @PathVariable int month, 
			@PathVariable int day, @PathVariable String subjectKey,
			@PathVariable Long inReplyToId,
			@RequestParam(value="externalFeedId", required=false) Integer externalFeedId,
			@AuthenticationPrincipal User user, Model model, Errors errors) {
		if (user==null) {
			throw new AuthenticationCredentialsNotFoundException("You have to be logged in to do that");
		}
		
		Journal journal = get(username, year, month, day, subjectKey);
		
		comment.setInReplyTo(getComment(inReplyToId));
		comment = saveComment(comment, journal, user);
				
		return "redirect:"+comment.getUri();
	}
	
	@RequestMapping(value="/{username}/journal/{year}/{month}/{day}/{subjectKey}/comment/{commentId}", method = RequestMethod.POST, params="submit")
	public String commentEdit(@ModelAttribute(JournalFactory.MODEL_ATTRIBUTE_COMMENT) JournalComment comment, 
			@PathVariable String username, 
			@PathVariable int year, @PathVariable int month, 
			@PathVariable int day, @PathVariable String subjectKey,
			@PathVariable Long commentId,
			@AuthenticationPrincipal User user, Model model, Errors errors) {
		if (user==null) {
			throw new AuthenticationCredentialsNotFoundException("You have to be logged in to do that");
		}
		
		Journal journal = get(username, year, month, day, subjectKey);
		JournalComment existingComment = get(journal, commentId);
		if (!existingComment.getAuthor().equals(user)) {
			throw new AccessDeniedException("Comment #"+commentId+" - That's not yours to edit!");
		}
		
		existingComment.setBody(comment.getBody());
		comment = updateComment(existingComment);
				
		return "redirect:"+comment.getUri();
	}
	
	/**
	 * Show ActivityStream Object
	 * 
	 * @param username
	 * @param year
	 * @param month
	 * @param day
	 * @param subjectKey
	 * @return
	 */
	@RequestMapping(value=ActivityPubManager.NOTE_URI_PREFIX+"/{username}/journal/{year}/{month}/{day}/{subjectKey}", 
			method = RequestMethod.GET,
			produces=ActivityImpl.APPLICATION_ACTIVITY_VALUE)
	public @ResponseBody org.w3c.activitystreams.Object getJournalActivityNote(
			@PathVariable String username, 
			@PathVariable int year, @PathVariable int month, 
			@PathVariable int day, @PathVariable String subjectKey,
			Authentication auth) {
		
		final Journal journal = get(username, year, month, day, subjectKey);
		assertVisibility(journal, auth);
		
		return activityPubManager.toNote(apiManager.toTimelineElement(journal, null), 
				journal.getAuthor().getUsername(), true);
	}
	@RequestMapping(value=ActivityPubManager.CREATE_URI_PREFIX+"/{username}/journal/{year}/{month}/{day}/{subjectKey}", 
			method = RequestMethod.GET,
			produces=ActivityImpl.APPLICATION_ACTIVITY_VALUE)
	public @ResponseBody org.w3c.activitystreams.Object getJournalActivityCreate(
			@PathVariable String username, 
			@PathVariable int year, @PathVariable int month, 
			@PathVariable int day, @PathVariable String subjectKey,
			Authentication auth) {
		
		final Journal journal = get(username, year, month, day, subjectKey);
		assertVisibility(journal, auth);
		
		return activityPubManager.toCreateNote(apiManager.toTimelineElement(journal, null), 
				journal.getAuthor().getUsername());
	}
		
	/**
	 * Show ActivityStream Object
	 * 
	 * @param username
	 * @param year
	 * @param month
	 * @param day
	 * @param subjectKey
	 * @return
	 */
	@RequestMapping(value=ActivityPubManager.NOTE_URI_PREFIX+"/{username}/journal/{year}/{month}/{day}/{subjectKey}/comment/{commentId}", 
			method = RequestMethod.GET,
			produces=ActivityImpl.APPLICATION_ACTIVITY_VALUE)
	public @ResponseBody org.w3c.activitystreams.Object getJournalCommentActivityNote(
			@PathVariable String username, 
			@PathVariable int year, @PathVariable int month, 
			@PathVariable int day, @PathVariable String subjectKey,
			@PathVariable long commentId) {
		
		final Journal journal = get(username, year, month, day, subjectKey);
		final JournalComment comment = get(journal, commentId);
		return activityPubManager.toNote(apiManager.toTimelineElement(comment, null), 
				journal.getAuthor().getUsername(), true);
	}
	@RequestMapping(value=ActivityPubManager.CREATE_URI_PREFIX+"/Create/{username}/journal/{year}/{month}/{day}/{subjectKey}/comment/{commentId}", 
			method = RequestMethod.GET,
			produces=ActivityImpl.APPLICATION_ACTIVITY_VALUE)
	public @ResponseBody org.w3c.activitystreams.Object getJournalCommentActivityCreate(
			@PathVariable String username, 
			@PathVariable int year, @PathVariable int month, 
			@PathVariable int day, @PathVariable String subjectKey,
			@PathVariable long commentId) {
		
		final Journal journal = get(username, year, month, day, subjectKey);
		final JournalComment comment = get(journal, commentId);
		return activityPubManager.toCreateNote(apiManager.toTimelineElement(comment, null), 
				journal.getAuthor().getUsername());
	}
}
