package net.mixednutz.app.server;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import net.mixednutz.api.nodeinfo.server.NodeinfoSchemaImpl;
import software.diaspora.nodeinfo.server.NodeinfoSchema;

@Configuration
@ConfigurationProperties(prefix="nodeinfo")
public class NodeInfoConfig {
	
	//injected from config properties
	private Software software = new Software();
	private boolean openRegistrations;
	
	private boolean twitterEnabled;
	private boolean rssEnabled;
	
	@Autowired
	MessageSource messageSource;
	
	
	private void setup() {
		
		software.version = messageSource.getMessage("version", null, Locale.getDefault());
		try {
			Class.forName("net.mixednutz.api.twitter.TwitterFeedType",false, getClass().getClassLoader());
			twitterEnabled=true;
		} catch (ClassNotFoundException e) {twitterEnabled=false;}
		
		try {
			Class.forName("net.mixednutz.app.server.controller.rss.RssTimelineController",false, getClass().getClassLoader());
			rssEnabled=true;
		} catch (ClassNotFoundException e) {rssEnabled=false;}
		
	}
	
	@Bean
	public NodeinfoSchema schema() {
		
		setup();
		
		NodeinfoSchemaImpl schema = new NodeinfoSchemaImpl();
		schema.getSoftware().put("version",software.version);
		if (software.repository!=null) {
			schema.getSoftware().put("repository",software.repository);
		}
		if (twitterEnabled) {
			schema.getServices().get("inbound").add("twitter");
			schema.getServices().get("outbound").add("twitter");
		}
		if (rssEnabled) {
			schema.getServices().get("outbound").add("rss2.0");
		}
		schema.setOpenRegistrations(openRegistrations);
		return schema;
	}
	
	
	public Software getSoftware() {
		return software;
	}

	public void setSoftware(Software software) {
		this.software = software;
	}

	public boolean isOpenRegistrations() {
		return openRegistrations;
	}

	public void setOpenRegistrations(boolean openRegistrations) {
		this.openRegistrations = openRegistrations;
	}


	public static class Software {
		private String version;
		private String repository;
		
		public String getVersion() {
			return version;
		}
		public void setVersion(String version) {
			this.version = version;
		}
		public String getRepository() {
			return repository;
		}
		public void setRepository(String repository) {
			this.repository = repository;
		}
		
	}

}
