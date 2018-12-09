package net.mixednutz.app.server;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.social.connect.ConnectionFactory;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.connect.UsersConnectionRepository;
import org.springframework.social.connect.mem.InMemoryUsersConnectionRepository;
import org.springframework.social.connect.support.ConnectionFactoryRegistry;

import net.mixednutz.api.core.provider.ApiProviderRegistry;
import net.mixednutz.api.provider.ApiProvider;
import net.mixednutz.app.server.controller.web.NewExternalCredentialsController;
import net.mixednutz.app.server.repository.ExternalCredentialsRepository;
import net.mixednutz.app.server.repository.ExternalFeedRepository;

@Configuration
@ConfigurationProperties(prefix="mixednutz.social")
@ComponentScan(basePackages={"net.mixednutz.api"})
public class SocialConfig {

	private ConnectionFactoryRegistry registry;
	
	@Autowired
	@Bean
	public NewExternalCredentialsController.NewExternalCredentialsCallback externalCredentialsCallback(
			ExternalCredentialsRepository credentialsRepository,
			ApiProviderRegistry apiProviderRegistry, 
			ExternalFeedRepository externalFeedRepository) {
		return new NewExternalCredentialsController.NewExternalCredentialsCallback(
				credentialsRepository,
				apiProviderRegistry,
				externalFeedRepository);
	}
	
	/**
	 * In order for a external feed to be registered, there must be a 
	 * Configuration class in net.mixednutz.api that registers a ConnectionFactory
	 * bean.
	 * 
	 * @param connectionFactories
	 * @return
	 */
	@Autowired
	@Bean
	public ConnectionFactoryLocator connectionFactoryLocator(List<ConnectionFactory<?>> connectionFactories) {
		registry = new ConnectionFactoryRegistry();
		for (ConnectionFactory<?> connectionFactory: connectionFactories) {
			registry.addConnectionFactory(connectionFactory);
		}
	    return registry;
	}
	
	@Bean
	public ApiProviderRegistry socialManagerRegistry(List<ApiProvider<?,?>> socialNetworkClients) {
		ApiProviderRegistry registry = new ApiProviderRegistry();
		for (ApiProvider<?,?> socialNetworkClient: socialNetworkClients) {
			registry.addSocialNetworkClient(socialNetworkClient);
		}
		return registry;
	}
	
	@Autowired
	@Bean
    public UsersConnectionRepository usersConnectionRepository() {
        return new InMemoryUsersConnectionRepository(registry);
    }
	
	@Bean
    @Scope(value="request", proxyMode=ScopedProxyMode.INTERFACES)
    public ConnectionRepository connectionRepository() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new IllegalStateException("Unable to get a ConnectionRepository: no user signed in");
        }
        return usersConnectionRepository().createConnectionRepository(authentication.getName());
    }
		
}
