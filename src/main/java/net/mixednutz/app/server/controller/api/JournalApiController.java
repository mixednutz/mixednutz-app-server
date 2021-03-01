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
import net.mixednutz.app.server.entity.post.journal.JournalTag;
import net.mixednutz.app.server.manager.ReactionManager;
import net.mixednutz.app.server.manager.TagManager;
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
			notificationManager.notifyNewReaction(journal, reaction);
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
			notificationManager.notifyNewReaction(journal, reaction);
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
	
	//------------
	// Tags Mappings
	//------------
	
	@RequestMapping(value="/{username}/journal/{year}/{month}/{day}/{subjectKey}/tag/toggle", method = RequestMethod.POST)
	public @ResponseBody JournalTag toggleTag(
			@PathVariable String username, 
			@PathVariable int year, @PathVariable int month, 
			@PathVariable int day, @PathVariable String subjectKey,
			@RequestParam(value="tag") String tagString,
			@AuthenticationPrincipal final User user) {
		final Journal journal = get(username, year, month, day, subjectKey);
		
		JournalTag tag =  tagManager.toggleTag(tagString, journal.getTags(), journal.getAuthor(), 
				user, new TagManager.NewTagCallback<JournalTag>(){
			@Override
			public JournalTag createTag(String tagString) {
				if (user.equals(journal.getAuthor())) {
					return new JournalTag(journal, tagString);
				}
				return new JournalTag(journal, tagString, user);
			}});
		journalRepository.save(journal);
		return tag;
	}
	
	@RequestMapping(value="/{username}/journal/{year}/{month}/{day}/{subjectKey}/tag", method = RequestMethod.POST)
	public @ResponseBody Collection<JournalTag> apiNewTag(
			@PathVariable String username, 
			@PathVariable int year, @PathVariable int month, 
			@PathVariable int day, @PathVariable String subjectKey,
			@RequestParam(value="tagsString") String tagsString,
			@AuthenticationPrincipal final User user) {
		final Journal journal = get(username, year, month, day, subjectKey);
		
		String[] tagArray = tagManager.splitTags(tagsString);
		Collection<JournalTag> addedTags = addTags(tagArray, journal, user);
		journalRepository.save(journal);
		return addedTags;
	}
	
	/**
	 * Adds tags to the thread that are not already in there
	 * 
	 * @param tagArray
	 * @param thread
	 */
	protected Collection<JournalTag> addTags(final String[] tagArray, final Journal journal, 
			final User currentUser) {
		return tagManager.addTags(tagArray, journal.getTags(), journal.getAuthor(), 
				currentUser, new TagManager.NewTagCallback<JournalTag>(){
			@Override
			public JournalTag createTag(String tagString) {
				if (currentUser.equals(journal.getAuthor())) {
					return new JournalTag(journal, tagString);
				}
				return new JournalTag(journal, tagString, currentUser);
			}});
	}

}
