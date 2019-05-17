package net.mixednutz.app.server.manager.post.journal.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.mixednutz.api.model.ITimelineElement;
import net.mixednutz.app.server.entity.InternalTimelineElement;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.post.journal.Journal;
import net.mixednutz.app.server.entity.post.journal.JournalComment;
import net.mixednutz.app.server.entity.post.journal.JournalView;
import net.mixednutz.app.server.manager.post.impl.PostManagerImpl;
import net.mixednutz.app.server.manager.post.journal.JournalManager;
import net.mixednutz.app.server.manager.post.journal.JournalViewManager;
import net.mixednutz.app.server.repository.JournalRepository;

@Transactional
@Service
public class JournalManagerImpl extends PostManagerImpl<Journal, JournalComment, JournalView>
	implements JournalManager {

	@Autowired
	public void setPostRepository(JournalRepository journalRepository) {
		this.postRepository = journalRepository;
	}

	public JournalRepository getJournalRepository() {
		return (JournalRepository) postRepository;
	}

	@Autowired
	public void setPostViewManager(JournalViewManager journalViewManager) {
		this.postViewManager = journalViewManager;
	}

	protected InternalTimelineElement toTimelineElement(Journal journal, User viewer) {
		return apiManager.toTimelineElement(journal, viewer);
	}
	
	@Override
	public List<? extends ITimelineElement> getUserJournals(User user, User viewer, int pageSize) {
		return this.getUserTimelineInternal(user, viewer, pageSize);
	}

	@Override
	public Map<String, List<? extends ITimelineElement>> getJournalsForTag(User user, String[] tags,
			Long excludeId) {
		Map<String, List<? extends ITimelineElement>> journals = new HashMap<>();
		for (String tag: tags) {
			List<Journal> journalsForTag = getJournalRepository().getNutsterzJournalsForTag(user, tag, excludeId);
			if (!journalsForTag.isEmpty()) {
				List<InternalTimelineElement> newjournals = new ArrayList<>();
				for (Journal journal: journalsForTag) {
					newjournals.add(toTimelineElement(journal, null));
				}
				journals.put(tag, newjournals);
			}
		}
		return journals;
	}

}
