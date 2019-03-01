package net.mixednutz.app.server.repository;

import java.util.List;

import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.post.journal.Journal;

public interface JournalCustomRepository {

	public List<Journal> getNutsterzJournalsForTag(User user, String tag, Long excludeId);
	
}
