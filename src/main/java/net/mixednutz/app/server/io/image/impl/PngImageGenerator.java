/**
 * 
 */
package net.mixednutz.app.server.io.image.impl;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

/**
 * @author Andy
 *
 */
@Component
public class PngImageGenerator extends AbstractImageGenerator {
		
	private String extension = ".png";
	
	@Override
	public String getMimeType() {
		return MediaType.IMAGE_PNG_VALUE;
	}
	@Value("${photoDirectory:#{null}}")
	public void setPhotoDirectory(String photosDirectory) {
		super.setAvatarDirectory(new File(photosDirectory,"avatars"));
		super.setDestinationDirectory(new File(photosDirectory!=null?photosDirectory:""));
	}
	@Value("#{systemProperties['java.io.tmpdir']}")
	public void setTempDirectory(String tempDirectory) {
		super.setTempDirectory(new File(tempDirectory));
	}

	protected void doWrite(BufferedImage outImage, OutputStream out) throws IOException {
		//PNG-encode the image and write to file.
		ImageIO.write(outImage, "png", out); 
	}
	
	public String getExtension() {
		return extension;
	}

	public void setExtension(String extension) {
		this.extension = extension;
	}
	
}
