package net.mixednutz.app.server.manager;

import org.springframework.social.connect.Connection;

import net.mixednutz.app.server.entity.ExternalCredentials.ExternalAccountCredentials;

public interface ExternalAccountCredentialsManager {
	
	public ExternalAccountCredentials refresh(ExternalAccountCredentials creds);
	
	public Connection<?> connectAndRefresh(ExternalAccountCredentials creds);

}
