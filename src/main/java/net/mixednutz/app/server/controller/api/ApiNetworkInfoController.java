package net.mixednutz.app.server.controller.api;

import java.net.MalformedURLException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import net.mixednutz.api.core.model.NetworkInfo;

@Controller
public class ApiNetworkInfoController {

	@Autowired
	private NetworkInfo networkInfo;
		
	@RequestMapping(value={"/social-network-info","/network-info","/mixednutz-info"}, 
			method = RequestMethod.GET)
	public @ResponseBody NetworkInfo networkInfo(HttpServletRequest request) throws MalformedURLException {
		return networkInfo;
	}
}
