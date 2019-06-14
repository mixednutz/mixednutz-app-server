package net.mixednutz.app.server.io.domain;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Implementation of PersistableFile that wraps a File.
 * 
 * @author apfesta
 *
 */
public class FileWrapper implements Closeable,PersistableFile {
	
	final File file;
	final String contentType;
	InputStream inputStream;

	public FileWrapper(File file, String contentType) {
		super();
		this.file = file;
		this.contentType = contentType;
	}

	@Override
	public synchronized InputStream getInputStream() {
		if (inputStream!=null) {
			throw new RuntimeException("Cannot call user this object more than once.");
		}
		
		try {
			inputStream = new FileInputStream(file);
			return inputStream;
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getFilename() {
		return file.getName();
	}

	@Override
	public String getContentType() {
		return contentType;
	}
	
	public File getFile() {
		return file;
	}

	public void close() {
		if (inputStream!=null) {
			try {
				inputStream.close();
			} catch (IOException e) {}
		}
	}
	
}