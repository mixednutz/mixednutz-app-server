package net.mixednutz.app.server.controller.web;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import net.mixednutz.app.server.controller.BaseExternalCredentialsController;
import net.mixednutz.app.server.entity.User;

@Controller
public class ExternalFeedController extends BaseExternalCredentialsController {
	

	//------------
	// Delete Mappings
	//------------
	

	@RequestMapping(value="/feed/id/{feedId}/delete", method = RequestMethod.POST, params="confirm")
	public String deleteModal(@PathVariable Long feedId, 
			@AuthenticationPrincipal User user) {
		
		delete(feedId, user);
		
		return "redirect:/main";
	}

}
