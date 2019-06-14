package net.mixednutz.app.server;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import net.mixednutz.api.core.model.NetworkInfo;

@Configuration
@ConfigurationProperties(prefix="mixednutz.network-info")
public class NetworkInfoConfig {

	private String id;
	private String hostName;
	private String displayName;
	
	@Bean
	public NetworkInfo networkInfo() {
		NetworkInfo networkInfo = new NetworkInfo();
		networkInfo.setId(id);
		networkInfo.setHostName(hostName);
		networkInfo.setDisplayName(displayName);
		networkInfo.setBaseUrl("https://"+hostName);
		return networkInfo;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getHostName() {
		return hostName;
	}
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

}
