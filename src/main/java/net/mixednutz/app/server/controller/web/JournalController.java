package net.mixednutz.app.server.controller.web;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
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

import net.mixednutz.app.server.controller.BaseJournalController;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.post.journal.Journal;
import net.mixednutz.app.server.entity.post.journal.JournalFactory;


@Controller
public class JournalController extends BaseJournalController {
	

	@RequestMapping(value="/{username}/journal/{year}/{month}/{day}/{subjectKey}", method = {RequestMethod.GET,RequestMethod.HEAD})
	public String getJournal(@PathVariable String username, 
			@PathVariable int year, @PathVariable int month, 
			@PathVariable int day, @PathVariable String subjectKey, Authentication auth, Model model) {
		Journal journal = get(username, year, month, day, subjectKey);
		getJournal(journal, auth,model);
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
	
	@RequestMapping(value="/journal/new", method = RequestMethod.POST, params="submit")
	public String saveNew(@ModelAttribute(JournalFactory.MODEL_ATTRIBUTE) Journal journal, 
//			@RequestParam("fgroup_id") Integer friendGroupId, 
			@RequestParam("group_id") Long groupId,
			@RequestParam(value="externalFeedId", required=false) Integer[] externalFeedId,
			@RequestParam(value="tagsString", defaultValue="") String tagsString,
			@RequestParam(value="email_fgroup", defaultValue="false") boolean emailFriendGroup,
			@DateTimeFormat(iso=ISO.DATE_TIME) @RequestParam(value="localPublishDate", required=false) LocalDateTime localPublishDate,
			@AuthenticationPrincipal User user, Model model, Errors errors) {
		journal = save(journal, groupId, externalFeedId, 
				tagsString, emailFriendGroup, localPublishDate, user);

		return "redirect:"+journal.getUri();
	}	
	
}
