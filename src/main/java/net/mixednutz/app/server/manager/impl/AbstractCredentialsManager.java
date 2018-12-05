package net.mixednutz.app.server.manager.impl;

import org.springframework.data.repository.CrudRepository;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionData;
import org.springframework.social.connect.ConnectionFactory;
import org.springframework.social.connect.ConnectionFactoryLocator;

import net.mixednutz.app.server.entity.AbstractCredentials;
import net.mixednutz.app.server.entity.ExternalCredentials.Oauth2Credentials;

public class AbstractCredentialsManager<C extends AbstractCredentials> {

	private ConnectionFactoryLocator connectionFactoryLocator;
	
	protected CrudRepository<C, ?> credentialsRepository;
	
	protected Connection<?> getConnection(C creds) {
		if (creds instanceof Oauth2Credentials) {
			Oauth2Credentials oauth2Creds = (Oauth2Credentials) creds;
			
			ConnectionFactory<?> connectionFactory = connectionFactoryLocator
					.getConnectionFactory(creds.getProviderId());
			return connectionFactory.createConnection(oauth2Creds.createConnectionData());
		}
		return null;
	}

	public C refresh(C creds) {
		Connection<?> connection = getConnection(creds);
		if (connection!=null && connection.hasExpired()) {
			connection.refresh();
			if (!connection.hasExpired()) {
				creds = updateConnectionData(creds, 
						connection.createData());
			}
		}
		return creds;
	}
	
	public Connection<?> connectAndRefresh(C creds) {
		Connection<?> connection = getConnection(creds);
		if (connection!=null && connection.hasExpired()) {
			connection.refresh();
			if (!connection.hasExpired()) {
				creds = updateConnectionData(creds, 
						connection.createData());
			}
		}
		return connection;
	}

	public C updateConnectionData(C creds,
			ConnectionData cd) {
		if (creds instanceof Oauth2Credentials) {
			Oauth2Credentials oauth2 = (Oauth2Credentials) creds;
			oauth2.setAuthCode(cd.getAccessToken());
			oauth2.setExpireTime(cd.getExpireTime());
			if (cd.getRefreshToken()!=null) {
				oauth2.setRefreshToken(cd.getRefreshToken());
			}
		}
		return (C) credentialsRepository.save(creds);
	}
	
	public void setAbstractCredentialsRepository(CrudRepository<C, ?> credentialsRepository) {
		this.credentialsRepository = credentialsRepository; 
	}

	public void setConnectionFactoryLocator(
			ConnectionFactoryLocator connectionFactoryLocator) {
		this.connectionFactoryLocator = connectionFactoryLocator;
	}
	
}
