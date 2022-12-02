package net.mixednutz.app.server;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.w3c.activitystreams.model.BaseObjectOrLink;

@Configuration
@ComponentScan("org.w3c.activitypub")
public class ActivityPubConfig {
	
	@Bean
	public ActivityPubUtils activityPubUtils() {
		return new ActivityPubUtils();
	}
	
	public class ActivityPubUtils {
		
		public void initRoot(BaseObjectOrLink root) {
			root.set_Context(BaseObjectOrLink.CONTEXT);
		}
		
	}
	
}
