package net.mixednutz.app.server.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import net.mixednutz.api.core.model.NetworkInfo;
import net.mixednutz.api.core.model.NetworkInfoSmall;

@Controller
public class ApiNetworkInfoController {

	@Autowired
	private NetworkInfo networkInfo;
		
	@RequestMapping(value={"/social-network-info","/network-info","/mixednutz-info"}, 
			method = RequestMethod.GET)
	public @ResponseBody NetworkInfo networkInfo() {
		return networkInfo;
	}
	
	public NetworkInfoSmall networkInfoSmall() {
		return new NetworkInfoSmall(networkInfo);
	}
}
