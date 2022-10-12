package net.mixednutz.app.server.manager.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import net.mixednutz.app.server.entity.OembedFilterWhitelist;
import net.mixednutz.app.server.entity.Oembeds;
import net.mixednutz.app.server.manager.OembedFilterWhitelistManager;
import net.mixednutz.app.server.repository.OembedFilterWhitelistRepository;

@Service
public class OembedFilterWhitelistManagerImpl implements OembedFilterWhitelistManager {
	
	private static final Logger LOG = LoggerFactory.getLogger(OembedFilterWhitelistManagerImpl.class);
		
	private final OembedFilterWhitelistRepository oembedFilterWhitelistRepository;

	private Map<String, ApiLookup<?>> lookupMap;
	private Map<String, Pattern> patternMap;
	
	@Autowired
	public OembedFilterWhitelistManagerImpl(OembedFilterWhitelistRepository oembedFilterWhitelistRepository) {
		super();
		this.oembedFilterWhitelistRepository = oembedFilterWhitelistRepository;
	}

	@PostConstruct
	public void loadWhitelist() {
		lookupMap = new HashMap<String, ApiLookup<?>>();
		patternMap = new HashMap<String, Pattern>();
		
		for (OembedFilterWhitelist whitelisted: oembedFilterWhitelistRepository.findAll()) {
			this.lookupMap.put(whitelisted.getName(), new WhitelistLookup(whitelisted.getOembedUrl()));
			this.patternMap.put(whitelisted.getName(), 
					Pattern.compile(whitelisted.getUrlPattern(), Pattern.CASE_INSENSITIVE));
			LOG.debug("Registered Oembed Whitelist Pattern for {} : {} {}",
					whitelisted.getDescription(), whitelisted.getUrlPattern(), whitelisted.getOembedUrlPattern());
		}
	}
	
	public Optional<String> deriveSourceType(String sourceId) {
		for (Entry<String, Pattern> entry: patternMap.entrySet()) {
			if (entry.getValue().matcher(sourceId).matches()) {
				return Optional.of(entry.getKey());
			}
		}
		return Optional.empty();
	}
	
	public interface ApiLookup<Item> {
		Item lookupContent(String sourceId);
	}
	
	public abstract static class AbstractOEmbedLookup<Item> implements ApiLookup<Item> {
		private static final Logger LOG = LoggerFactory.getLogger(AbstractOEmbedLookup.class);
		
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

		@Override
		public Item lookupContent(String sourceId) {
			validateUrlFormat(sourceId);
			
			//Validate url exists
			RestTemplate rest = new RestTemplate();
			String url = getUrl(sourceId);
			Map<String, Object> urlVariables = getUrlVariables(sourceId);
			ResponseEntity<Oembeds.Oembed> response;
			try {
				response = rest.getForEntity(url, Oembeds.Oembed.class, urlVariables);
				HttpStatus status = response.getStatusCode();
				if (!status.is2xxSuccessful()) {
					return parseError(sourceId, response);
				}
			} catch (HttpStatusCodeException e) {
				return parseError(sourceId, e);
			} catch (RestClientException e) {
				LOG.debug("Unexpected format", e);
				ResponseEntity<String> response2 = rest.getForEntity(url, String.class, urlVariables);
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
	
	public static class WhitelistOembedLookup extends AbstractOEmbedLookup<Oembeds.Oembed> {
		private Logger LOG = LoggerFactory.getLogger(WhitelistOembedLookup.class);

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
	
	public static class WhitelistLookup extends WhitelistOembedLookup {
	
		private String oembedUrl;
		
		public WhitelistLookup(String oembedUrl) {
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
	
	
}
