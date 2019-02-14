package net.mixednutz.app.server.manager.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import net.mixednutz.app.server.entity.SiteSettings;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.manager.SiteSettingsManager;
import net.mixednutz.app.server.repository.SiteSettingsRepository;

@Service
public class SiteSettingsManagerImpl implements SiteSettingsManager {
	
	@Autowired
	SiteSettingsRepository siteSettingsRepository;
	
	@Value("${site.settings.uniqueId}")
	private long settingsUniqueId;

	@Cacheable("siteSettings")
	public SiteSettings getSiteSettings() {
		return siteSettingsRepository.findById(settingsUniqueId).get();
	}
	
	public SiteSettings createSiteSettings(User adminUser) {
		return new SiteSettings(settingsUniqueId, adminUser);
	}

	@CacheEvict(value="siteSettings", allEntries=true)
	public SiteSettings save(SiteSettings siteSettings) {
		return siteSettingsRepository.save(siteSettings);
	}
}
