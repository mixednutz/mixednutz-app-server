package net.mixednutz.app.server.manager;

import net.mixednutz.app.server.entity.SiteSettings;
import net.mixednutz.app.server.entity.User;

public interface SiteSettingsManager {

	public SiteSettings getSiteSettings();
	
	public SiteSettings createSiteSettings(User adminUser);
	
	public SiteSettings save(SiteSettings siteSettings);
	
}
