package net.mixednutz.app.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;

import net.mixednutz.app.server.controller.exception.ResourceNotFoundException;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.ExternalFeeds.AbstractFeed;
import net.mixednutz.app.server.entity.ExternalFeeds.Oauth1AuthenticatedFeed;
import net.mixednutz.app.server.entity.ExternalFeeds.Oauth2AuthenticatedFeed;
import net.mixednutz.app.server.repository.ExternalCredentialsRepository;
import net.mixednutz.app.server.repository.ExternalFeedRepository;

public class BaseExternalCredentialsController {
	
	@Autowired
	private ExternalCredentialsRepository credentialsRepository;
	
	@Autowired
	private ExternalFeedRepository externalFeedRepository;
	
	protected void delete(Long feedId, User user) {
		AbstractFeed externalFeed = externalFeedRepository.findById(feedId).orElseThrow(()->{
			return new ResourceNotFoundException("");
		});
		if (!externalFeed.getUser().equals(user)) {
			throw new AccessDeniedException("Feed #"+feedId+" - That's not yours to edit!");
		}
		externalFeedRepository.delete(externalFeed);
		if (externalFeed instanceof Oauth1AuthenticatedFeed) {
			Oauth1AuthenticatedFeed auth1Feed = (Oauth1AuthenticatedFeed)externalFeed;
			credentialsRepository.delete(auth1Feed.getCredentials());
		}
		if (externalFeed instanceof Oauth2AuthenticatedFeed) {
			Oauth2AuthenticatedFeed auth2Feed = (Oauth2AuthenticatedFeed)externalFeed;
			credentialsRepository.delete(auth2Feed.getCredentials());
		}
	}

}
