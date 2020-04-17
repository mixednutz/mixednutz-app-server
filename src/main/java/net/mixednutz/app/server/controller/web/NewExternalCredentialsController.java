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
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.view.RedirectView;

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
import net.mixednutz.app.server.entity.ExternalFeeds.Oauth2AuthenticatedFeed;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.entity.VisibilityType;
import net.mixednutz.app.server.manager.ExternalFeedManager;
import net.mixednutz.app.server.repository.ExternalCredentialsRepository;
import net.mixednutz.app.server.repository.ExternalFeedRepository;


@Controller
@SessionAttributes(NewExternalCredentialsController.CREDENTIALS_SESSION_NAME)
public class NewExternalCredentialsController {
	
	public static final String CREDENTIALS_SESSION_NAME = "newaccount";
			
	public static class NewExternalCredentialsCallback implements CredentialsCallback {
		
		private ExternalCredentialsRepository credentialsRepository;
		private ApiProviderRegistry apiProviderRegistry;
		private ExternalFeedRepository externalFeedRepository;
		private ExternalFeedManager feedManager;
		
		public NewExternalCredentialsCallback(ExternalCredentialsRepository credentialsRepository,
				ApiProviderRegistry apiProviderRegistry, ExternalFeedRepository externalFeedRepository,
				ExternalFeedManager feedManager) {
			super();
			this.credentialsRepository = credentialsRepository;
			this.apiProviderRegistry = apiProviderRegistry;
			this.externalFeedRepository = externalFeedRepository;
			this.feedManager = feedManager;
		}

		@Override
		public ICredentials instantiate(ConnectionFactory<?> connectionFactory, WebRequest request) {
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
			creds.setVisibility(VisibilityType.valueOf(request.getParameter("visibility")));
			User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	    	creds.setUser(user);
			return creds;
		}
		
		@Override
		public void save(IOauth1Credentials creds, Connection<?> connection) {
			Oauth1Credentials oauth = (Oauth1Credentials) creds;
			oauth = credentialsRepository.save(oauth);

			AbstractAuthenticatedFeed<?> feed = createExternalFeed(connection, oauth);
			feed = externalFeedRepository.save(feed);
			
			crawlNewFeed(feed);
		}

		@Override
		public void save(IOauth2Credentials creds, Connection<?> connection) {
			Oauth2Credentials oauth = (Oauth2Credentials) creds;
			oauth = credentialsRepository.save(oauth);
			
			AbstractAuthenticatedFeed<?> feed = createExternalFeed(connection, oauth);
			feed = externalFeedRepository.save(feed);
			
			crawlNewFeed(feed);
		}

		AbstractAuthenticatedFeed<?> createExternalFeed(Connection<?> connection, Oauth1Credentials creds) {
			INetworkInfoSmall networkInfo = apiProviderRegistry.getSocialNetworkClient(creds.getProviderId())
					.getNetworkInfo();
			UserProfile userProfile = connection.fetchUserProfile();

			Oauth1AuthenticatedFeed feed = new Oauth1AuthenticatedFeed();
			feed.setProviderId(creds.getProviderId());
			feed.setUser(creds.getUser());
			feed.setCredentials((Oauth1Credentials) creds);
			feed.setImageUrl(connection.getImageUrl());
			feed.setUsername(userProfile.getUsername());			
			feed.setName(networkInfo.getDisplayName() + " - " + userProfile.getUsername());
			feed.setVisibility(creds.getVisibility());
			return feed;
		}
		AbstractAuthenticatedFeed<?> createExternalFeed(Connection<?> connection, Oauth2Credentials creds) {
			INetworkInfoSmall networkInfo = apiProviderRegistry.getSocialNetworkClient(creds.getProviderId())
					.getNetworkInfo();
			UserProfile userProfile = connection.fetchUserProfile();

			Oauth2AuthenticatedFeed feed = new Oauth2AuthenticatedFeed();
			feed.setProviderId(creds.getProviderId());
			feed.setUser(creds.getUser());
			feed.setCredentials((Oauth2Credentials) creds);
			feed.setImageUrl(connection.getImageUrl());
			feed.setUsername(userProfile.getUsername());
			feed.setName(networkInfo.getDisplayName() + " - " + userProfile.getUsername());
			feed.setVisibility(creds.getVisibility());
			return feed;
		}
		void crawlNewFeed(AbstractAuthenticatedFeed<?> feed) {
			try {
				if (!VisibilityType.PRIVATE.equals(feed.getVisibility())) {
					feedManager.pollTimeline(feed);
					feedManager.pollUserTimeline(feed);
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw e;
			}
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

		@Override
		protected RedirectView connectionStatusRedirect(String providerId, NativeWebRequest request) {
			return new RedirectView("/main", true);
		}

		@PostConstruct
		public void addInterceptors() {
			for (ConnectInterceptor<?> connectInterceptor: this.connectInterceptors) {
				this.addInterceptor(connectInterceptor);
			}
		}
		
	}
		
	
}
