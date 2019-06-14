package net.mixednutz.app.server.manager;

import net.mixednutz.api.model.IUser;
import net.mixednutz.api.model.IUserSmall;
import net.mixednutz.app.server.entity.InternalTimelineElement;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.UserProfile;
import net.mixednutz.app.server.entity.post.journal.Journal;

public interface ApiManager {

	public InternalTimelineElement toTimelineElement(Journal journal, User viewer);
	
	public IUserSmall toUser(User entity);
	
	public IUser toUser(User entity, UserProfile profile);
	
}
