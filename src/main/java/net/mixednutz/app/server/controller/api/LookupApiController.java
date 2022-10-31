package net.mixednutz.app.server.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import net.mixednutz.app.server.manager.ExternalContentManager;
import net.mixednutz.app.server.manager.ExternalContentManager.ExtractedOembedHtml;

@Controller
@RequestMapping({"/api","/internal"})
public class LookupApiController {
	
	@Autowired
	private ExternalContentManager externalContentManager;
	
	@RequestMapping(value="/lookup", method = RequestMethod.GET)
	public ResponseEntity<ExtractedOembedHtml> lookupUrl(@RequestParam("url") String url) {
		return externalContentManager.deriveSourceType(url)
				.flatMap(sourceType->{
					return externalContentManager.lookupContent(
							sourceType, url);
				})
				.map(xurl->{
					return new ResponseEntity<ExtractedOembedHtml>(xurl, 
							HttpStatus.valueOf(xurl.getStatusCode()));
				})
				.orElse(new ResponseEntity<ExtractedOembedHtml>((ExtractedOembedHtml)null, 
					HttpStatus.NO_CONTENT));
				
	}

}
