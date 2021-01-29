package net.mixednutz.app.server.controller.api;

import java.util.Collection;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import net.mixednutz.app.server.controller.BaseJournalController;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.post.journal.Journal;
import net.mixednutz.app.server.entity.post.journal.JournalReaction;
import net.mixednutz.app.server.manager.ReactionManager;
import net.mixednutz.app.server.repository.EmojiRepository;

@Controller
@RequestMapping({"/api","/internal"})
public class JournalApiController extends BaseJournalController {
	
	@RequestMapping(value="/{username}/journal/{year}/{month}/{day}/{subjectKey}/reaction/toggle", method = RequestMethod.POST)
	public @ResponseBody JournalReaction apiToggleReaction(
			@PathVariable String username, 
			@PathVariable int year, @PathVariable int month, 
			@PathVariable int day, @PathVariable String subjectKey,
			@RequestParam(value="emojiId") String emojiId,
			@AuthenticationPrincipal final User user) {
		
		final Journal journal = get(username, year, month, day, subjectKey);
		
//		CollectionDifference<ChapterReaction> diff= new CollectionDifference<>(chapter.getReactions());
		JournalReaction reaction =  reactionManager.toggleReaction(emojiId, journal.getReactions(), journal.getAuthor(), 
				user, new NewJournalReaction(emojiRepository, journal, user));
		if (reaction!=null) {
			reaction = reactionRepository.save(reaction);
			journalRepository.save(journal);
//			notificationManager.notifyNewReaction(journal, reaction);
		} else {
//			notificationManager.unnotifiyReaction(diff.missing(journal.getReactions()));
			journalRepository.save(journal);
		}
		
		return reaction;
	}
	
	@RequestMapping(value="/{username}/journal/{year}/{month}/{day}/{subjectKey}/reaction", method = RequestMethod.POST)
	public @ResponseBody Collection<JournalReaction> apiNewReaction(
			@PathVariable String username, 
			@PathVariable int year, @PathVariable int month, 
			@PathVariable int day, @PathVariable String subjectKey,
			@RequestParam(value="emojiId") String emojiId,
			@AuthenticationPrincipal final User user) {
		final Journal journal = get(username, year, month, day, subjectKey);
		Collection<JournalReaction> addedReactions = reactionManager.addReaction(emojiId, journal.getReactions(), journal.getAuthor(), 
				user, new NewJournalReaction(emojiRepository, journal, user));
		for (JournalReaction reaction: addedReactions) {
			reaction = reactionRepository.save(reaction);
//			notificationManager.notifyNewReaction(journal, reaction);
		}
		journalRepository.save(journal);
		return addedReactions;
	}
	
	private static class NewJournalReaction implements ReactionManager.NewReactionCallback<JournalReaction> {
		private final User user;
		private final Journal journal;
		private EmojiRepository emojiRepository;
		
		public NewJournalReaction(EmojiRepository emojiRepository, Journal journal, User user) {
			super();
			this.emojiRepository = emojiRepository;
			this.journal = journal;
			this.user = user;
		}

		@Override
		public JournalReaction createReaction(String emojiId) {
			JournalReaction reaction = new JournalReaction(journal, emojiId, user);
			reaction.setEmoji(emojiRepository.findById(emojiId).get());
			return reaction;
		}
		
	}

}
