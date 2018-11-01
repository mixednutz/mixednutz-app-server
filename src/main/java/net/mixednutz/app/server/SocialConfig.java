package net.mixednutz.app.server;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.connect.UsersConnectionRepository;
import org.springframework.social.connect.mem.InMemoryUsersConnectionRepository;
import org.springframework.social.connect.support.ConnectionFactoryRegistry;
import org.springframework.social.mixednutz.v1_9.connect.MixednutzConnectionFactory;

@Configuration
@ConfigurationProperties(prefix="mixednutz.social")
public class SocialConfig {

	private MixednutzConnectionProperties mixednutz = new MixednutzConnectionProperties();
		
	@Bean
	public MixednutzConnectionFactory mixednutzConnectionFactory() {
		MixednutzConnectionFactory mcf = new MixednutzConnectionFactory(
				mixednutz.baseUrl, mixednutz.clientId, mixednutz.clientSecret);
		return mcf;
	}
	
	@Bean
	public ConnectionFactoryLocator connectionFactoryLocator() {
		ConnectionFactoryRegistry registry = new ConnectionFactoryRegistry();
		registry.addConnectionFactory(mixednutzConnectionFactory());
//	    registry.addConnectionFactory(googleConnectionFactory());
//	    registry.addConnectionFactory(gdataConnectionFactory());
//	    registry.addConnectionFactory(facebookConnectionFactory());
//	    registry.addConnectionFactory(instagramConnectionFactory());
//	    registry.addConnectionFactory(twitterConnectionFactory());
	    return registry;
	}
	
	@Bean
    public UsersConnectionRepository usersConnectionRepository() {
        return new InMemoryUsersConnectionRepository(connectionFactoryLocator());
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
	
	public MixednutzConnectionProperties getMixednutz() {
		return mixednutz;
	}

	public void setMixednutz(MixednutzConnectionProperties mixednutz) {
		this.mixednutz = mixednutz;
	}

	public static class MixednutzConnectionProperties {
		private String baseUrl;
		private String clientId;
		private String clientSecret;
		
		public String getBaseUrl() {
			return baseUrl;
		}
		public void setBaseUrl(String baseUrl) {
			this.baseUrl = baseUrl;
		}
		public String getClientId() {
			return clientId;
		}
		public void setClientId(String clientId) {
			this.clientId = clientId;
		}
		public String getClientSecret() {
			return clientSecret;
		}
		public void setClientSecret(String clientSecret) {
			this.clientSecret = clientSecret;
		}
		
	}
	
}
