package net.mixednutz.app.server.manager.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.mixednutz.app.server.entity.ExternalCredentials.ExternalAccountCredentials;
import net.mixednutz.app.server.manager.ExternalAccountCredentialsManager;
import net.mixednutz.app.server.repository.ExternalCredentialsRepository;


@Service
@Transactional
public class ExternalAccountCredentialsManagerImpl extends AbstractCredentialsManager<ExternalAccountCredentials> 
	implements ExternalAccountCredentialsManager {
	
	
	@SuppressWarnings("unchecked")
	@Override
	public <C extends ExternalAccountCredentials> C refresh(C creds) {
		return (C) super.refresh(creds);
	}

	@Autowired
	public void setDao(ExternalCredentialsRepository repository) {
		super.setAbstractCredentialsRepository(repository);
	}

	@Autowired
	public void setConnectionFactoryLocator(
			ConnectionFactoryLocator connectionFactoryLocator) {
		super.setConnectionFactoryLocator(connectionFactoryLocator);
	}
	
}
