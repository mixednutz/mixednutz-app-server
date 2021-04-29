/**
 * 
 */
package net.mixednutz.app.server.io.image.impl;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.andrewfesta.io.FileSupport;
import com.andrewfesta.io.OutputStreamCallback;

import net.mixednutz.app.server.io.domain.PersistableFile;
import net.mixednutz.app.server.io.image.ImageGenerator;

/**
 * @author Andy
 *
 */
public abstract class AbstractImageGenerator implements ImageGenerator {
	
	Log logger = LogFactory.getLog(this.getClass());
	
	private File destinationDirectory;
	private File avatarDirectory;
	private File bookCoversDirectory;
	private File tempDirectory;
	
//	private static final String DEFAULT_THUMBNAIL_DIR = "tn";
	private static final String DEFAULT_TINY_FEATURE_DIR = "ty";
	private static final String DEFAULT_SMALL_FEATURE_DIR = "sm";
	private static final String DEFAULT_LARGE_FEATURE_DIR = "lg";
//	private static final String DEFAULT_XLARGE_FEATURE_DIR = "xl";
	private static final String DEFAULT_LARGE_AVATAR_DIR = "avatar_lg";
	private static final String DEFAULT_BOOK_COVER_DIR = "book";
	
	private static final int DEFAULT_THUMBNAIL_SIZE = 99;
	private static final int DEFAULT_SMALL_AVATAR_SIZE = 32;
	private static final int DEFAULT_LARGE_AVATAR_SIZE = 128;
	private static final int DEFAULT_XLARGE_AVATAR_SIZE = 256;
	private static final int DEFAULT_AVATAR_SIZE = 80;
		
	private static final int DEFAULT_TINY_WIDTH = 40;
	private static final int DEFAULT_TINY_HEIGHT = 32;
	private static final int DEFAULT_SMALL_WIDTH = 160;
	private static final int DEFAULT_SMALL_HEIGHT = DEFAULT_SMALL_WIDTH; //was 90 forces this to be square
	private static final int DEFAULT_MEDIUM_WIDTH = 422;
	private static final int DEFAULT_MEDIUM_HEIGHT = 317;
	private static final int DEFAULT_LARGE_WIDTH = 500; //was 600
	private static final int DEFAULT_LARGE_HEIGHT = 400;
	private static final int DEFAULT_COVER_WIDTH = 250;
	private static final int DEFAULT_COVER_HEIGHT = 400;
	
		
	private int thumbnailSize = DEFAULT_THUMBNAIL_SIZE;
	private int avatarSize = DEFAULT_AVATAR_SIZE;
	private int smallAvatarSize = DEFAULT_SMALL_AVATAR_SIZE;
	private int largeAvatarSize = DEFAULT_LARGE_AVATAR_SIZE;
	private int xlargeAvatarSize = DEFAULT_XLARGE_AVATAR_SIZE;
	
	private int mediumFeatureWidth = DEFAULT_MEDIUM_WIDTH;
	private int mediumFeatureHeight = DEFAULT_MEDIUM_HEIGHT;
	private int smallFeatureWidth = DEFAULT_SMALL_WIDTH;
	private int smallFeatureHeight = DEFAULT_SMALL_HEIGHT;
	private int largeFeatureWidth = DEFAULT_LARGE_WIDTH;
	private int largeFeatureHeight = DEFAULT_LARGE_HEIGHT;
	private int tinyFeatureWidth = DEFAULT_TINY_WIDTH;
	private int tinyFeatureHeight = DEFAULT_TINY_HEIGHT;
	private int coverWidth = DEFAULT_COVER_WIDTH;
	private int coverHeight = DEFAULT_COVER_HEIGHT;
		
	protected FileSupport fileSupport = new FileSupport();
	
	public abstract String getExtension();
	
	protected abstract void doWrite(BufferedImage outImage, OutputStream out) throws IOException;
	
	
	public boolean deleteImage(String filename) {
		File dest = new File(destinationDirectory, filename);
		return dest.delete();
	}

	protected String getFilename(String filename, String suffix) {
		
		String baseFilename = filename.substring(0, filename.length()-4).replaceAll("\\s", "");
//		String ext = filename.substring(filename.length()-4);
				
		return baseFilename+suffix+getExtension();
	}

	@Override
	public InputStream resize(final InputStream stream, int width, int height) {
		ByteArrayOutputStream dest = new ByteArrayOutputStream();
		
		scaleImage(stream, dest, width, height, true);
		
		return new ByteArrayInputStream(dest.toByteArray());
	}
	
	public File getRotateFilename(int degrees) {
		String filename = System.currentTimeMillis()+"_"+degrees+"deg";
		return new File(tempDirectory, filename);
	}
	
	public File getCoverFilename(String file) {
		String filename = getFilename(file,"_"+coverWidth+"_"+coverHeight);
		File coverDirectory = new File(bookCoversDirectory, DEFAULT_BOOK_COVER_DIR);
		coverDirectory.mkdirs();
		return new File(coverDirectory, filename);
	}
	
	public File getLargeFeatureFilename(String file) {
		String filename = getFilename(file,"_"+largeFeatureWidth+"_"+largeFeatureHeight);
		File largeFeatureDirectory = new File(destinationDirectory, DEFAULT_LARGE_FEATURE_DIR);
		largeFeatureDirectory.mkdirs();
		return new File(largeFeatureDirectory, filename);
	}
	
	public File getSmallFeatureFilename(String file) {
		String filename = getFilename(file,"_"+smallFeatureWidth+"_"+smallFeatureHeight);
		File smallFeatureDirectory = new File(destinationDirectory, DEFAULT_SMALL_FEATURE_DIR);
		smallFeatureDirectory.mkdirs();
		return new File(smallFeatureDirectory, filename);
	}
	
	public File getTinyFeatureFilename(String file) {
		String filename = getFilename(file,"_"+tinyFeatureWidth+"_"+tinyFeatureHeight);
		File tinyFeatureDirectory = new File(destinationDirectory, DEFAULT_TINY_FEATURE_DIR);
		tinyFeatureDirectory.mkdirs();
		return new File(tinyFeatureDirectory, filename);
	}
	
	public File getLargeAvatarFilename(String file) {
		String filename = getFilename(file,"_"+largeAvatarSize);
		File largeAvatarDirectory = new File(avatarDirectory, DEFAULT_LARGE_AVATAR_DIR);
		largeAvatarDirectory.mkdirs();
		return new File(largeAvatarDirectory, filename);
	}
	
	public File generateCover(final PersistableFile file) {
		File dest = getCoverFilename(file.getFilename());
		
		fileSupport.write(dest, (out)-> {
				try (InputStream is = file.getInputStream()){
					scaleImage(is, out, coverWidth, coverHeight, false);
				} catch (RuntimeException e) {
					throw e;
				} 
			});
				
		return dest;
	}
	
	public File generateLargeFeature(final PersistableFile file) {
		File dest = getLargeFeatureFilename(file.getFilename());
		
		fileSupport.write(dest, new OutputStreamCallback() {

			public void doInOutputStream(OutputStream out) throws IOException {
				try (InputStream is = file.getInputStream()){
					scaleImage(is, out, largeFeatureWidth, largeFeatureHeight, true);
				} catch (RuntimeException e) {
					throw e;
				} 
			}});
		
		return dest;
	}

	public String generateMediumFeature(final PersistableFile file) {
		String filename = getFilename(file.getFilename(),"_"+mediumFeatureWidth+"_"+mediumFeatureHeight);
		File dest = new File(destinationDirectory, filename);
		
		fileSupport.write(dest, new OutputStreamCallback() {

			public void doInOutputStream(OutputStream out) throws IOException {
				InputStream is = file.getInputStream();
				try {
					scaleImage(is, out, mediumFeatureWidth, mediumFeatureHeight, true);
				} catch (RuntimeException e) {
					throw e;
				} finally {
					is.close();
				}
			}});
		
		return filename;
	}

	public File generateSmallFeature(final PersistableFile file) {
		final File dest = getSmallFeatureFilename(file.getFilename());
		
		fileSupport.write(dest, new OutputStreamCallback() {

			public void doInOutputStream(OutputStream out) throws IOException {
				try (InputStream is = file.getInputStream()) {
					scaleImage(is, out, smallFeatureWidth, smallFeatureHeight, true);
				} catch (RuntimeException e) {
					logger.error("Unable to scale image to "+dest.getAbsolutePath());
					throw e;
				} 
			}});
		
		return dest;
	}
	
	public File generateTinyFeature(final PersistableFile file) {
		final File dest = getTinyFeatureFilename(file.getFilename());
		
		fileSupport.write(dest, new OutputStreamCallback() {

			public void doInOutputStream(OutputStream out) throws IOException {
				try (InputStream is = file.getInputStream()) {
					scaleImage(is, out, tinyFeatureWidth, tinyFeatureHeight, true);
				} catch (RuntimeException e) {
					logger.error("Unable to scale image to "+dest.getAbsolutePath());
					throw e;
				} 
			}});
		
		return dest;
	}

	public File generateThumbnail(final PersistableFile file) {
		String filename = getFilename(file.getFilename(),"_"+thumbnailSize);
		File dest = new File(destinationDirectory, filename);
		
		fileSupport.write(dest, new OutputStreamCallback() {

			public void doInOutputStream(OutputStream out) throws IOException {
				InputStream is = file.getInputStream();
				try {
					createThumbnail(is, out, thumbnailSize);
				} catch (RuntimeException e) {
					throw e;
				} finally {
					is.close();
				}
			}});
		
		return dest;
	}
	
	public String generateSmallAvatar(final PersistableFile file) {
		String filename = getFilename(file.getFilename(),"_"+smallAvatarSize);
		File dest = new File(avatarDirectory, filename);
		
		fileSupport.write(dest, new OutputStreamCallback() {

			public void doInOutputStream(OutputStream out) throws IOException {
				InputStream is = file.getInputStream();
				try {
					createThumbnail(is, out, smallAvatarSize);
				} catch (RuntimeException e) {
					throw e;
				} finally {
					is.close();
				}
			}});
		
		return filename;
	}

	public File generateLargeAvatar(final PersistableFile file) {
		File dest = getLargeAvatarFilename(file.getFilename());
		
		fileSupport.write(dest, new OutputStreamCallback() {

			public void doInOutputStream(OutputStream out) throws IOException {
				try (InputStream is = file.getInputStream()) {
					createThumbnail(is, out, largeAvatarSize);
				} catch (RuntimeException e) {
					throw e;
				} 
			}});
		
		return dest;
	}
	
	public String generateXLargeAvatar(final PersistableFile file) {
		String filename = getFilename(file.getFilename(),"_"+xlargeAvatarSize);
		File dest = new File(avatarDirectory, filename);
		
		fileSupport.write(dest, new OutputStreamCallback() {

			public void doInOutputStream(OutputStream out) throws IOException {
				InputStream is = file.getInputStream();
				try {
					createThumbnail(is, out, xlargeAvatarSize);
				} catch (RuntimeException e) {
					throw e;
				} finally {
					is.close();
				}
			}});
		
		return filename;
	}
	
	public String generateAvatar(final PersistableFile file) {
		String filename = getFilename(file.getFilename(),"_"+avatarSize);
		File dest = new File(avatarDirectory, filename);
		
		fileSupport.write(dest, new OutputStreamCallback() {

			public void doInOutputStream(OutputStream out) throws IOException {
				try (InputStream is = file.getInputStream()) {
					createThumbnail(is, out, avatarSize);
				} catch (RuntimeException e) {
					throw e;
				} 
			}});
		
		return filename;
	}
	
	@Override
	public File rotateImage(final InputStream stream, final int angleInDegrees) {
		File dest = this.getRotateFilename(angleInDegrees);
		
		fileSupport.write(dest, new OutputStreamCallback() {
			public void doInOutputStream(OutputStream out) throws IOException {
				rotateImage(stream, out, angleInDegrees);
			}});
		
		return dest;
	}

	/**
	* Reads an image in a file and creates a thumbnail in another file.
	* largestDimension is the largest dimension of the thumbnail, the other dimension is scaled accordingly.
	* Utilises weighted stepping method to gradually reduce the image size for better results,
	* i.e. larger steps to start with then smaller steps to finish with.
	* Note: always writes a JPEG because GIF is protected or something - so always make your outFilename end in 'jpg'.
	* PNG's with transparency are given white backgrounds
	*/
	protected void createThumbnail(InputStream in, OutputStream out, int largestDimension)
	{
		try
		{
			BufferedImage inImage = ImageIO.read(in);
			
			double scale = (double)inImage.getWidth(null) / (double)inImage.getHeight(null);
			double newWidth, newHeight;						
			
			//find biggest dimension	
			if(inImage.getWidth(null) > inImage.getHeight(null))
			{
				newWidth = largestDimension;
				newHeight = newWidth / scale;
			}
			else
			{
				newHeight = largestDimension;
				newWidth = newHeight * scale;
			}
			
			if (logger.isDebugEnabled()) {
				logger.debug("Current Width: "+inImage.getWidth()+
						" Height: "+inImage.getHeight()+
						" Scale: "+scale);
				logger.debug("New Width: "+newWidth+
						" Height: "+newHeight+
						" Scale: "+scale);
			}
			
			BufferedImage outImage = this.getScaledInstance(
					inImage, (int)newWidth, (int)newHeight, 
					RenderingHints.VALUE_INTERPOLATION_BILINEAR, true);
			
			doWrite(outImage, out);
		}
		catch(Exception ex)
		{
			throw new RuntimeException(ex);
		}
	}
	
	protected BufferedImage rotateForAngle(BufferedImage img, double angleInRadians) {
		double sin = Math.abs(Math.sin(angleInRadians));
		double cos = Math.abs(Math.cos(angleInRadians));
		
		int w = img.getWidth();
		int h = img.getHeight();
		
		int newWidth = (int)Math.floor(w*cos + h*sin);
		int newHeight = (int)Math.floor(h*cos + w*sin);
		
		if (logger.isDebugEnabled()) {
			logger.debug("Current Width: "+w+
					" Height: "+h);
			logger.debug("New Width: "+newWidth+
					" Height: "+newHeight+
					" Angle (radians): "+angleInRadians);
		}
		
		Graphics2D g2d;
		final BufferedImage outImage = new BufferedImage((int)newWidth, (int)newHeight, 
				BufferedImage.TYPE_INT_RGB);
		g2d = outImage.createGraphics();
		g2d.setBackground(Color.WHITE);
		g2d.translate((newWidth-w)/2, (newHeight-h)/2);
		g2d.rotate(angleInRadians, w/2, h/2);
		g2d.drawRenderedImage(img, null);
		g2d.dispose();
		return outImage;
	}
	
	protected void rotateImage(InputStream in, OutputStream out, int angleInDegrees) {
		try
		{
			BufferedImage inImage = ImageIO.read(in);

			try {
				
				BufferedImage outImage = this.rotateForAngle(inImage, 
						Math.toRadians(angleInDegrees));
				
				try {
					doWrite(outImage, out);
				} finally {
					outImage = null;
				}
				
			} finally {
				inImage = null;
			}

		}
		catch(IOException ex)
		{
			throw new RuntimeException(ex);
		}
	}
	
	/**
	 * Greater than one if Landscape.  Less than one if Portrait.  One if Square.
	 * @param img
	 * @return
	 */
	protected double currentScale(BufferedImage img) {
		return (double)img.getWidth(null) / (double)img.getHeight(null);
	}
	
	/**
	 * Crops the image if either length needs to be trimmed
	 * 
	 * @param img
	 * @param scale ratio of width/height
	 * @return
	 */
	protected BufferedImage cropForScale(BufferedImage img, 
			double scale) {
		
		double currentScale = currentScale(img);
				
		double newWidth, newHeight;
		double xoffset=0, yoffset=0;
		if (currentScale < scale) {
			// If height needs to be cropped
			newWidth = img.getWidth(null);
			newHeight = (double)img.getWidth(null) / scale;
			yoffset = -((double)img.getHeight()-newHeight)/2;
		} else if (currentScale > scale) {
			// If width needs to be cropped
			newWidth = img.getHeight(null) * scale;
			newHeight = img.getHeight(null);
			xoffset = -((double)img.getWidth()-newWidth)/2;
		} else {
			return img;
		}
		
		if (logger.isDebugEnabled()) {
			logger.debug("Current Width: "+img.getWidth()+
					" Height: "+img.getHeight()+
					" Scale: "+currentScale);
			logger.debug("New Width: "+img.getWidth()+
					" Height: "+newHeight+
					" Offset (x,y): "+xoffset+","+yoffset+
					" Scale: "+scale);
		}
			
		//Crop
		Graphics2D g2d;
		final BufferedImage outImage = new BufferedImage((int)newWidth, (int)newHeight, 
				BufferedImage.TYPE_INT_RGB);
		g2d = outImage.createGraphics();
		g2d.setBackground(Color.WHITE);
		g2d.clearRect(0, 0, outImage.getWidth(), outImage.getHeight());
		AffineTransform tx = new AffineTransform();
		tx.setToIdentity(); //use identity matrix so image is copied exactly
		//g2d.drawImage(img, tx, null);
		g2d.drawImage(img, new AffineTransformOp(tx,null), (int)xoffset, (int)yoffset);
		g2d.dispose();
		g2d = null;
		return outImage;	
		
	}
	
	
	/**
     * Convenience method that returns a scaled instance of the
     * provided {@code BufferedImage}.
     *
     * @param img the original image to be scaled
     * @param targetWidth the desired width of the scaled instance,
     *    in pixels
     * @param targetHeight the desired height of the scaled instance,
     *    in pixels
     * @param hint one of the rendering hints that corresponds to
     *    {@code RenderingHints.KEY_INTERPOLATION} (e.g.
     *    {@code RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR},
     *    {@code RenderingHints.VALUE_INTERPOLATION_BILINEAR},
     *    {@code RenderingHints.VALUE_INTERPOLATION_BICUBIC})
     * @param higherQuality if true, this method will use a multi-step
     *    scaling technique that provides higher quality than the usual
     *    one-step technique (only useful in downscaling cases, where
     *    {@code targetWidth} or {@code targetHeight} is
     *    smaller than the original dimensions, and generally only when
     *    the {@code BILINEAR} hint is specified)
     * @return a scaled version of the original {@code BufferedImage}
     */
    protected BufferedImage getScaledInstance(final BufferedImage img,
                                           int targetWidth,
                                           int targetHeight,
                                           Object hint,
                                           boolean higherQuality)
    {
        int type = (img.getTransparency() == Transparency.OPAQUE) ?
            BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
        BufferedImage ret = (BufferedImage)img;
        int w, h;
        if (higherQuality) {
            // Use multi-step technique: start with original size, then
            // scale down in multiple passes with drawImage()
            // until the target size is reached
            w = img.getWidth();
            h = img.getHeight();
            
            //Dont enlarge!
            if (w < targetWidth && h < targetHeight) {
            	return ret;
            }
        } else {
            // Use one-step technique: scale directly from original
            // size to target size with a single drawImage() call
            w = targetWidth;
            h = targetHeight;
        }
        
        do {
            if (higherQuality && w > targetWidth) {
                w /= 2;
                if (w < targetWidth) {
                    w = targetWidth;
                }
            }

            if (higherQuality && h > targetHeight) {
                h /= 2;
                if (h < targetHeight) {
                    h = targetHeight;
                }
            }
            
            //This prevents the infinite loop if both w and h are less than their targets
            if (higherQuality && w < targetWidth && h < targetHeight) {
            	w = targetWidth;
            	h = targetHeight;
            }
            
            BufferedImage tmp = new BufferedImage(w, h, type);
            Graphics2D g2 = tmp.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
            g2.drawImage(ret, 0, 0, w, h, null);
            g2.dispose();
            g2 = null;
            ret = tmp;
            tmp = null;
        } while (w != targetWidth || h != targetHeight);

        return ret;
    }

	
	
	protected void scaleImage(InputStream in, OutputStream out, int newWidth, int newHeight, boolean crop) {
		
		try
		{
			BufferedImage inImage = ImageIO.read(in);

			try {
				double scale = (double)newWidth/(double)newHeight;
				int adjNewWidth = newWidth;
				int adjNewHeight = newHeight;
				double currentScale = currentScale(inImage);
				if (crop) {
					if (currentScale<1.0) {
						//Swap for Portrait
						scale = (double)newHeight/(double)newWidth;
						adjNewWidth=newHeight;
						adjNewHeight=newWidth;
					} else if (currentScale==1.0) {
						//Keep Square
						scale = 1.0;
						adjNewHeight=newWidth;
					}
				}
				
				BufferedImage outImage = this.getScaledInstance(
						crop?cropForScale(inImage, scale):inImage, adjNewWidth, adjNewHeight, 
						RenderingHints.VALUE_INTERPOLATION_BILINEAR, true);
				
				try {
					doWrite(outImage, out);
				} finally {
					outImage = null;
				}
				
			} finally {
				inImage = null;
			}

		}
		catch(IOException ex)
		{
			throw new RuntimeException(ex);
		}
		
	}
	
	public File getDestinationDirectory() {
		return destinationDirectory;
	}

	public void setDestinationDirectory(File destinationDirectory) {
		this.destinationDirectory = destinationDirectory;
	}

	public int getThumbnailSize() {
		return thumbnailSize;
	}

	public void setThumbnailSize(int thumbnailSize) {
		this.thumbnailSize = thumbnailSize;
	}

	public int getMediumFeatureWidth() {
		return mediumFeatureWidth;
	}

	public void setMediumFeatureWidth(int mediumFeatureWidth) {
		this.mediumFeatureWidth = mediumFeatureWidth;
	}

	public int getSmallFeatureWidth() {
		return smallFeatureWidth;
	}

	public void setSmallFeatureWidth(int smallFeatureWidth) {
		this.smallFeatureWidth = smallFeatureWidth;
	}

	public int getLargeFeatureWidth() {
		return largeFeatureWidth;
	}

	public void setLargeFeatureWidth(int largeFeatureWidth) {
		this.largeFeatureWidth = largeFeatureWidth;
	}

	public FileSupport getFileSupport() {
		return fileSupport;
	}

	public void setFileSupport(FileSupport fileSupport) {
		this.fileSupport = fileSupport;
	}

	public int getMediumFeatureHeight() {
		return mediumFeatureHeight;
	}

	public void setMediumFeatureHeight(int mediumFeatureHeight) {
		this.mediumFeatureHeight = mediumFeatureHeight;
	}

	public int getSmallFeatureHeight() {
		return smallFeatureHeight;
	}

	public void setSmallFeatureHeight(int smallFeatureHeight) {
		this.smallFeatureHeight = smallFeatureHeight;
	}

	public int getLargeFeatureHeight() {
		return largeFeatureHeight;
	}

	public void setLargeFeatureHeight(int largeFeatureHeight) {
		this.largeFeatureHeight = largeFeatureHeight;
	}

	public int getAvatarSize() {
		return avatarSize;
	}

	public void setAvatarSize(int avatarSize) {
		this.avatarSize = avatarSize;
	}

	public File getAvatarDirectory() {
		return avatarDirectory;
	}

	public void setAvatarDirectory(File avatarDirectory) {
		this.avatarDirectory = avatarDirectory;
	}

	public File getBookCoversDirectory() {
		return bookCoversDirectory;
	}

	public void setBookCoversDirectory(File bookCoversDirectory) {
		this.bookCoversDirectory = bookCoversDirectory;
	}

	public File getTempDirectory() {
		return tempDirectory;
	}

	public void setTempDirectory(File tempDirectory) {
		this.tempDirectory = tempDirectory;
	}
	
}
