package net.mixednutz.app.server.manager.post.journal;

import java.util.List;
import java.util.Map;

import net.mixednutz.api.model.ITimelineElement;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.post.journal.Journal;
import net.mixednutz.app.server.entity.post.journal.JournalComment;
import net.mixednutz.app.server.manager.post.PostManager;


public interface JournalManager extends PostManager<Journal, JournalComment>{

	/**
	 * Returns public Journals from any user.
	 * @param user
	 * @param viewer
	 * @param pageSize
	 * @param paging
	 * @return
	 */
	List<? extends ITimelineElement> getUserJournals(User user, User viewer,
			int pageSize);
	
	/**
	 * Returns posts from the people you subscribe to for a given tag.
	 * 
	 * @param user
	 * @param tags
	 * @param excludeId
	 * @return
	 */
	Map<String, List<? extends ITimelineElement>> getJournalsForTag(User user, 
			String[] tags, Long excludeId);
	
	void incrementViewCount(Journal journal, User viewer);
}
