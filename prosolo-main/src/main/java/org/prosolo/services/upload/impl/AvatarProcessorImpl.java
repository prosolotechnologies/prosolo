package org.prosolo.services.upload.impl;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.prosolo.app.Settings;
import org.prosolo.common.config.CommonSettings;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.util.ImageFormat;
import org.prosolo.services.upload.AmazonS3UploadManager;
import org.prosolo.services.upload.AvatarProcessor;
import org.prosolo.services.upload.ImageUtil;
import org.prosolo.util.FileUtil;
import org.prosolo.web.util.AvatarUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mortennobel.imagescaling.ResampleFilters;
import com.mortennobel.imagescaling.ResampleOp;

/**
zoran
 */
@Service("org.prosolo.services.upload.AvatarProcessor")
public class AvatarProcessorImpl implements AvatarProcessor, Serializable {
	
	private static final long serialVersionUID = -1113141913701527990L;

	private static Logger logger = Logger.getLogger(AvatarProcessorImpl.class);
	
	@Autowired private AmazonS3UploadManager amazonS3UploadManager;

	@Override
	public String storeUserAvatar(User user, InputStream imageInputStream, String avatarFilename, boolean createResizedCopies) throws IOException {
		String userFolder = AvatarUtils.getUserFolderPath(user);
		String avatarUploadFolder = Settings.getInstance().config.fileManagement.uploadPath + CommonSettings.getInstance().config.services.userService.userAvatarPath + userFolder;
		
		String imageExtension = ImageUtil.getExtension(avatarFilename);
		
		if (imageExtension == null) {
			imageExtension = "png";
		}
 
		File originalFile = new File(avatarUploadFolder + '/' + "original.png");
	 	
		// store original
		FileUtils.copyInputStreamToFile(imageInputStream, originalFile);
		String fileType=FileUtil.getFileType(originalFile);
		amazonS3UploadManager.storeInputStreamByKey(new FileInputStream(originalFile), 
		 CommonSettings.getInstance().config.services.userService.userAvatarPath +
				// "images/users/"+
				userFolder + '/' + "original.png",fileType);
		// create resized copies
		createAllResizedCopies(originalFile, avatarUploadFolder, imageExtension, userFolder);
		return userFolder;
	}

	@Override
	public String storeTempAvatar(User user, InputStream imageInputStream, String avatarFilename, int scaledWidth, int scaledHeight) throws IOException {
		String avatarUrl = "temp" + '/' + CommonSettings.getInstance().config.services.userService.userAvatarPath + AvatarUtils.getUserFolderPath(user);
		String avatarUploadFolder = Settings.getInstance().config.fileManagement.uploadPath + avatarUrl;
		
		AvatarUtils.createDirectoryIfDoesNotExist(avatarUploadFolder);
		
		BufferedImage originalBufferedImage = ImageIO.read(imageInputStream);

		int originalWidth = originalBufferedImage.getWidth(null);
		int originalHeight = originalBufferedImage.getHeight(null);
		
		ImageDimensions imageDimensions = getScalledImageDimensions(originalWidth, originalHeight, scaledWidth, scaledHeight);
		
		BufferedImage scaledBI = new BufferedImage(imageDimensions.width, imageDimensions.height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = scaledBI.createGraphics();
		g.setComposite(AlphaComposite.Src);
		
		g.drawImage(originalBufferedImage, 0, 0, imageDimensions.width, imageDimensions.height, null);
		g.dispose();
		
		String imageExtension = ImageUtil.getExtension(avatarFilename);
		if (imageExtension == null) {
			imageExtension = "png";
		}
		
		ImageIO.write(scaledBI, imageExtension, new File(avatarUploadFolder + '/' + avatarFilename));
		
		return avatarUrl + '/' + avatarFilename;
	}
	
	public static ImageDimensions getScalledImageDimensions(int originalWidth, int originalHeight, int targetScalledWidth, int targetScalledHeight) {
		int realScalledWidth = targetScalledWidth;
		int realScalledHeight = targetScalledHeight;
		
		// if not rectangular, recalculate scaled values
		if (originalWidth != originalHeight) {
			if (originalWidth > originalHeight) {
				realScalledHeight = (int) (((double) targetScalledWidth / (double) originalWidth) * originalHeight);
			} else {
				realScalledWidth = (int) (((double) targetScalledHeight / (double) originalHeight) * originalWidth);
			}
		}
		return new ImageDimensions(realScalledWidth, realScalledHeight);
	}
	
	private void createAllResizedCopies(File inputFile, String avatarUploadFolder, String imageExtension, String userFolder) throws IOException{
		BufferedImage originalBufferedImage = ImageIO.read(inputFile);
		
		File userUploadDir = new File(avatarUploadFolder);
		
		if (!userUploadDir.exists()) {
			userUploadDir.mkdirs();
		}
		
		for (ImageFormat imgFormat : ImageFormat.values()) {
			String fileAvatarUrl = avatarUploadFolder + '/' + imgFormat + ".png" /*+ imageExtension*/;
			
			try {
				createResizedCopy(
						originalBufferedImage, 
						fileAvatarUrl, 
						imageExtension, 
						imgFormat.getWidth(), 
						imgFormat.getHeight(), 
						true, userFolder, imgFormat+".png");
				
			} catch (IOException e) {
				logger.error("Error saving resized copy of photo on path "+fileAvatarUrl+
						" in dimension "+imgFormat.getWidth()+" x "+imgFormat.getHeight()+". "+e);
			}
		}
	}
	
	private void createResizedCopy(BufferedImage originalImage,
			String imageUrl,
			String imageExtension,
    		int scaledWidth, int scaledHeight, 
    		boolean preserveAlpha, String userFolder, String imageName) throws IOException {
		
//		int originalWidth = originalImage.getWidth(null);
//		int originalHeight = originalImage.getHeight(null);
//		
//		// if not rectangular, recalculate scaled values
//		if (originalWidth != originalHeight) {
//			if (originalWidth > originalHeight) {
//				scaledHeight = scaledHeight / (originalWidth/originalHeight);
//			} else {
//				scaledWidth = scaledWidth / (originalHeight/originalWidth);
//			}
//		}
//		
//		int imageType = preserveAlpha ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
//		BufferedImage scaledBI = new BufferedImage(scaledWidth, scaledHeight, imageType);
//		Graphics2D g = scaledBI.createGraphics();
//		
//		if (preserveAlpha) {
//			g.setComposite(AlphaComposite.Src);
//		}
//		
//		g.drawImage(originalImage, 0, 0, scaledWidth, scaledHeight, null);
//		g.dispose();
		
//		BufferedImage scaledBI = getScaledInstance(originalImage, scaledWidth, scaledHeight, true);
		
		ResampleOp resampleOp = new ResampleOp(scaledWidth, scaledHeight);
		resampleOp.setFilter(ResampleFilters.getLanczos3Filter());
		resampleOp.setUnsharpenMask(ResampleOp.UnsharpenMask.Soft);
		BufferedImage scaledBI = resampleOp.filter(originalImage, null);
		
//		BufferedImage scaledBI =
//				  Scalr.resize(originalImage, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.AUTOMATIC,
//						  scaledWidth, scaledHeight, Scalr.OP_ANTIALIAS);
		
//		BufferedImage scaledBI = Scalr.resize(originalImage, Scalr.Method.SPEED, Scalr.Mode.FIT_TO_WIDTH,
//				scaledWidth, scaledWidth, Scalr.OP_ANTIALIAS);
		File resizedImage=new File(imageUrl);
		ImageIO.write(scaledBI, imageExtension, resizedImage);
		String fileType=FileUtil.getFileType(resizedImage);
	 	amazonS3UploadManager.storeInputStreamByKey(new FileInputStream(resizedImage), 
	 			CommonSettings.getInstance().config.services.userService.userAvatarPath+  userFolder+'/' +imageName, fileType);
    }
	
	@Override
	public String cropImage(User user, String imagePath, int left, int top, int width, int height) throws IOException{
		BufferedImage originalBufferedImage = ImageIO.read(new File(imagePath));
		BufferedImage dest = originalBufferedImage.getSubimage(left, top, width, height);
		
		String[] pathTokens = imagePath.split("\\.");
		String format =  pathTokens[pathTokens.length - 1];
		
		String userFolder = AvatarUtils.getUserFolderPath(user);
		String avatarUploadFolder = Settings.getInstance().config.fileManagement.uploadPath + CommonSettings.getInstance().config.services.userService.userAvatarPath + userFolder;
		
		AvatarUtils.createDirectoryIfDoesNotExist(avatarUploadFolder);
		
		File destinationFile = new File(avatarUploadFolder + '/' + "original.png");
		ImageIO.write(dest, format, destinationFile);
		
		// create resized copies
		createAllResizedCopies(destinationFile, avatarUploadFolder, format, userFolder);
		return userFolder;
	}
	
	public static BufferedImage getScaledInstance(BufferedImage img, int targetWidth,
			int targetHeight, boolean higherQuality) {
		int type = (img.getTransparency() == Transparency.OPAQUE) ? BufferedImage.TYPE_INT_RGB
				: BufferedImage.TYPE_INT_ARGB;
		BufferedImage ret = (BufferedImage) img;
		int w, h;
		if (higherQuality) {
			// Use multi-step technique: start with original size, then
			// scale down in multiple passes with drawImage()
			// until the target size is reached
			w = img.getWidth();
			h = img.getHeight();
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

			BufferedImage tmp = new BufferedImage(w, h, type);
			Graphics2D g2 = tmp.createGraphics();
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
			g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
			g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
			g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
			g2.drawImage(ret, 0, 0, w, h, null);
			g2.dispose();

			ret = tmp;
		} while (w != targetWidth || h != targetHeight);

		return ret;
	}
	
}
