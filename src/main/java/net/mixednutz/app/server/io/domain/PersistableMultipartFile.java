package net.mixednutz.app.server.io.domain;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.web.multipart.MultipartFile;

/**
 * Implementation of PersistableFile that wraps a MultipartFile.
 * 
 * @author apfesta
 *
 */
public class PersistableMultipartFile implements PersistableFile {

	private MultipartFile file;

	public MultipartFile getFile() {
		return file;
	}

	public void setFile(MultipartFile file) {
		this.file = file;
	}

	public String getFilename() {
		return file.getOriginalFilename();
	}

	public String getContentType() {
		return file.getContentType();
	}

	public InputStream getInputStream() {
		try {
			return file.getInputStream();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
