package net.mixednutz.app.server.manager;

import java.util.Optional;

import net.mixednutz.app.server.entity.Oembeds;


public interface ExternalContentManager {
	
	ExtractedMetadata lookupMetadata(String sourceId);
	
	boolean isAllowListed(String sourceId);

	Optional<String> deriveSourceType(String sourceId);
	
	public Oembeds.Oembed toOembed(ExtractedMetadata xcontent, Integer maxwidth,
			Integer maxheight);
	
	Optional<Oembeds.Oembed> toOembed(String sourceType, String sourceId, Integer maxwidth,
			Integer maxheight);
	
	public class ExtractedMetadata {
		protected String url;
		private String type;
		private String summary;
		private int statusCode;
		private String siteName;
		private String title;
		private String description;
		private String contentType;
		private String imageUrl;
		private String oembedUrl;
		private String oembedTitle;
		
		public String getUrl() {
			return url;
		}
		public void setUrl(String url) {
			this.url = url;
		}
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
		public String getSummary() {
			return summary;
		}
		public void setSummary(String summary) {
			this.summary = summary;
		}
		public int getStatusCode() {
			return statusCode;
		}
		public void setStatusCode(int statusCode) {
			this.statusCode = statusCode;
		}
		public String getSiteName() {
			return siteName;
		}
		public void setSiteName(String siteName) {
			this.siteName = siteName;
		}
		public String getTitle() {
			return title;
		}
		public void setTitle(String title) {
			this.title = title;
		}
		public String getDescription() {
			return description;
		}
		public void setDescription(String description) {
			this.description = description;
		}
		public String getContentType() {
			return contentType;
		}
		public void setContentType(String contentType) {
			this.contentType = contentType;
		}
		public String getImageUrl() {
			return imageUrl;
		}
		public void setImageUrl(String imageUrl) {
			this.imageUrl = imageUrl;
		}
		public String getOembedUrl() {
			return oembedUrl;
		}
		public void setOembedUrl(String oembedUrl) {
			this.oembedUrl = oembedUrl;
		}
		public String getOembedTitle() {
			return oembedTitle;
		}
		public void setOembedTitle(String oembedTitle) {
			this.oembedTitle = oembedTitle;
		}
	}
	
}
