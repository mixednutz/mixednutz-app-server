package net.mixednutz.app.server.manager.post.impl;

import java.util.Date;
import java.util.Optional;
import java.util.function.Supplier;

import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.post.AbstractPostView;
import net.mixednutz.app.server.entity.post.AbstractPostView.ViewPK;
import net.mixednutz.app.server.entity.post.Post;
import net.mixednutz.app.server.entity.post.PostComment;
import net.mixednutz.app.server.manager.post.PostViewManager;
import net.mixednutz.app.server.repository.PostViewRepository;


public abstract class PostViewManagerImpl<P extends Post<C>, C extends PostComment, V extends AbstractPostView> implements PostViewManager<P,C,V> {

	protected PostViewRepository<V> postViewRepository;
	
	@Override
	public void addView(P post, User viewer) {
		V view = getView(post, viewer).orElseGet(
				new Supplier<V>(){
					@Override
					public V get() {
						V view = create(post);
						view.setId(new ViewPK(post.getId(), viewer.getUserId()));
						view.setLastViewed(new Date());
						return view;
					}});
		view.setLastViewed(new Date());
		postViewRepository.save(view);
	}
	
	public Optional<V> getView(P post, User user) {
		return postViewRepository.findById(new AbstractPostView.ViewPK(post.getId(), user.getUserId()));
	}
	
	protected abstract V create(P post);


}
