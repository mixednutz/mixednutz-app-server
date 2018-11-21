package net.mixednutz.app.server.entity;

import javax.persistence.MappedSuperclass;

import net.mixednutz.api.provider.ICredentials;

@MappedSuperclass
public abstract class AbstractCredentials
	implements ICredentials {

	private String providerId;
	
	public AbstractCredentials() {
		super();
	}

	public String getProviderId() {
		return providerId;
	}
	
	public void setProviderId(String type) {
		this.providerId = type;
	}
	
}
