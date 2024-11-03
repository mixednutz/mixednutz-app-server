package org.w3c.activitypub;

import static org.ietf.webfinger.WebfingerSettings.WEBFINGER_ENDPOINT;

import java.net.URI;

import org.ietf.webfinger.WebfingerResponse;
import org.ietf.webfinger.WebfingerResponse.WebfingerError;
import org.ietf.webfinger.server.WebfingerServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
public class WebfingerController {
	
	private static final Logger LOG = LoggerFactory.getLogger(WebfingerController.class);
	
	@Autowired
	private WebfingerServer webfingerServer;
	
	private ObjectMapper mapper = new ObjectMapper();
	

	@GetMapping(path=WEBFINGER_ENDPOINT,produces={"application/jrd+json"})
	public ResponseEntity<String> webfinger(@RequestParam("resource")URI resource) throws JsonProcessingException {
		
		WebfingerResponse response;
		try {
			response = webfingerServer.handleWebfingerRequest(resource);
		} catch (WebfingerServer.BadRequestException e) {
			LOG.error("Error handing WebfingerRequest", e);
			response = new WebfingerError(e.getMessage());
		} catch (Exception e) {
			LOG.error("Error handing WebfingerRequest", e);
			throw e;
		}
		
		String str = mapper.writeValueAsString(response);
		
		if (response instanceof WebfingerError) {
			return new ResponseEntity<String>(str, HttpStatus.BAD_REQUEST);
		}
		
		return new ResponseEntity<String>(str, HttpStatus.OK);
	}
		
}
