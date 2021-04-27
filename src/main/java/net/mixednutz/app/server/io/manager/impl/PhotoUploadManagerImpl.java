package net.mixednutz.app.server.io.manager.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.SecureRandom;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;

import net.mixednutz.app.server.entity.User;
import net.mixednutz.app.server.io.domain.FileWrapper;
import net.mixednutz.app.server.io.domain.PersistableFile;
import net.mixednutz.app.server.io.image.ImageGenerators;
import net.mixednutz.app.server.io.manager.PhotoUploadManager;

@Service
public class PhotoUploadManagerImpl implements PhotoUploadManager {
	
	private static Logger LOG = LoggerFactory.getLogger(PhotoUploadManagerImpl.class);

	@Value("${photoDirectory:#{null}}")
	private String photosDirectory;
	
	@Value("${mixednutz.aws.photosBucket:#{null}}")
	private String photosBucket;
	
	private static final String SLASH = "/";
	private static final String ORIGINAL_SIZE = "original";
	private static final String LARGE_SIZE = "large";
	private static final String SMALL_SIZE = "small";
	private static final String TINY_SIZE = "tiny";
	private static final String AVATAR_SIZE = "avatar";
	private static final String BOOK_SIZE = "book";

	@Autowired
	private ImageGenerators imageGenerators;
	
    @Autowired
    private AmazonS3 amazonS3Client;
    
    
	@Override
	public FileWrapper downloadFile(User user, String filename, String sizeName) throws IOException {

		String contentType = getContentType(filename.substring(
				filename.lastIndexOf('.')+1, filename.length())); 
		
		File file = this.getFile(filename, contentType, sizeName);
		if (file==null) {
			LOG.error("getFile({},{},{}) returned null.  Throwing exception", 
					filename, contentType, sizeName);
			throw new RuntimeException("No ImageGenerator for "+contentType);
		}
		if (!file.exists()) {
			//Download
			LOG.info("Pulling {} {} from cloud", sizeName, filename);
			return downloadCloudFileInternal(user, filename, sizeName);
		}
		LOG.debug("{} {} exists locally", sizeName, filename);
		return new FileWrapper(file, contentType);
	}

	@Override
	public String uploadFile(User user, File file) throws IOException {
		return this.uploadFile(user, file, null, false);
	}
	
	public String uploadFile(User user, File file, String size) throws IOException {
		return this.uploadFile(user, file, null, false, size);
	}

	@Override
	public String uploadFile(User user, File file, String renameToFilename, boolean replaceIfExisting) throws IOException {
		if (!file.getParentFile().equals(new File(photosDirectory)) || renameToFilename!=null) {
			file = replaceLocally(file, renameToFilename, replaceIfExisting);
		}
		
		String filename = file.getName();
		String contentType = getContentType(filename.substring(
				filename.lastIndexOf('.')+1, filename.length())); 
		
		uploadAllSizesToCloud(user, file, contentType, replaceIfExisting);
		
		return file.getName();
	}
	
	public String uploadFile(User user, File file, String renameToFilename, boolean replaceIfExisting, String size) throws IOException {
		if (!file.getParentFile().equals(new File(photosDirectory)) || renameToFilename!=null) {
			file = replaceLocally(file, renameToFilename, replaceIfExisting);
		}
		
		String filename = file.getName();
		String contentType = getContentType(filename.substring(
				filename.lastIndexOf('.')+1, filename.length())); 
		
		uploadToCloud(user, file, contentType, replaceIfExisting, size);
		
		return file.getName();
	}

	@Override
	public String uploadFile(User user, PersistableFile persistableFile) throws IOException {
		File localOriginal = uploadLocally(persistableFile.getInputStream(), persistableFile.getContentType());

		uploadAllSizesToCloud(user, localOriginal, persistableFile.getContentType(), false);
		
		return localOriginal.getName();
	}
	
	@Override
	public String uploadFile(User user, PersistableFile persistableFile, String size) throws IOException {
		File localOriginal = uploadLocally(persistableFile.getInputStream(), persistableFile.getContentType());

		uploadToCloud(user, localOriginal, persistableFile.getContentType(), false, size);
		
		return localOriginal.getName();
	}
	
	/*
	 * //Syncronous uploader
		Executor executor = new Executor() {
			@Override
			public void execute(Runnable command) {
				command.run();
			}
		};
	 */
	
	protected CloudUploadWorker getCloudWorker(User user, final File file, 
			String contentType, boolean replaceIfExisting, final String size) {
		return new CloudUploadWorker(file,contentType, user.getUserId(), size, replaceIfExisting) {
			@Override
			File getSourceFile(PersistableFile persistableFile) {
				LOG.debug("Generating {} version of {}", size, file.getName());
				if (LARGE_SIZE.equals(size)) {
					return imageGenerators.generateLargeFeature(persistableFile);
				} else if (SMALL_SIZE.equals(size)) {
					return imageGenerators.generateSmallFeature(persistableFile);
				} else if (TINY_SIZE.equals(size)) {
					return imageGenerators.generateTinyFeature(persistableFile);
				} else if (AVATAR_SIZE.equals(size)) {
					return imageGenerators.generateLargeAvatar(persistableFile);
				} else if (BOOK_SIZE.equals(size)) {
					return imageGenerators.generateCover(persistableFile);
				}
				throw new UnsupportedOperationException("Unknown size : "+size);
			}};
	}
	
	protected void uploadToCloud(User user, final File file, String contentType, boolean replaceIfExisting, String size) {
		//Syncronous uploader
		Executor executor = new Executor() {
			@Override
			public void execute(Runnable command) {
				command.run();
			}
		};
		
		executor.execute(getCloudWorker(user, file, contentType, replaceIfExisting, size));
	}
	
	protected void uploadAllSizesToCloud(User user, final File file, String contentType, boolean replaceIfExisting) {
		ExecutorService executor = Executors.newCachedThreadPool();
		LOG.info("File {} is {} bytes.", file.getName(), file.length());
		
		executor = Executors.newSingleThreadExecutor();
		
		//Upload Original
		executor.execute(
				new CloudUploadWorker(file, contentType, user.getUserId(), ORIGINAL_SIZE, replaceIfExisting) {
					@Override
					File getSourceFile(PersistableFile persistableFile) {
						return localFile;
					}});
		
		//Upload Large Feature
		executor.execute(getCloudWorker(user, file, contentType, replaceIfExisting, LARGE_SIZE));
		//Upload Small Feature
		executor.execute(getCloudWorker(user, file, contentType, replaceIfExisting, SMALL_SIZE));
		//Upload Small Feature
		executor.execute(getCloudWorker(user, file, contentType, replaceIfExisting, TINY_SIZE));
		//Upload Large Avatar
		executor.execute(getCloudWorker(user, file, contentType, replaceIfExisting, AVATAR_SIZE));
				
		executor.shutdown();
		try {
			executor.awaitTermination(60, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	protected FileWrapper downloadCloudFileInternal(User user, String filename, String sizeName) throws IOException {
		try (S3Object s3object = downloadFromCloud(photosBucket, getCloudFileName(filename, user.getUserId(), sizeName))) {
			File file;
			String contentType = s3object.getObjectMetadata().getContentType();
			if (ORIGINAL_SIZE.equalsIgnoreCase(sizeName)) {
				file = new File(photosDirectory, filename);
			} else if (LARGE_SIZE.equalsIgnoreCase(sizeName)) {
				file = imageGenerators.getLargeFeatureFilename(filename, contentType);
			} else if (SMALL_SIZE.equalsIgnoreCase(sizeName)) {
				file = imageGenerators.getSmallFeatureFilename(filename, contentType);
			} else if (TINY_SIZE.equalsIgnoreCase(sizeName)) {
				file = imageGenerators.getTinyFeatureFilename(filename, contentType);
			} else if (AVATAR_SIZE.equalsIgnoreCase(sizeName)) {
				file = imageGenerators.getLargeAvatarFilename(filename, contentType);
			} else {
				throw new RuntimeException("Unknown size: "+sizeName);
			}
			if (!file.exists()) {
				saveFile(s3object.getObjectContent(), file);
			}
			
			return new FileWrapper(file, contentType);
		} 
	}
	
	protected File uploadLocally(InputStream in, String contentType) throws IOException {
		String baseFilename = uniqid();
		String extension = getExtension(contentType);
		File file = new File(photosDirectory, 
				baseFilename+'.'+extension);
		saveFile(in, file);
		return file;
	}
	
	protected File replaceLocally(File inFile, String renameToFilename, boolean replaceIfExisting) throws IOException {
		Path newPath = Files.move(inFile.toPath(), 
				new File(photosDirectory, renameToFilename!=null?renameToFilename:inFile.getName()).toPath(), 
				replaceIfExisting?StandardCopyOption.REPLACE_EXISTING:null);
		return newPath.toFile();
	}

	protected void saveFile(InputStream in, File outFile) throws IOException {
		Path target = outFile.toPath();
		Files.copy(in, target);
	}
	
	protected String getCloudFileName(File file, Long userId, String size) {
		return getCloudFileName(file.getName(), userId, size);
	}
	
	protected String getCloudFileName(String filename, Long userId, String size) {
		return userId+SLASH+(size!=null?size+SLASH:"")+filename;
	}
	
	protected PutObjectResult uploadToCloud(File file, String bucketName, String filename) throws IOException {
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, filename, file);
        return this.amazonS3Client.putObject(putObjectRequest);
	}
	
	protected boolean existsInCloud(String bucketName, String filename) {
		return this.amazonS3Client.doesObjectExist(bucketName, filename);
	}
	
	protected S3Object downloadFromCloud(String bucketName, String filename) {
		GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, filename);
		return this.amazonS3Client.getObject(getObjectRequest);
	}
	
	protected String getExtension(String contentType) {
		String extension = "unknown";
		MediaType mediaType = MediaType.parseMediaType(contentType);
		if (MediaType.IMAGE_GIF.isCompatibleWith(mediaType)) {
			extension = "gif";
		}
		if (MediaType.IMAGE_JPEG.isCompatibleWith(mediaType)) {
			extension = "jpg";
		}
		if (MediaType.IMAGE_PNG.isCompatibleWith(mediaType)) {
			extension = "png";
		}
		return extension;
	}
	
	protected String getContentType(String extension) {
		if ("gif".equalsIgnoreCase(extension)) {
			return MediaType.IMAGE_GIF_VALUE;
		}
		if ("jpg".equalsIgnoreCase(extension)) {
			return MediaType.IMAGE_JPEG_VALUE;
		}
		if ("png".equalsIgnoreCase(extension)) {
			return MediaType.IMAGE_PNG_VALUE;
		}
		return MediaType.APPLICATION_OCTET_STREAM_VALUE;
	}
	
	/**
	 * Gets the full path to this local file
	 * @param filename
	 * @return
	 */
	protected File getFile(String filename, String contentType, String sizeName) {
		if (ORIGINAL_SIZE.equalsIgnoreCase(sizeName)) {
			return new File(photosDirectory, filename);
		}
		if (LARGE_SIZE.equalsIgnoreCase(sizeName)) {
			return imageGenerators.getLargeFeatureFilename(filename, contentType);
		}
		if (SMALL_SIZE.equalsIgnoreCase(sizeName)) {
			return imageGenerators.getSmallFeatureFilename(filename, contentType);
		}
		if (TINY_SIZE.equalsIgnoreCase(sizeName)) {
			return imageGenerators.getTinyFeatureFilename(filename, contentType);
		}
		if (AVATAR_SIZE.equalsIgnoreCase(sizeName)) {
			return imageGenerators.getLargeAvatarFilename(filename, contentType);
		}
		if (BOOK_SIZE.equalsIgnoreCase(sizeName)) {
			return imageGenerators.getCoverFilename(filename, contentType);
		}
		throw new RuntimeException("Unknown size: "+sizeName);
	}
	
	/***
	 *  Copy of uniqid in php http://php.net/manual/fr/function.uniqid.php
	 * @param prefix
	 * @param more_entropy
	 * @return
	 */
	protected String uniqid(String prefix, boolean more_entropy)
	{
		long time = System.currentTimeMillis();
		//String uniqid = String.format("%fd%05f", Math.floor(time),(time-Math.floor(time))*1000000);
		//uniqid = uniqid.substring(0, 13);
		String uniqid = "";
		if(!more_entropy)
		{
			uniqid = String.format("%s%08x%05x", prefix, time/1000, time);
		}else
		{
			SecureRandom sec = new SecureRandom();
			byte[] sbuf = sec.generateSeed(8);
			ByteBuffer bb = ByteBuffer.wrap(sbuf);

			uniqid = String.format("%s%08x%05x", prefix, time/1000, time);
			uniqid += "." + String.format("%.8s", ""+bb.getLong()*-1);
		}


		return uniqid ;
	}
	
	protected String uniqid() {
		return uniqid("img", false);
	}
	
	
	
	/**
	 * Worker class to upload a photo to the cloud
	 * 
	 * @author apfesta
	 *
	 */
	abstract class CloudUploadWorker implements Runnable {
				
		final File localFile;
		final String contentType;
		final Long userId;
		final String sizeName;
		final boolean replaceIfExisting;
		
		CloudUploadWorker(File localFile, String contentType, Long userId, String sizeName, boolean replaceIfExisting) {
			this.localFile = localFile;
			this.contentType = contentType;
			this.userId = userId;
			this.sizeName = sizeName;
			this.replaceIfExisting = replaceIfExisting;
		}
		
		abstract File getSourceFile(PersistableFile persistableFile);

		@Override
		public void run() {
			LOG.debug("Working on local file {} size: {}", localFile.getName(), localFile.length());
			try (FileWrapper pFile = new FileWrapper(localFile, contentType)) {
				String filename = getCloudFileName(pFile.getFile(), userId, sizeName);
				if (photosBucket==null) {
					LOG.warn("photoBucket is not set.  Local copies are not uploaded to cloud.");
					File sourceFile = getSourceFile(pFile);
					LOG.info("Created local file {}", new Object[]{sourceFile.getAbsolutePath()});
				} else {
					if (!existsInCloud(photosBucket, filename) || replaceIfExisting) {
						if (replaceIfExisting) {
							LOG.debug("Replacing {} in {}", filename, photosBucket);
						} else {
							LOG.debug("{} not found in {}", filename, photosBucket);
						}
						File sourceFile = getSourceFile(pFile);
						LOG.info("Uploading {} to {}/{}", new Object[]{sourceFile.getAbsolutePath(), photosBucket, filename});
						uploadToCloud(sourceFile, photosBucket, filename);
					} else {
						LOG.debug("{} already exists at {}/{}", new Object[]{localFile.getAbsolutePath(), photosBucket, filename});
					}
					LOG.debug("Finished uploading {} to {}/{}", new Object[]{localFile.getAbsolutePath(), photosBucket, filename});
				}
			} catch (Exception e) {
				LOG.error("Unable to upload "+localFile.getAbsolutePath()+" "+sizeName, e);
			} catch (Throwable t) {
				LOG.error("WTF!", t);
				t.printStackTrace();
			}
		}
		
	}
	
		
	class FutureArray<V> implements Future<V[]> {
		
		Future<V>[] futures;

		@Override
		public boolean cancel(boolean mayInterruptIfRunning) {
			boolean canBeCancelled = true;
			for (Future<V> future: futures) {
				boolean can = future.cancel(mayInterruptIfRunning);
				if (!can) canBeCancelled = false;
			}
			return canBeCancelled;
		}

		@Override
		public boolean isCancelled() {
			boolean cancelled = false;
			for (Future<V> future: futures) {
				boolean can = future.isCancelled();
				if (can) cancelled = true;
			}
			return cancelled;
		}

		@Override
		public boolean isDone() {
			boolean done = true;
			for (Future<V> future: futures) {
				boolean d = future.isDone();
				if (!d) done = false;
			}
			return done;
		}

		@Override
		public V[] get() throws InterruptedException, ExecutionException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public V[] get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
			// TODO Auto-generated method stub
			return null;
		}
		
	}

}
