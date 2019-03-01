package net.mixednutz.app.server.manager.post.journal;

import net.mixednutz.app.server.entity.post.journal.Journal;
import net.mixednutz.app.server.entity.post.journal.JournalComment;
import net.mixednutz.app.server.entity.post.journal.JournalView;
import net.mixednutz.app.server.manager.post.PostViewManager;

public interface JournalViewManager extends PostViewManager<Journal, JournalComment, JournalView> {

}
