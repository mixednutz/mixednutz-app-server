package net.mixednutz.app.server.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import net.mixednutz.app.server.manager.ExternalContentManager.ExtractedMetadata;
import net.mixednutz.app.server.manager.impl.ExternalContentManagerImpl.ApiLookup;

@Entity
@Table(name="x_whitelist")
public class OembedFilterAllowlist {
	
	private String name;
	private String description;
	private String urlPattern;
	private String oembedUrlPattern;
	private String oembedUrl;
	private Class<?> customLookupClass;
	
	public OembedFilterAllowlist() {
		super();
	}

	public OembedFilterAllowlist(String name, String description, 
			String urlPattern, String oembedUrlPattern, 
			String oembedUrl, Class<? extends ApiLookup<ExtractedMetadata>> customLookupClass) {
		this.name = name;
		this.description = description;
		this.urlPattern = urlPattern;
		this.oembedUrlPattern = oembedUrlPattern;
		this.oembedUrl = oembedUrl;
		this.customLookupClass = customLookupClass;
	}
	
	public OembedFilterAllowlist(String name, String description, 
			String urlPattern, String oembedUrlPattern, 
			String oembedUrl) {
		this(name, description, urlPattern, oembedUrlPattern, oembedUrl, null);
	}
	
	@Id
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getUrlPattern() {
		return urlPattern;
	}
	public void setUrlPattern(String urlPattern) {
		this.urlPattern = urlPattern;
	}
	public String getOembedUrlPattern() {
		return oembedUrlPattern;
	}
	public void setOembedUrlPattern(String oembedUrlPattern) {
		this.oembedUrlPattern = oembedUrlPattern;
	}
	public String getOembedUrl() {
		return oembedUrl;
	}
	public void setOembedUrl(String oembedUrl) {
		this.oembedUrl = oembedUrl;
	}
	@SuppressWarnings("unchecked")
	public Class<? extends ApiLookup<ExtractedMetadata>> getCustomLookupClass() {
		return (Class<ApiLookup<ExtractedMetadata>>) customLookupClass;
	}
	public void setCustomLookupClass(Class<? extends ApiLookup<ExtractedMetadata>> customLookupClass) {
		this.customLookupClass = customLookupClass;
	}
	
}
