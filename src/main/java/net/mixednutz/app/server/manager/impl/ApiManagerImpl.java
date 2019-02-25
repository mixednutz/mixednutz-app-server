package net.mixednutz.app.server.manager.impl;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.mixednutz.api.core.model.AlternateLink;
import net.mixednutz.api.model.ITimelineElement;
import net.mixednutz.app.server.entity.InternalTimelineElement;
import net.mixednutz.app.server.entity.InternalTimelineElement.Type;
import net.mixednutz.app.server.entity.Journal;
import net.mixednutz.app.server.entity.NetworkInfo;
import net.mixednutz.app.server.entity.Post;
import net.mixednutz.app.server.manager.ApiManager;

@Service
public class ApiManagerImpl implements ApiManager{

	@Autowired
	private NetworkInfo networkInfo;
	
	private static final String APPLICATION_JSON_OEMBED = "application/json+oembed";
	
	@Override
	public ITimelineElement toTimelineElement(Journal entity) {
		InternalTimelineElement api = toTimelineElement((Post<?>)entity);
		api.setType(new Type("Journal",
				networkInfo.getHostName(),
				networkInfo.getId()+"_Journal"));
		api.setId(entity.getJournalId());
		api.setTitle(entity.getSubject());
		return api;
	}

	@Override
	public InternalTimelineElement toTimelineElement(Post<?> entity) {
		InternalTimelineElement api = new InternalTimelineElement();
		api.setUri(entity.getUri());
		api.setUrl(networkInfo.getBaseUrl()+entity.getUri());
		api.setPostedByUser(entity.getAuthor());
		api.setPostedOnDate(entity.getDateCreated());
		api.setDescription(entity.getDescription());
		api.setAlternateLinks(new ArrayList<>());
		api.getAlternateLinks().add(new AlternateLink(
				networkInfo.getOembedBaseUrl()+"?url="+api.getUrl(), APPLICATION_JSON_OEMBED));
		return api;
	}

}
