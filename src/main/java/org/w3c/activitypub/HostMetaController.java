package org.w3c.activitypub;

import static org.ietf.hostmeta.server.HostMetaSettings.HOSTMETA_ENDPOINT;

import java.io.StringWriter;

import javax.annotation.PostConstruct;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.ietf.hostmeta.server.HostMetaServer;
import org.ietf.hostmeta.server.HostMetaServer.Xrd;
import org.ietf.hostmeta.server.HostMetaSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@Controller
public class HostMetaController {
	
	@Autowired
	private HostMetaServer hostMetaServer;
	
	DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
	DocumentBuilder docBuilder;
	 TransformerFactory transformerFactory = TransformerFactory.newInstance();
	
	@PostConstruct
	public void init() throws ParserConfigurationException {
		docFactory.setNamespaceAware(true);
		docBuilder = docFactory.newDocumentBuilder();
	}
	
	@GetMapping(path = HOSTMETA_ENDPOINT, produces = { "application/xrd+xml" })
	public ResponseEntity<String> hostMeta() throws TransformerException {
		Xrd response = hostMetaServer.handleHostMetaRequest();

		// root elements
		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElementNS(HostMetaSettings.XRD_NS, "XRD");
		
//		Element rootElement = doc.createElement("XRD");
		doc.appendChild(rootElement);

		response.getLinks().forEach(link -> {
			Element linkElement = doc.createElement("Link");
			rootElement.appendChild(linkElement);
			linkElement.setAttribute("rel", link.getRel());
			linkElement.setAttribute("template", link.getTemplate());
		});

		Transformer transformer = transformerFactory.newTransformer();

		// pretty print
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC,"yes"); 
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		
		// hide the standalone="no"
		doc.setXmlStandalone(true);

		DOMSource source = new DOMSource(doc);
		StringWriter stringWriter = new StringWriter();
		StreamResult result = new StreamResult(stringWriter);

		transformer.transform(source, result);

		String str = stringWriter.toString();

		return new ResponseEntity<String>(str, HttpStatus.OK);
	}

}
