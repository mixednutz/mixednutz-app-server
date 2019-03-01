package net.mixednutz.app.server.manager.post;

import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.post.AbstractPostView;
import net.mixednutz.app.server.entity.post.Post;
import net.mixednutz.app.server.entity.post.PostComment;

public interface PostViewManager<P extends Post<C>, C extends PostComment, V extends AbstractPostView> {

	public void addView(P post, User viewer);
	
}
