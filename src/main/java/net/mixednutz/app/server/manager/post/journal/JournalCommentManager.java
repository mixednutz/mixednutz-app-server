package net.mixednutz.app.server.manager.post.journal;

import net.mixednutz.app.server.entity.post.journal.JournalComment;
import net.mixednutz.app.server.manager.TimelineElementManager;
import net.mixednutz.app.server.manager.post.CommentManager;

public interface JournalCommentManager extends CommentManager<JournalComment>, TimelineElementManager {

}
