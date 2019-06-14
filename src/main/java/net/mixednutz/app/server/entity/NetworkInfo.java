package net.mixednutz.app.server.entity;

import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;

public class NetworkInfo extends net.mixednutz.api.core.model.NetworkInfo {

	private String oembedBaseUrl;
	
	public void init(HttpServletRequest request) {
		if (getBaseUrl()==null) {
			try {
				URL baseUrl = new URL(
						request.getScheme(), 
						request.getServerName(), 
						request.getServerPort(), 
						"");
				setBaseUrl(baseUrl.toExternalForm());
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public String getOembedBaseUrl() {
		return oembedBaseUrl;
	}

	public void setBaseOembedUrl(String oembedBaseUrl) {
		this.oembedBaseUrl = oembedBaseUrl;
	}
		
}
