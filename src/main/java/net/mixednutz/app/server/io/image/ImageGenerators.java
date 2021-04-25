package net.mixednutz.app.server.io.image;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import net.mixednutz.app.server.io.domain.PersistableFile;

@Component
public class ImageGenerators {
	
	private Map<String,ImageGenerator> imageGenerators = new HashMap<String,ImageGenerator>();

	@Autowired
	public void addGenerators(List<ImageGenerator> imageGeneratorList) {
		for (ImageGenerator imageGenerator: imageGeneratorList) {
			addImageGenerator(imageGenerator.getMimeType(), imageGenerator);
		}
	}
	
	public ImageGenerators addImageGenerator(String contentType, ImageGenerator imageGenerator) {
		imageGenerators.put(contentType, imageGenerator);
		return this;
	}
		
	public Map<String, ImageGenerator> getImageGenerators() {
		return imageGenerators;
	}

	public void setImageGenerators(Map<String, ImageGenerator> imageGenerators) {
		this.imageGenerators = imageGenerators;
	}
	
	public InputStream resize(String contentType, InputStream stream, int width, int height) {
		if (imageGenerators.containsKey(contentType)) {
			return imageGenerators.get(contentType).resize(stream, width, height);
		}
		return null;
	}
	
	public File rotate(String contentType, InputStream stream, int angleInDegrees) {
		if (imageGenerators.containsKey(contentType)) {
			return imageGenerators.get(contentType).rotateImage(stream, angleInDegrees);
		}
		return null;
	}
	
	public String generateAvatar(PersistableFile file) {
		if (imageGenerators.containsKey(file.getContentType())) {
			return imageGenerators.get(file.getContentType()).generateAvatar(file);
		}
		return null;
	}
	
	public File generateLargeAvatar(PersistableFile file) {
		if (imageGenerators.containsKey(file.getContentType())) {
			return imageGenerators.get(file.getContentType()).generateLargeAvatar(file);
		}
		return null;
	}
	
	public File generateTinyFeature(PersistableFile file) {
		if (imageGenerators.containsKey(file.getContentType())) {
			return imageGenerators.get(file.getContentType()).generateTinyFeature(file);
		}
		return null;
	}
	
	public File generateSmallFeature(PersistableFile file) {
		if (imageGenerators.containsKey(file.getContentType())) {
			return imageGenerators.get(file.getContentType()).generateSmallFeature(file);
		}
		return null;
	}
		
	public File generateLargeFeature(PersistableFile file) {
		if (imageGenerators.containsKey(file.getContentType())) {
			return imageGenerators.get(file.getContentType()).generateLargeFeature(file);
		}
		return null;
	}
	
	public File generateCover(PersistableFile file) {
		if (imageGenerators.containsKey(file.getContentType())) {
			return imageGenerators.get(file.getContentType()).generateCover(file);
		}
		return null;
	}
	
	public File generateThumbnail(PersistableFile file) {
		if (imageGenerators.containsKey(file.getContentType())) {
			return imageGenerators.get(file.getContentType()).generateThumbnail(file);
		}
		return null;
	}
		
	public File getLargeAvatarFilename(String basefilename, String contentType) {
		if (imageGenerators.containsKey(contentType)) {
			return imageGenerators.get(contentType).getLargeAvatarFilename(basefilename);
		}
		return null;
	}
	
	public File getTinyFeatureFilename(String basefilename, String contentType) {
		if (imageGenerators.containsKey(contentType)) {
			return imageGenerators.get(contentType).getTinyFeatureFilename(basefilename);
		}
		return null;
	}
	
	public File getSmallFeatureFilename(String basefilename, String contentType) {
		if (imageGenerators.containsKey(contentType)) {
			return imageGenerators.get(contentType).getSmallFeatureFilename(basefilename);
		}
		return null;
	}
	
	public File getLargeFeatureFilename(String basefilename, String contentType) {
		if (imageGenerators.containsKey(contentType)) {
			return imageGenerators.get(contentType).getLargeFeatureFilename(basefilename);
		}
		return null;
	}
	
	public File getCoverFilename(String basefilename, String contentType) {
		if (imageGenerators.containsKey(contentType)) {
			return imageGenerators.get(contentType).getCoverFilename(basefilename);
		}
		return null;
	}
	

}
