package net.mixednutz.app.server.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="x_whitelist")
public class OembedFilterAllowlist {
	
	private String name;
	private String description;
	private String urlPattern;
	private String oembedUrlPattern;
	private String oembedUrl;
	
	public OembedFilterAllowlist() {
		super();
	}

	public OembedFilterAllowlist(String name, String description, 
			String urlPattern, String oembedUrlPattern, 
			String oembedUrl) {
		this.name = name;
		this.description = description;
		this.urlPattern = urlPattern;
		this.oembedUrlPattern = oembedUrlPattern;
		this.oembedUrl = oembedUrl;
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
	
}
