package net.mixednutz.app.server.controller.api;

import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import net.mixednutz.app.server.controller.exception.ResourceNotFoundException;

@Controller
@RequestMapping(OembedController.OEMBED_DIR)
public class OembedController {
	
	public static final String OEMBED_DIR = "/photos-storage";
	
	@RequestMapping(method = RequestMethod.GET, params="format=xml")
	public @ResponseBody Oembed oembedXml() {
		throw new UnsupportedOperationException("We don't support the XML oembed format.  Please use format=json instead.");
	}
	
	@RequestMapping(method = RequestMethod.GET, params="format!=xml")
	public @ResponseBody Oembed oembedJson(
			@RequestParam String url,
			@RequestParam(defaultValue="0") Integer maxwidth,
			@RequestParam(defaultValue="0") Integer maxheight,
			@RequestParam(defaultValue="json") String format,
			WebRequest request, Authentication auth) {
		URI uri = null;
		try {
			uri = new URI(url);
		} catch (URISyntaxException e) {
			throw new ResourceNotFoundException("Bad URL: "+url, e);
		}
		
		String contextPath = request.getContextPath();
		String path = uri.getPath();
		if (path.startsWith(contextPath)) {
			path = path.substring(contextPath.length());
		}
		Oembed oembed = null;
		//TODO Implement this!
//		oembed = this.getMsgthread(path, maxwidth, maxheight);
//		if (oembed==null) oembed = this.getJournal(path, maxwidth, maxheight, auth);
//		if (oembed==null) oembed = this.getAlbum(path, maxwidth, maxheight, auth);
//		if (oembed==null) oembed = this.getPhoto(path, maxwidth, maxheight, auth);
//		if (oembed==null) oembed = this.getPoll(path, maxwidth, maxheight, auth);
//		if (oembed==null) oembed = this.getExternalContent(url, maxwidth, maxheight);
		
//		if (oembed==null) {
//			throw new ResourceNotFoundException("Unable to find resource: "+url);
//		}
		return oembed;
	}
	
	
	@JsonTypeInfo(  
		    use = JsonTypeInfo.Id.NAME,  
		    include = JsonTypeInfo.As.PROPERTY,  
		    property = "type")  
		@JsonSubTypes({  
			@Type(value = OembedPhoto.class, name = "photo"),
			@Type(value = OembedVideo.class, name = "video"),
		    @Type(value = OembedLink.class, name = "link"),
		    @Type(value = OembedRich.class, name = "rich")}) 
	@JsonInclude(Include.NON_NULL)
	@JsonIgnoreProperties(ignoreUnknown=true)
	public abstract static class Oembed {
		private String type;
		private String version="1.0";
		private String providerName="MixedNutz.net";
		private String providerUrl="https://MixedNutz.net";
		private String title;
		private String authorName;
		private String thumbnailUrl;
		private Integer thumbnailWidth;
		private Integer thumbnailHeight;
		
		public Oembed() {
			super();
		}
		public Oembed(String type) {
			super();
			this.type = type;
		}
		/**
		 * The resource type. Valid values, along with value-specific parameters, are described below.
		 * @return
		 */
		@JsonIgnore
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
		/**
		 * The oEmbed version number. This must be 1.0.
		 * @return
		 */
		public String getVersion() {
			return version;
		}
		public void setVersion(String version) {
			this.version = version;
		}
		/**
		 * The name of the resource provider.
		 * @return
		 */
		@JsonProperty("provider_name")
		public String getProviderName() {
			return providerName;
		}
		public void setProviderName(String providerName) {
			this.providerName = providerName;
		}
		/**
		 * The url of the resource provider.
		 * @return
		 */
		@JsonProperty("provider_url")
		public String getProviderUrl() {
			return providerUrl;
		}
		public void setProviderUrl(String providerUrl) {
			this.providerUrl = providerUrl;
		}
		/**
		 * A text title, describing the resource.
		 * @return
		 */
		public String getTitle() {
			return title;
		}
		public void setTitle(String title) {
			this.title = title;
		}
		/**
		 * The name of the author/owner of the resource.
		 * @return
		 */
		@JsonProperty("author_name")
		public String getAuthorName() {
			return authorName;
		}
		public void setAuthorName(String authorName) {
			this.authorName = authorName;
		}
		
		@JsonProperty("thumbnail_url")
		public String getThumbnailUrl() {
			return thumbnailUrl;
		}
		public void setThumbnailUrl(String thumbnailUrl) {
			this.thumbnailUrl = thumbnailUrl;
		}
		
		@JsonProperty("thumbnail_width")
		public Integer getThumbnailWidth() {
			return thumbnailWidth;
		}
		public void setThumbnailWidth(Integer thumbnailWidth) {
			this.thumbnailWidth = thumbnailWidth;
		}
		
		@JsonProperty("thumbnail_height")
		public Integer getThumbnailHeight() {
			return thumbnailHeight;
		}
		public void setThumbnailHeight(Integer thumbnailHeight) {
			this.thumbnailHeight = thumbnailHeight;
		}	
	}
	
	/**
	 * This type is used for representing static photos.
	 */
	public static class OembedPhoto extends Oembed {
		private String url;
		private int width;
		private int height;
			
		public OembedPhoto() {
			super("photo");
		}
		/**
		 * The source URL of the image. Consumers should be able to insert this URL into an <img> element. Only HTTP and HTTPS URLs are valid.
		 * @return
		 */
		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}
		/**
		 * The width in pixels of the image specified in the url parameter.
		 * @return
		 */
		public int getWidth() {
			return width;
		}

		public void setWidth(int width) {
			this.width = width;
		}
		/**
		 * The height in pixels of the image specified in the url parameter.
		 * @return
		 */
		public int getHeight() {
			return height;
		}

		public void setHeight(int height) {
			this.height = height;
		}			
	}
	
	/**
	 * This type is used for representing playable videos.
	 */
	public static class OembedVideo extends Oembed {
		private String html;
		private int width;
		private int height;
		
		public OembedVideo() {
			super("video");
		}
		/**
		 * The HTML required to embed a video player. The HTML should have no padding or margins. Consumers may wish to load the HTML in an off-domain iframe to avoid XSS vulnerabilities.
		 * @return
		 */
		public String getHtml() {
			return html;
		}

		public void setHtml(String html) {
			this.html = html;
		}
		/**
		 * The width in pixels required to display the HTML.
		 * @return
		 */
		public int getWidth() {
			return width;
		}

		public void setWidth(int width) {
			this.width = width;
		}
		/**
		 * The height in pixels required to display the HTML.
		 * @return
		 */
		public int getHeight() {
			return height;
		}

		public void setHeight(int height) {
			this.height = height;
		}
		
	}
	
	/**
	 * Responses of this type allow a provider to return any generic embed data (such as title and author_name), without providing either the url or html parameters. The consumer may then link to the resource, using the URL specified in the original request.
	 */
	public static class OembedLink extends Oembed {
		
		public OembedLink() {
			super("link");
		}
		
	}
	
	/**
	 * This type is used for rich HTML content that does not fall under one of the other categories.
	 */
	public static class OembedRich extends Oembed {
		private String html;
		private int width;
		private int height;
		
		public OembedRich() {
			super("rich");
		}

		/**
		 * The HTML required to embed a video player. The HTML should have no padding or margins. Consumers may wish to load the HTML in an off-domain iframe to avoid XSS vulnerabilities.
		 * @return
		 */
		public String getHtml() {
			return html;
		}

		public void setHtml(String html) {
			this.html = html;
		}
		/**
		 * The width in pixels required to display the HTML.
		 * @return
		 */
		public int getWidth() {
			return width;
		}

		public void setWidth(int width) {
			this.width = width;
		}
		/**
		 * The height in pixels required to display the HTML.
		 * @return
		 */
		public int getHeight() {
			return height;
		}

		public void setHeight(int height) {
			this.height = height;
		}
	}

}
