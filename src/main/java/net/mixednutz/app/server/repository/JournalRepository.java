package net.mixednutz.app.server.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.post.journal.Journal;
import net.mixednutz.app.server.entity.post.journal.JournalComment;

@Repository
public interface JournalRepository extends PostRepository<Journal, JournalComment>, JournalCustomRepository {

	Optional<Journal> findByOwnerAndPublishDateKeyAndSubjectKey(
			User owner,
			LocalDate publishDateKey,
			String subjectKey
			);
	
}
