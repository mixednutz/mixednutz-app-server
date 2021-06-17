package net.mixednutz.app.server.controller.api;

import org.springframework.data.rest.webmvc.PersistentEntityResource;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import net.mixednutz.app.server.entity.User;

@RepositoryRestController
public class UserRestController {
	
	@RequestMapping(value = "/user", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.CREATED)
	public @ResponseBody PersistentEntityResource create(@RequestBody Resource<User> userRes,
			Authentication auth, PersistentEntityResourceAssembler resourceAssembler) {
		// if (existing) {
		// throw new Exception("Resource already exists");
		// }
		// return resourceAssembler.toResource(manager.save(user));
		return null;
	}
	
	/**
	 * Simple endpoint to keep the authentication session alive
	 */
	@RequestMapping("/keepAlive")
	@ResponseStatus(value=HttpStatus.NO_CONTENT)
	public @ResponseBody String keepAlive() {
		return null;
	}

}
