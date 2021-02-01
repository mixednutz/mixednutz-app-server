package net.mixednutz.app.server.repository.impl;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;

import net.mixednutz.app.server.entity.post.AbstractNotification;
import net.mixednutz.app.server.repository.PostNotificationCustomRepository;

public class PostNotificationCustomRepositoryImpl implements PostNotificationCustomRepository {

	@Autowired
    private EntityManager entityManager;

	@Override
	public <N extends AbstractNotification> Iterable<N> loadNotifications(CriteriaBuilderCallback<N> callback, Class<N> entityType) {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<N> criteriaQuery = criteriaBuilder.createQuery(entityType);
		Root<N> itemRoot = criteriaQuery.from(entityType);
		Predicate predicate = callback.withCriteriaBuilder(criteriaBuilder, itemRoot);
		criteriaQuery.where(predicate);
		return entityManager.createQuery(criteriaQuery).getResultList();
	}
		
}
