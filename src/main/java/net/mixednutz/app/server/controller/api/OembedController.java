package net.mixednutz.app.server.controller.api;

import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.NativeWebRequest;

import net.mixednutz.api.core.model.NetworkInfo;
import net.mixednutz.app.server.controller.exception.ResourceNotFoundException;
import net.mixednutz.app.server.entity.InternalTimelineElement;
import net.mixednutz.app.server.entity.Oembeds.Oembed;
import net.mixednutz.app.server.entity.Oembeds.OembedRich;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.manager.ApiElementConverter;
import net.mixednutz.app.server.manager.ApiManager;
import net.mixednutz.app.server.manager.ExternalContentManager;
import net.mixednutz.app.server.manager.ExternalContentManager.ExtractedMetadata;

@Controller
@RequestMapping(OembedController.OEMBED_DIR)
public class OembedController {
	
	private static final Logger LOG = LoggerFactory.getLogger(OembedController.class);
	
	public static final String OEMBED_DIR = "/oembed";
	
	@Autowired
	private ApiManager apiManager;
	
	@Autowired
	private NetworkInfo networkInfo;
	
	@Autowired
	private ExternalContentManager externalContentManager;
	
	@RequestMapping(method = RequestMethod.GET, params="format=xml")
	public @ResponseBody Oembed oembedXml() {
		throw new UnsupportedOperationException("We don't support the XML oembed format.  Please use format=json instead.");
	}
	
	private boolean isExternalPath(String url) {
		URI uri = null;
		try {
			uri = new URI(url);
			
			return (!uri.getHost().equalsIgnoreCase(networkInfo.getHostName()));
		} catch (URISyntaxException e) {
			throw new ResourceNotFoundException("Bad URL: "+url, e);
		}
	}
	
	@RequestMapping(method = RequestMethod.GET, params="format!=xml")
	public @ResponseBody Oembed oembedJson(
			@RequestParam String url,
			@RequestParam(defaultValue="0") Integer maxwidth,
			@RequestParam(defaultValue="0") Integer maxheight,
			@RequestParam(defaultValue="json") String format,
			NativeWebRequest request, Authentication auth) {
		URI uri = null;
		try {
			uri = new URI(url);
		} catch (URISyntaxException e) {
			throw new ResourceNotFoundException("Bad URL: "+url, e);
		}
		
		String contextPath = request.getContextPath();
		String host = uri.getHost();
		String path = uri.getPath();
		if (host.equalsIgnoreCase("localhost")) {
			//This is only allowed if request was localhost
			URI requestUri = null;
			try {
				requestUri = new URI(request.getNativeRequest(HttpServletRequest.class).getRequestURL().toString());
			} catch (URISyntaxException e) {
				throw new ResourceNotFoundException("Bad URL: "+url, e);
			}
			if (host.equals(requestUri.getHost())) {
				host = networkInfo.getHostName();
			} else {
				LOG.warn("Host is localhost: {}.  This can only be used for testing!", url);
			}
		}
		if (!host.equalsIgnoreCase(networkInfo.getHostName())) {
			//External URL
			path = uri.toString();
		} else if (path.startsWith(contextPath)) {
			path = path.substring(contextPath.length());
		}
		
		return apiManager.toOembed(path, maxwidth, maxheight, format, auth, networkInfo.getBaseUrl())
				.orElseThrow(() -> 
				new ResourceNotFoundException("Unable to find resource: "+url));
	}
	
	/**
	 * The ApiManger will pick this up during the toOembed method
	 */
	@Component
	public class OembedConverter implements ApiElementConverter<Void> {

		private static final String EMBED_BASE_URL = "/embed/lookup?url=";
		
		@Override
		public boolean canConvert(Class<?> entityClazz) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public Oembed toOembed(String path, Integer maxwidth, Integer maxheight, String format, Authentication auth,
				String baseUrl) {
			
			if (externalContentManager.isAllowListed(path)) {
				
				//Get Metadata
				ExtractedMetadata xurl = externalContentManager.lookupMetadata(path);
	
				//Add oembed attributes
				Oembed oembed = externalContentManager.toOembed(xurl, maxwidth, maxheight);
				if (oembed instanceof OembedRich) {
					int height = (maxheight > 270 || maxheight <=0) ? 270 : maxheight;
					int width = (maxwidth > 658 || maxwidth <= 0) ? 658 : maxwidth;
					
					StringBuffer html = new StringBuffer();
					html.append("<iframe");
					html.append(" height=\""+height+"\"");
					html.append(" src=\""+networkInfo.getBaseUrl()+EMBED_BASE_URL+xurl.getUrl()+"\"");
					html.append(" style=\"max-width: "+width+"px; width: calc(100% - 2px);\"");
					html.append(" frameborder=\"0\"></iframe>");
					((OembedRich) oembed).setHtml(html.toString());
				}
				return oembed;
			}
			return null;
		}

		@Override
		public boolean canConvertOembed(String path) {
			return isExternalPath(path);
		}

		@Override
		public InternalTimelineElement toTimelineElement(InternalTimelineElement element, Void entity, User viewer,
				String baseUrl) {
			// TODO Auto-generated method stub
			return null;
		}

	}

	

}
