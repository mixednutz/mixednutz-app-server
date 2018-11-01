package net.mixednutz.app.server.domain;

import org.springframework.social.connect.ConnectionData;

public interface Credentials {

	public String getProviderId();
	
	public void setProviderId(String providerId);
		
	public ConnectionData createConnectionData();
}
