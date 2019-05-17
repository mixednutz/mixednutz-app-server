package net.mixednutz.app.server.manager;

import net.mixednutz.app.server.entity.InternalTimelineElement;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.post.journal.Journal;

public interface ApiManager {

	public InternalTimelineElement toTimelineElement(Journal journal, User viewer);
	
}
