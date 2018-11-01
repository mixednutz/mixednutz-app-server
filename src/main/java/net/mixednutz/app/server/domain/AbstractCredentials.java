package net.mixednutz.app.server.domain;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import org.springframework.social.connect.ConnectionData;

@MappedSuperclass
public abstract class AbstractCredentials
	implements Credentials {

	private String providerId;
	
	public AbstractCredentials() {
		super();
	}

	@Column(name="type",insertable=false, updatable=false)
	public String getProviderId() {
		return providerId;
	}
	
	public void setProviderId(String type) {
		this.providerId = type;
	}
	
	public ConnectionData createConnectionData() {
		 throw new UnsupportedOperationException("Not implemented yet!");
	 }
}
