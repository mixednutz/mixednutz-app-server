package net.mixednutz.app.server.manager.post.journal.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import net.mixednutz.api.core.model.NetworkInfo;
import net.mixednutz.app.server.entity.InternalTimelineElement;
import net.mixednutz.app.server.entity.InternalTimelineElement.Type;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.post.journal.Journal;
import net.mixednutz.app.server.manager.ApiElementConverter;

@Component
public class JournalEntityConverter implements ApiElementConverter<Journal> {
	
	@Autowired
	private NetworkInfo networkInfo;
	
	@Override
	public InternalTimelineElement toTimelineElement(
			InternalTimelineElement api, Journal entity, User viewer) {
		api.setType(new Type("Journal",
				networkInfo.getHostName(),
				networkInfo.getId()+"_Journal"));
		api.setId(entity.getId());
		api.setTitle(entity.getSubject());
		return api;
	}

	@Override
	public boolean canConvert(Class<?> entityClazz) {
		return Journal.class.isAssignableFrom(entityClazz);
	}

}
