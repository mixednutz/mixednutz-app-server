package org.w3c.activitypub;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import software.diaspora.nodeinfo.server.NodeinfoResponse;
import software.diaspora.nodeinfo.server.NodeinfoSchema;
import software.diaspora.nodeinfo.server.NodeinfoServer;
import software.diaspora.nodeinfo.server.NodeinfoSettings;


@Controller
public class NodeinfoController {
	
	@Autowired
	private NodeinfoServer nodeinfoServer;
	
	@GetMapping(path=NodeinfoSettings.NODEINFO_ENDPOINT)
	public @ResponseBody NodeinfoResponse nodeinfo() {
		return nodeinfoServer.handleNodeinfoRequest();
	}
	
	@GetMapping(path=NodeinfoServer.SCHEMA_URI)
	public @ResponseBody NodeinfoSchema nodeinfoSchema() {
		return nodeinfoServer.handleNodeinfoSchemaRequest();
	}

}
