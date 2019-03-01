package net.mixednutz.app.server.manager.post.journal.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.mixednutz.app.server.entity.post.journal.Journal;
import net.mixednutz.app.server.entity.post.journal.JournalComment;
import net.mixednutz.app.server.entity.post.journal.JournalView;
import net.mixednutz.app.server.manager.post.impl.PostViewManagerImpl;
import net.mixednutz.app.server.manager.post.journal.JournalViewManager;
import net.mixednutz.app.server.repository.JournalViewRepository;

@Transactional
@Service
public class JournalViewManagerImpl extends PostViewManagerImpl<Journal, JournalComment, JournalView> 
	implements JournalViewManager {

	@Override
	protected JournalView create(Journal journal) {
		JournalView view = new JournalView();
		view.setJournal(journal);
		return view;
	}
	
	@Autowired
	public void setPostViewRepository(JournalViewRepository postViewRepository) {
		this.postViewRepository = postViewRepository;
	}

}
