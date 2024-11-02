package net.mixednutz.app.server.manager.impl;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.social.support.URIBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.ServletWebRequest;

import net.mixednutz.app.server.controller.api.OembedController;
import net.mixednutz.app.server.entity.OembedFilterAllowlist;
import net.mixednutz.app.server.entity.Oembeds;
import net.mixednutz.app.server.entity.Oembeds.Oembed;
import net.mixednutz.app.server.manager.ExternalContentManager;
import net.mixednutz.app.server.repository.OembedFilterAllowlistRepository;

@Service
public class ExternalContentManagerImpl implements ExternalContentManager {
	
	private static final Logger LOG = LoggerFactory.getLogger(ExternalContentManagerImpl.class);
		
	private final OembedFilterAllowlistRepository oembedFilterWhitelistRepository;
	
	private final RestTemplate restTemplate;

	private Map<String, ApiLookup<?>> lookupMap;
	private Map<String, Pattern> patternMap;
	private Map<String, ApiLookup<ExtractedMetadata>> urlLookups;
	private RestTemplateUrlLookup urlLookup;
	
	@Autowired
	HttpServletRequest request;
	
	@Autowired
	OembedController oembedController;
	
	private List<ApiLookup<ExtractedMetadata>> customApiLookups;
	
	@Autowired
	public ExternalContentManagerImpl(OembedFilterAllowlistRepository oembedFilterWhitelistRepository,
			Optional<RestTemplate> restTemplate, List<ApiLookup<ExtractedMetadata>> customApiLookups) {
		super();
		this.restTemplate = restTemplate.orElse(new RestTemplate());
		this.oembedFilterWhitelistRepository = oembedFilterWhitelistRepository;
		this.customApiLookups = customApiLookups;
		urlLookup = new RestTemplateUrlLookup();
	}
	
	private Optional<ApiLookup<ExtractedMetadata>> loadCustomClass(OembedFilterAllowlist whitelisted) {
		return this.customApiLookups.stream()
			.filter(instance->instance.getClass().equals(whitelisted.getCustomLookupClass()))
			.findFirst();
	}

	@PostConstruct
	public void loadWhitelist() {
		lookupMap = new HashMap<>();
		patternMap = new HashMap<>();
		urlLookups = new HashMap<>();
		
		for (OembedFilterAllowlist whitelisted: oembedFilterWhitelistRepository.findAll()) {
			try {
				this.lookupMap.put(whitelisted.getName(), new AllowlistLookup(whitelisted.getOembedUrl()));
				this.patternMap.put(whitelisted.getName(), 
						Pattern.compile(whitelisted.getUrlPattern(), Pattern.CASE_INSENSITIVE));
				if (whitelisted.getCustomLookupClass()!=null) {
					this.loadCustomClass(whitelisted).ifPresent(instance->this.urlLookups.put(whitelisted.getName(), 
							instance));
				} else {
					this.urlLookups.put(whitelisted.getName(),urlLookup);
				}
				LOG.debug("Registered Embed Whitelist Pattern for {} : {} {}",
						whitelisted.getDescription(), whitelisted.getUrlPattern(), whitelisted.getUrlPattern());
			} catch (PatternSyntaxException e) {
				LOG.warn("Unable to register Embed Whitelist Pattern for "+
						whitelisted.getName()+" : "+whitelisted.getUrlPattern(), e);
			}
			
			//This is for the pattern of the full oembed url
			try {
				this.lookupMap.put(whitelisted.getName()+"_oembed", new AllowlistOembedLookup());
				this.patternMap.put(whitelisted.getName()+"_oembed", 
						Pattern.compile(whitelisted.getOembedUrlPattern(), Pattern.CASE_INSENSITIVE));
				LOG.debug("Registered Oembed Whitelist Pattern for {} : {} {}",
						whitelisted.getDescription(), whitelisted.getUrlPattern(), whitelisted.getOembedUrlPattern());
			} catch (PatternSyntaxException e) {
				LOG.warn("Unable to register Oembed Whitelist Pattern for "+
						whitelisted.getName()+" : "+whitelisted.getOembedUrlPattern(), e);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	protected <Item> ApiLookup<Item> getLookup(String sourceType) {
		return (ApiLookup<Item>) lookupMap.get(sourceType);
	}
	
	@Override
	public boolean isAllowListed(String sourceId) {
		return deriveSourceType(sourceId).isPresent();
	}
	
	public Optional<String> deriveSourceType(String sourceId) {
		for (Entry<String, Pattern> entry: patternMap.entrySet()) {
			if (entry.getValue().matcher(sourceId).matches()) {
				return Optional.of(entry.getKey());
			}
		}
		return Optional.empty();
	}
	
	@Cacheable(value="externalContent")
	public Optional<ExtractedOembedHtml> lookupContent(String sourceType, String sourceId) {
				
		ApiLookup<?> apiLookup = this.getLookup(sourceType);
		LOG.debug("sourceType {} sourceId {}", sourceType, sourceId);
		
		Object content = apiLookup.lookupContent(sourceId);
		LOG.debug("content instanceof {}", content.getClass().getName());
		
		if (content instanceof Oembeds.Oembed) {
			ExtractedOembedHtml exstatus = new ExtractedOembedHtml();
			exstatus.setUrl(sourceId);
			exstatus.setStatusCode(200);
			exstatus.setOembed((Oembeds.Oembed) content);
			return Optional.of(exstatus);
		}
		
		return Optional.empty();
	}
	
	@Cacheable(value="externalMetaData")
	public ExtractedMetadata lookupMetadata(String sourceId) {
		Optional<ExtractedMetadata> content = deriveSourceType(sourceId)
				.<ApiLookup<ExtractedMetadata>>map(sourceType->urlLookups.get(sourceType))
				.<ExtractedMetadata>map(urlLookup->urlLookup.lookupContent(sourceId));
		
		return content.orElse(null);
	}
	
	/**
	 * For services that we've provided Oembed for
	 * 
	 * @param xcontent
	 * @param maxwidth
	 * @param maxheight
	 * @return
	 */
	public Oembeds.Oembed toOembed(ExtractedMetadata xcontent, Integer maxwidth,
			Integer maxheight) {

		Oembeds.Oembed oembed;
		if (xcontent.getContentType().startsWith("text/html")) {
			Oembeds.OembedRich rich = new Oembeds.OembedRich();
			if (xcontent.getImageUrl()!=null) {
				populateThumbnailProperties(rich, xcontent.getImageUrl(), maxwidth, maxheight);
			}
			oembed = rich;
			//Dont worry about the HTML.  It will be done by ApiManager
		} else if (xcontent.getContentType().startsWith("image/")) {
			Oembeds.OembedPhoto photo = new Oembeds.OembedPhoto();
			populatePhotoProperties(photo, xcontent.getUrl(), maxwidth, maxheight);
			oembed = photo;
		} else {
			Oembeds.OembedLink link = new Oembeds.OembedLink();
			oembed = link;
		}
		
		oembed.setTitle(xcontent.getTitle());
		oembed.setProviderName(xcontent.getSiteName());
		return oembed;

	}
	
	public Optional<Oembeds.Oembed> toOembed(String sourceType, String sourceId, Integer maxwidth,
			Integer maxheight) {
		ApiLookup<?> apiLookup = this.getLookup(sourceType);
		LOG.debug("sourceType {} sourceId {}", sourceType, sourceId);
		
		Object content = apiLookup.lookupContent(sourceId);
		LOG.debug("content instanceof {}", content.getClass().getName());
		
		if (content instanceof Oembeds.Oembed) {
			return Optional.of((Oembed) content);
		}
		
		return Optional.empty();
	}
	
	private void populateThumbnailProperties(Oembeds.Oembed oembed, String imageUrl,
			Integer maxwidth, Integer maxheight) {
		try {
			PhotoDimensions d = new PhotoDimensions(imageUrl)
					.resize(maxwidth, maxheight);
			oembed.setThumbnailUrl(imageUrl);
			oembed.setThumbnailHeight(d.height);
			oembed.setThumbnailWidth(d.width);
		} catch (Exception e) {
			LOG.warn("Unabled to get thumbnail properties for "+imageUrl, e);
		} 
	}
	
	
	private void populatePhotoProperties(Oembeds.OembedPhoto oembed, String imageUrl,
			Integer maxwidth, Integer maxheight) {
		try {
			PhotoDimensions d = new PhotoDimensions(imageUrl)
					.resize(maxwidth, maxheight);
			oembed.setUrl(imageUrl);
			oembed.setHeight(d.height);
			oembed.setWidth(d.width);
		} catch (Exception e) {
			LOG.warn("Unabled to get photo properties for "+imageUrl, e);
		} 
	}
	
	static String truncate(String s, int length, String suffix) {
		if (s.length()>255) {
			return s.substring(0, 255-suffix.length())+suffix;
		}
		return s;
	}
	
	public interface ApiLookup<Item> {
		Item lookupContent(String sourceId);
	}
	
	public abstract class AbstractOEmbedLookup<Item> implements ApiLookup<Item> {
		private final Logger LOG = LoggerFactory.getLogger(AbstractOEmbedLookup.class);
				
		Map<String, Object> getUrlVariables(String sourceId) {
			return new HashMap<String, Object>();	
		}
				
		void validateUrlFormat(String sourceId) {
			try {
				new URL(sourceId);
			} catch (MalformedURLException e) {
				throw new RuntimeException(e);
			}
		}
		
		public ResponseEntity<Oembed> lookupContentInterally(String url) {
			return new ResponseEntity<Oembed>(oembedController.oembedJson(url, 0, 0, "json", 
					new ServletWebRequest(request), SecurityContextHolder.getContext().getAuthentication()), 
					HttpStatus.OK);
		}

		@Override
		public Item lookupContent(String sourceId) {
			validateUrlFormat(sourceId);
			
			//Validate url exists
			String url = getUrl(sourceId);
			Map<String, Object> urlVariables = getUrlVariables(sourceId);
			ResponseEntity<Oembeds.Oembed> response;
			try {
				if (url.startsWith("/")) {
					response = lookupContentInterally(sourceId);
				} else {
					response = restTemplate.getForEntity(url, Oembeds.Oembed.class, urlVariables);
				}
				HttpStatus status = response.getStatusCode();
				if (!status.is2xxSuccessful()) {
					return parseError(sourceId, response);
				}
			} catch (HttpStatusCodeException e) {
				return parseError(sourceId, e);
			} catch (RestClientException e) {
				LOG.debug("Unexpected format", e);
				ResponseEntity<String> response2 = restTemplate.getForEntity(url, String.class, urlVariables);
				LOG.debug("Actual response:\n{}",response2.getBody());
				throw e;
			}
				
			return parseSuccess(sourceId, response);
		}
		
		abstract String getUrl(String sourceId);
				
		abstract Item parseError(String sourceId, ResponseEntity<Oembeds.Oembed> response);
		
		abstract Item parseError(String sourceId, HttpStatusCodeException e);
		
		abstract Item parseSuccess(String sourceId, ResponseEntity<Oembeds.Oembed> response);
	}
	
	public class AllowlistOembedLookup extends AbstractOEmbedLookup<Oembeds.Oembed> {
		
		private Logger LOG = LoggerFactory.getLogger(AllowlistOembedLookup.class);
		
		@Override
		String getUrl(String sourceId) {
			return sourceId;
		}

		Oembeds.Oembed parseSuccess(String sourceId,
				ResponseEntity<Oembeds.Oembed> response) {
			return response.getBody();
		}

		Oembeds.Oembed parseError(String sourceId, ResponseEntity<Oembeds.Oembed> response)  {
			LOG.warn(sourceId+" returned a status of "+response.getStatusCode().value());
			
			return response.getBody();
		}
		
		Oembeds.Oembed parseError(String sourceId, HttpStatusCodeException e)  {
			LOG.warn(sourceId+" returned a status of "+e.getStatusCode().value());
			
			return null;
		}
	}
	
	public class AllowlistLookup extends AllowlistOembedLookup {
		
	
		private String oembedUrl;
		
		public AllowlistLookup(String oembedUrl) {
			super();
			this.oembedUrl = oembedUrl;
		}

		@Override
		String getUrl(String sourceId) {
			return oembedUrl;
		}

		@Override
		Map<String, Object> getUrlVariables(String sourceId) {
			Map<String, Object> urlVariables = super.getUrlVariables(sourceId);
			urlVariables.put("url", sourceId);
			return urlVariables;		
		}
	
	}
	
	/**
	 * Uses RestTemplate
	 */
	public class RestTemplateUrlLookup implements ApiLookup<ExtractedMetadata> {
		
		private final Logger LOG = LoggerFactory.getLogger(RestTemplateUrlLookup.class);
		
		@Override
		public ExtractedMetadata lookupContent(String sourceId) {
			
			//Check Content type first
			MediaType contentType;
			try {
				ResponseEntity<String> response = restTemplate.exchange(sourceId, 
						HttpMethod.HEAD, new HttpEntity<String>(""), String.class);
				LOG.debug("Connection to {} returned status code {}", 
						sourceId, response.getStatusCode());
				for (Entry<String, List<String>> entry: response.getHeaders().entrySet()) {
					LOG.debug(entry.getKey()+" "+entry.getValue());
				}
				
				if (!response.getStatusCode().is2xxSuccessful()) {
					//If Redirect
					if (response.getStatusCode().is3xxRedirection() &&
							notInfinteLoop(sourceId, response.getHeaders().getLocation().toString())) {
						URI location = response.getHeaders().getLocation();
						if (!location.isAbsolute()) {
							//Handles sites that give relative Locations for redirects
							URI original = URIBuilder.fromUri(sourceId).build();
							try {
								location = new URI(original.getScheme(), original.getHost(), 
										location.getPath(), null);
							} catch (URISyntaxException e) {
								return parseError(sourceId, response);
							}
						}
						return lookupContent(location.toString());
					}
					
					return parseError(sourceId, response);
				}
				contentType = response.getHeaders().getContentType();
			} catch (HttpStatusCodeException e) {
				LOG.warn("Unabled to connect to URL {} {}", sourceId, e.getStatusCode(), e);
				LOG.debug("Unabled to connect to URL {} {}: {}", 
						sourceId, e.getStatusCode(), e.getResponseBodyAsString(), e);
				ExtractedMetadata exurl = new ExtractedMetadata();
				exurl.setUrl(sourceId);
				exurl.setTitle("Unabled to reach site: "+e.getClass().getSimpleName());
				return exurl;
			}
			
			/**
			 * TODO - start testing; this!
			 */
			ExtractedMetadata exurl = null;
			if (contentType.isCompatibleWith(MediaType.TEXT_HTML)) {
				exurl = parseHtml(sourceId, contentType);
			}
			MediaType imageType = MediaType.parseMediaType("image/*");
			if (contentType.isCompatibleWith(imageType)) {
				exurl = parseImage(sourceId, contentType);
			}
//			if (contentType.startsWith("image/")) {
//				exurl = parseImage(url, contentType);
//			}
												
			return exurl;
		}
		
		private boolean notInfinteLoop(String originalUrl, String redirectUrl) {
			//TODO do better job of detecting infinte loop
			return !redirectUrl.equalsIgnoreCase(originalUrl);
		}
		
		private ExtractedMetadata parseError(String url, ResponseEntity<String> response) {
			LOG.warn("{} returned a status of {}", url, response.getStatusCode());
			
			ExtractedMetadata exurl = new ExtractedMetadata();
			exurl.setStatusCode(response.getStatusCode().value());
			exurl.setUrl(url);
			exurl.setTitle("HTTP Status Code "+response.getStatusCode());
			return exurl;
		}
		
		private ExtractedMetadata parseImage(String url, MediaType contentType) {
			ExtractedMetadata exurl = new ExtractedMetadata();
			exurl.setStatusCode(200);
			exurl.setUrl(url);
			exurl.setContentType(contentType.toString());
			exurl.setImageUrl(url.toString());
			exurl.setSummary("Image");
			return exurl;
		}
		
		private ExtractedMetadata parseHtml(String url, MediaType contentType) {
			ExtractedMetadata exurl = new ExtractedMetadata();
			exurl.setStatusCode(200);
			exurl.setUrl(url);
			exurl.setContentType(contentType.toString());
			exurl.setSummary("Link: "+exurl.getUrl());
			
			Document doc = null;
			try {
				Response response = Jsoup.connect(url.toString()).execute();
				exurl.setUrl(response.url().toString());
				doc = response.parse();
				//LOG.debug(doc.html());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			
			//oEmbed
			Elements oembeds = doc.select("link[type=application/json+oembed]");
			if (!oembeds.isEmpty()) {
				String oembedUrl = oembeds.attr("href");
				try {
					oembedUrl = URLDecoder.decode(oembedUrl,"UTF-8");
				} catch (UnsupportedEncodingException e) {}
				exurl.setOembedUrl(oembedUrl);
				exurl.setOembedTitle(oembeds.attr("title"));
			}
			
			//SiteName
			Elements siteName = doc.select("meta[property=og:site_name]");
			if (!siteName.isEmpty()) {
				exurl.setSiteName(siteName.get(0).attr("content"));
			}
			
			//Title
			Elements titles = doc.select("title");
			if (!titles.isEmpty()) {
				exurl.setTitle(titles.get(0).html());
				exurl.setSummary("Link: "+exurl.getTitle());
			}
			
			
			//Description
			//schema.org
			Elements descriptions = doc.select("[itemprop=description]");
			if (descriptions.isEmpty()) {
				//Description
				descriptions = doc.select("meta[name=description]");
				if (descriptions.isEmpty()) {
					//Twitter
					descriptions = doc.select("meta[name=twitter:description]");
					if (descriptions.isEmpty()) {
						//Facebook
						descriptions = doc.select("meta[property=og:description]");
					}
				}	
				if (!descriptions.isEmpty()) {
					exurl.setDescription(truncate(descriptions.get(0).attr("content"),255,"..."));
				}
			} else {
				String text = descriptions.get(0).text();
				String content = descriptions.get(0).attr("content");
				String s = text.length()>0?text:content;
				exurl.setDescription(truncate(s,255,"..."));
			}
			
			
			//Image
			//schema.org
			Elements images = doc.select("[itemprop=image]");
			if (images.isEmpty()) {
				//Twitter
				images = doc.select("meta[name=twitter:image]");
				if (images.isEmpty()) {
					//Facebook
					images = doc.select("meta[property=og:image]");
				}
				if (!images.isEmpty()) {
					exurl.setImageUrl(images.get(0).attr("content"));
				}
			} else {
				String src = images.get(0).attr("src");
				String content = images.get(0).attr("content");
				String s = src.length()>0?src:content;
				exurl.setImageUrl(s);
			}
			
			return exurl;
		}
	}
		
	
	class PhotoDimensions {
		int width;
		int height;
		public PhotoDimensions(URL imageUrl) throws IOException {
			readUrl(imageUrl);
		}
		public PhotoDimensions(String imageUrl) throws IOException {
			readUrl(imageUrl);
		}
		void readUrl(String imageUrl) throws IOException {
			readUrl(new URL(imageUrl));
		}
		void readUrl(URL imageUrl) throws IOException {
			BufferedImage bufferedImage = ImageIO.read(imageUrl);
			this.width = bufferedImage.getWidth();
			this.height = bufferedImage.getHeight();
		}
		public PhotoDimensions resize(int maxwidth, int maxheight) {
			if (maxwidth>0 && maxheight>0) {
				int fHeight = maxheight;
				int fWidth = maxwidth;
				if (height > maxheight || width > maxwidth) {
					fHeight = maxheight;
					int wid = maxwidth;
					float ratio = (float)width / (float)height;
					fWidth = Math.round(fHeight * ratio);
					if (fWidth > wid) {
						//resize again for the width this time
						fHeight = Math.round(wid/ratio);
						fWidth = wid;
					}
				}
				this.width = fWidth;
				this.height = fHeight;
			}
			return this;
		}
	}
	
}
