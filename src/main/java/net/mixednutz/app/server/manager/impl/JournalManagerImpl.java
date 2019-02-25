package net.mixednutz.app.server.manager.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.mixednutz.app.server.entity.Journal;
import net.mixednutz.app.server.entity.JournalComment;
import net.mixednutz.app.server.manager.JournalManager;
import net.mixednutz.app.server.repository.JournalRepository;

@Transactional
@Service
public class JournalManagerImpl extends PostManagerImpl<Journal, JournalComment>
	implements JournalManager {

	@Autowired
	public void setPostRepository(JournalRepository postRepository) {
		super.setPostRepository(postRepository);
	}

}
