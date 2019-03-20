package net.mixednutz.app.server.manager;

import net.mixednutz.api.model.ITimelineElement;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.post.journal.Journal;

public interface ApiManager {

	public ITimelineElement toTimelineElement(Journal journal, User viewer);
	
}
