package net.mixednutz.app.server.manager.post.journal.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.mixednutz.app.server.entity.InternalTimelineElement;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.post.journal.JournalComment;
import net.mixednutz.app.server.manager.post.impl.AbstractCommentManagerImpl;
import net.mixednutz.app.server.manager.post.journal.JournalCommentManager;
import net.mixednutz.app.server.repository.JournalCommentRepository;

@Transactional
@Service
public class JournalCommentManagerImpl extends AbstractCommentManagerImpl<JournalComment>
	implements JournalCommentManager {
	
	@Autowired
	public void setPostRepository(JournalCommentRepository journalCommentRepository) {
		this.commentRepository = journalCommentRepository;
	}

	@Override
	protected InternalTimelineElement toTimelineElement(JournalComment comment, User viewer) {
		return apiManager.toTimelineElement(comment, viewer);
	}

}
