
package org.prosolo.web.util;

import java.io.File;
import java.util.Date;
import java.util.UUID;

import org.hibernate.Hibernate;
import org.prosolo.app.Settings;
import org.prosolo.common.config.CommonSettings;
import org.prosolo.common.config.services.FileStoreConfig;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.util.ImageFormat;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.services.nodes.DefaultManager;
import org.prosolo.util.StringUtils;

/**
 * @author Zoran Jeremic
 * @date Jul 6, 2012
 */

public class AvatarUtils {

	public static String generateAvatarUrl(String org, String imageType) {
		return "resources/images/users/" + org + "/format/" + UUID.randomUUID() + "." + imageType;
	}
	
	public static String getDefaultAvatarUrl(){
		return CommonSettings.getInstance().config.services.userService.defaultAvatarName;
	}
	
	public static String getAvatarUrlInFormat(User user, ImageFormat format) {
		
		String avatarUrl = null;

		if (user != null) {
			// check if avatar is already full URL
			if (user.getAvatarUrl() != null && user.getAvatarUrl().startsWith("http")) {
				return user.getAvatarUrl();
			}

			if (!Hibernate.isInitialized(user)) {
				user = ServiceLocator.getInstance().getService(DefaultManager.class).merge(user);
			}
			avatarUrl = user.getAvatarUrl();
		} else {
			avatarUrl = getDefaultAvatarUrl();
		}
		return getAvatarUrlInFormat(avatarUrl, format);
	}
	
	public static String getAvatarUrlInFormat(String avatarUrl, ImageFormat format) {
		String url;

		if (avatarUrl == null || avatarUrl.equals("") || avatarUrl.equals(getDefaultAvatarUrl())) {
			url = "/" + CommonSettings.getInstance().config.services.userService.defaultAvatarPath + format + ".png";
		} else {
			FileStoreConfig filesConfig=CommonSettings.getInstance().config.fileStore;
			url = filesConfig.fileStoreServiceUrl + "/" + 
					filesConfig.fileStoreBucketName + "/" +
					CommonSettings.getInstance().config.services.userService.userAvatarPath +
					avatarUrl + "/" + 
					format+".png";
		//	url = Settings.getInstance().config.fileManagement.uploadPath + Settings.getInstance().config.services.userService.userAvatarPath + avatarUrl + File.separator + format + ".png";
			//url = getUrlFromPath(url);
		}
		return url;
	}
	
	public static String getUserFolderPath(User user){
		String hashedUserId = StringUtils.getHashValue(String.valueOf(user.getId()));
		String timestamp = String.valueOf(new Date().getTime());
		return hashedUserId + '/' + timestamp;
	}
	
	public static boolean createDirectoryIfDoesNotExist(String path) {
		File userUploadDir = new File(path);
		
		if (!userUploadDir.exists()) {
			return userUploadDir.mkdirs();
		}
		return true;
	}
	
//	public static int getImageFormatHeight(ImageFormat imageFormat) {
//		String input = imageFormat.toString();
//		String extracted = input.substring(input.indexOf('e') + 1, input.lastIndexOf('x'));
//		int x = Integer.valueOf(extracted);
//		return x;
//	}
	
//	public static int getImageFormatWidth(ImageFormat imageFormat) {
//		String input = imageFormat.toString();
//		String extracted = input.substring(input.indexOf('x') + 1, input.length());
//		int x = Integer.valueOf(extracted);
//		return x;
//	}

	public static String getUrlFromPath(String path) {
		String link = path.replaceFirst(Settings.getInstance().config.fileManagement.uploadPath, "");
		link = Settings.getInstance().config.fileManagement.urlPrefixFolder + link;
		return link;
	}
	
	public static String getPathFromUrl(String url) {
		String path = url.replaceFirst(Settings.getInstance().config.fileManagement.urlPrefixFolder, Settings.getInstance().config.fileManagement.uploadPath);
		return path;
	}
	
}


