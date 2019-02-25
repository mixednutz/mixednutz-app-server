package net.mixednutz.app.server.manager;

import net.mixednutz.api.model.ITimelineElement;
import net.mixednutz.app.server.entity.Journal;
import net.mixednutz.app.server.entity.Post;

public interface ApiManager {

	public ITimelineElement toTimelineElement(Post<?> post);
	
	public ITimelineElement toTimelineElement(Journal journal);
	
}
