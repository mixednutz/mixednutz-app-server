package net.mixednutz.app.server.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import net.mixednutz.app.server.entity.Journal;
import net.mixednutz.app.server.entity.JournalComment;
import net.mixednutz.app.server.entity.User;

@Repository
public interface JournalRepository extends PostRepository<Journal, JournalComment> {

	Optional<Journal> findByOwnerAndPublishDateKeyAndSubjectKey(
			User owner,
			LocalDate publishDateKey,
			String subjectKey
			);
	
}
