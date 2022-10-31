package net.mixednutz.app.server.controller.web;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import net.mixednutz.app.server.controller.exception.ResourceNotFoundException;
import net.mixednutz.app.server.manager.ExternalContentManager;
import net.mixednutz.app.server.manager.ExternalContentManager.ExtractedMetadata;

/**
 * Controller for handling embedding of external content that we looked up.
 * 
 * @author apfesta
 *
 */
@Controller
public class LookupContentController {
	
	@Autowired
	private ExternalContentManager externalContentManager;
	
	@RequestMapping(value="/embed/lookup/url/**", method = RequestMethod.GET)
	public String getExternalContentEmbed(HttpServletRequest request, 
			Model model) {
		String uri = request.getRequestURI();
		String mapping = request.getContextPath()+"/embed/lookup/url/";
		String url = uri.substring(mapping.length());
		return getExternalContentEmbed(url, model);
	}
	
	@RequestMapping(value="/embed/lookup", method = RequestMethod.GET)
	public String getExternalContentEmbed(
			@RequestParam("url") String url, 
			Model model) {
		ExtractedMetadata xcontent = externalContentManager.lookupMetadata(url);
		
		if (xcontent==null) {
			throw new ResourceNotFoundException("Resource "+url+" not found");
		}
		
		model.addAttribute("content", xcontent);
		
		return "lookup/embed";
	}

}
