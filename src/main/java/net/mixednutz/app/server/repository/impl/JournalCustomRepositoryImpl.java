package net.mixednutz.app.server.repository.impl;

import java.util.Collections;
import java.util.List;

import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.post.journal.Journal;
import net.mixednutz.app.server.repository.JournalCustomRepository;

public class JournalCustomRepositoryImpl implements JournalCustomRepository {

	@Override
	public List<Journal> getNutsterzJournalsForTag(User user, String tag, Long excludeId) {
		// TODO Auto-generated method stub
		return Collections.emptyList();
	}

	
}
