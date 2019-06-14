package net.mixednutz.app.server.io.domain;

import java.io.InputStream;

/**
 * Represents a known file input stream coupled with its filename and content type.
 * 
 * @author apfesta
 *
 */
public interface PersistableFile {
	/**
	 * Must return a new InputStream each time.
	 * 
	 * @return
	 */
	public InputStream getInputStream();
		
	public String getFilename();
	
	public String getContentType();
}
