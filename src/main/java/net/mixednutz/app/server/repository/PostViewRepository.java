package net.mixednutz.app.server.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;

import net.mixednutz.app.server.entity.post.AbstractPostView;
import net.mixednutz.app.server.entity.post.AbstractPostView.ViewPK;

@NoRepositoryBean
public interface PostViewRepository<P extends AbstractPostView> extends CrudRepository<P, ViewPK> {

}
