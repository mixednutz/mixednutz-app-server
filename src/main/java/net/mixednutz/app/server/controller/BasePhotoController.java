package net.mixednutz.app.server.controller;

import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.amazonaws.services.s3.model.AmazonS3Exception;

import net.mixednutz.app.server.controller.exception.ResourceNotFoundException;
import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.io.domain.FileWrapper;
import net.mixednutz.app.server.io.image.ImageGenerators;
import net.mixednutz.app.server.io.manager.PhotoUploadManager;
import net.mixednutz.app.server.io.manager.PhotoUploadManager.Size;
import net.mixednutz.app.server.repository.UserRepository;

public class BasePhotoController {
	
	private static final Logger LOG = LoggerFactory.getLogger(BasePhotoController.class);
	
	public static final String PHOTOS_STORAGE_DIR = "/photos-storage";
	private static final String PHOTOS_STORAGE_MAPPING = PHOTOS_STORAGE_DIR+"/**";
	
	@Autowired
	protected PhotoUploadManager photoUploadManager;
	
	@Autowired
	protected UserRepository userRepository;
	
	@Autowired
	protected ImageGenerators imageGenerators;
	
	@RequestMapping(value = PHOTOS_STORAGE_MAPPING, method = RequestMethod.GET)
	public ResponseEntity<Resource> getPhotoResource(
			HttpServletRequest request,
			@RequestParam(value="size", defaultValue="original") String sizeName,
			@RequestParam(value="rotate", defaultValue="0") int rotateDegrees,
			@AuthenticationPrincipal User user) {
		String uri = request.getRequestURI();
		String mapping = request.getContextPath()+PHOTOS_STORAGE_DIR+"/";
		String filename = uri.substring(mapping.length());
		
//		Photo photo = photoManager.getPhoto(filename);
		User photoAccount;
		
//		if (photo!=null) {
//			if (user==null &&!photo.isPublic()) {
//				throw new AuthenticationCredentialsNotFoundException("This is not a public photo.");
//			} else if (user!=null) {
//				if (!photoManager.isVisible(photo, user)) {
//					throw new NotAuthorizedException("User does not have permission to view this photo.");
//				}
//			}
//			photoAccount = photo.getOwner()!=null?photo.getOwner():photo.getAuthor();
//		} else {
			//Avatars have public permissions
			photoAccount = userRepository.findByAvatarFilename(filename).orElse(null);
//		} 
		if (photoAccount==null) {
			throw new ResourceNotFoundException("Photo "+filename+" not found");
		}
		
		FileWrapper file = null;
		boolean missingSize = false;
		Size size = Size.getValue(sizeName);
		try {
			file = photoUploadManager.downloadFile(photoAccount, filename, size);
		} catch (AmazonS3Exception e) {
			LOG.error("AmazonError", e);
			if (!"original".equals(size) && e.getStatusCode()==404) {
				try {
					file = photoUploadManager.downloadFile(photoAccount, filename, Size.ORIGINAL);
					missingSize = true;
				} catch (AmazonS3Exception | IOException e2) {
					LOG.error("Error on re-try", e);
					return new ResponseEntity<>(null, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
				}				
			}
		} catch (IOException e) {
			LOG.error("Error reading image", e);
			return new ResponseEntity<>(null, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		if (file==null) {
			return new ResponseEntity<>(null, new HttpHeaders(), HttpStatus.NOT_FOUND);
		}
		try {
			if (missingSize) {
				photoUploadManager.uploadFile(photoAccount, file.getFile(), size);
			}
			
			if (rotateDegrees!=0) {
				try (FileInputStream stream = new FileInputStream(file.getFile())) {
					file = new FileWrapper(
							imageGenerators.rotate(
									file.getContentType(), stream, rotateDegrees), 
							file.getContentType());
				}
			}
			
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.parseMediaType(file.getContentType()));
			
			Resource resource = new FileSystemResource(file.getFile());
			return new ResponseEntity<>(resource, headers, HttpStatus.OK);
						
		} catch (IOException e) {
			LOG.error("Read creating stream", e);
			return new ResponseEntity<>(null, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
		} 
	
	}

}
