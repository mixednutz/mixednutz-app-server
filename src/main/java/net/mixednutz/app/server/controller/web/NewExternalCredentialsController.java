package net.mixednutz.app.server.controller.web;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionFactory;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.connect.UserProfile;
import org.springframework.social.connect.web.ConnectInterceptor;
import org.springframework.social.connect.web.CredentialsCallback;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.SessionAttributes;

import net.mixednutz.api.core.provider.ApiProviderRegistry;
import net.mixednutz.api.model.INetworkInfoSmall;
import net.mixednutz.api.provider.ApiProvider;
import net.mixednutz.api.provider.ICredentials;
import net.mixednutz.api.provider.IOauth1Credentials;
import net.mixednutz.api.provider.IOauth2Credentials;
import net.mixednutz.app.server.entity.ExternalCredentials.ExternalAccountCredentials;
import net.mixednutz.app.server.entity.ExternalCredentials.Oauth1Credentials;
import net.mixednutz.app.server.entity.ExternalCredentials.Oauth2Credentials;
import net.mixednutz.app.server.entity.ExternalFeeds.AbstractAuthenticatedFeed;
import net.mixednutz.app.server.entity.ExternalFeeds.Oauth1AuthenticatedFeed;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.repository.ExternalCredentialsRepository;
import net.mixednutz.app.server.repository.ExternalFeedRepository;


@Controller
@SessionAttributes("credentials")
public class NewExternalCredentialsController {
		
	
	public static class NewExternalCredentialsCallback implements CredentialsCallback {
		
		private ExternalCredentialsRepository credentialsRepository;
		private ApiProviderRegistry apiProviderRegistry;
		private ExternalFeedRepository externalFeedRepository;
		
		public NewExternalCredentialsCallback(ExternalCredentialsRepository credentialsRepository,
				ApiProviderRegistry apiProviderRegistry, ExternalFeedRepository externalFeedRepository) {
			super();
			this.credentialsRepository = credentialsRepository;
			this.apiProviderRegistry = apiProviderRegistry;
			this.externalFeedRepository = externalFeedRepository;
		}

		@Override
		public ICredentials instantiate(ConnectionFactory<?> connectionFactory) {
			ExternalAccountCredentials creds = null;
			
			ApiProvider<?,?> provider = apiProviderRegistry.getSocialNetworkClient(connectionFactory.getProviderId());
			if (provider==null) {
				throw new RuntimeException("Unknown provider: "+connectionFactory.getProviderId());
			}
			if (provider.getCredentialsInterface().isAssignableFrom(Oauth2Credentials.class)) {
				creds = new Oauth2Credentials();
			} else if (provider.getCredentialsInterface().isAssignableFrom(Oauth1Credentials.class)) {
				creds = new Oauth1Credentials();
			} else {
				throw new RuntimeException("Credentials Type does not extend or implement either IOauth1Credentials or IOauth2Credentials: "+provider.getCredentialsInterface());
			}
			User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	    	creds.setUser(user);
			return creds;
		}
		
		@Override
		public void save(IOauth1Credentials creds, Connection<?> connection) {
			Oauth1Credentials oauth = (Oauth1Credentials) creds;
			oauth = credentialsRepository.save(oauth);

			AbstractAuthenticatedFeed<?> feed = createExternalFeed(connection, oauth);
			externalFeedRepository.save(feed);
		}

		@Override
		public void save(IOauth2Credentials creds, Connection<?> connection) {
			// TODO Auto-generated method stub
		}

		AbstractAuthenticatedFeed<?> createExternalFeed(Connection<?> connection, Oauth1Credentials creds) {
			INetworkInfoSmall networkInfo = apiProviderRegistry.getSocialNetworkClient(creds.getProviderId())
					.getNetworkInfo();
			UserProfile userProfile = connection.fetchUserProfile();

			Oauth1AuthenticatedFeed feed = new Oauth1AuthenticatedFeed();
			feed.setUser(creds.getUser());
			feed.setCredentials((Oauth1Credentials) creds);
			feed.setImageUrl(connection.getImageUrl());
			feed.setName(networkInfo.getDisplayName() + " - " + userProfile.getUsername());
			return feed;
		}
	}
	
	
	@Controller
	public static class ConnectController extends org.springframework.social.connect.web.ConnectController {

		@Autowired
		private List<ConnectInterceptor<?>> connectInterceptors;
		
		@Autowired
		public ConnectController(
				ConnectionFactoryLocator connectionFactoryLocator,
				ConnectionRepository connectionRepository) {
			super(connectionFactoryLocator, connectionRepository);
		}
		
		@Override
		protected String connectedView(String providerId) {
			return "redirect:/main";
		}

		@PostConstruct
		public void addInterceptors() {
			for (ConnectInterceptor<?> connectInterceptor: this.connectInterceptors) {
				this.addInterceptor(connectInterceptor);
			}
		}
		
	}
		
	
}
