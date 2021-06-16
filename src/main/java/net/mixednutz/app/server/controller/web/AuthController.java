package net.mixednutz.app.server.controller.web;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.HandlerMapping;

/**
 * This controller exists so we can force authentication on a given page
 * 
 * It works by added /auth to the front of a URI. Because "/auth" is in
 * SecurityConfig as am authenticated page, a login is forced. Upon
 * authentication we're brought to this controller which will do an immediate
 * redirect page to path after the /auth prefix.
 * 
 * @author apfesta
 *
 */
@Controller
public class AuthController {

	public static final String AUTH_THEN_REDIRECT_PREFIX = "/auth/**";
	
	@RequestMapping(value = AUTH_THEN_REDIRECT_PREFIX)
	public String redirect(
			@RequestParam(value = "hash", defaultValue = "") String hash, 
			HttpServletRequest request) {
		String path = (String) request.getAttribute(
				HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
		String bestMatchPattern = (String) request.getAttribute(
				HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);

		AntPathMatcher apm = new AntPathMatcher();
		String finalPath = apm.extractPathWithinPattern(bestMatchPattern, path);

		return "redirect:/" + finalPath + 
				(StringUtils.hasText(hash) ? ("#" + hash) : "");
	}

}
