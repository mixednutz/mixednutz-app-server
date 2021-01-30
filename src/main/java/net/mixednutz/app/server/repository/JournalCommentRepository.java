package net.mixednutz.app.server.repository;

import org.springframework.stereotype.Repository;

import net.mixednutz.app.server.entity.post.journal.JournalComment;

@Repository
public interface JournalCommentRepository extends CommentRepository<JournalComment> {

}
