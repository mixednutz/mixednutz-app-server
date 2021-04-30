package net.mixednutz.app.server.controller.api;

import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;

import net.mixednutz.api.core.model.NetworkInfo;
import net.mixednutz.app.server.controller.exception.ResourceNotFoundException;
import net.mixednutz.app.server.entity.Oembeds.Oembed;
import net.mixednutz.app.server.manager.ApiManager;

@Controller
@RequestMapping(OembedController.OEMBED_DIR)
public class OembedController {
	
	public static final String OEMBED_DIR = "/oembed";
	
	@Autowired
	private ApiManager apiManager;
	
	@Autowired
	private NetworkInfo networkInfo;
	
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
		
		return apiManager.toOembed(path, maxwidth, maxheight, format, auth, networkInfo.getBaseUrl())
				.orElseThrow(() -> 
				new ResourceNotFoundException("Unable to find resource: "+url));
	}
	
	
	

}
