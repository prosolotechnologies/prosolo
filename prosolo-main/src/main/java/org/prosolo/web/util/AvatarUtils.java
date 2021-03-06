
package org.prosolo.web.util;

import org.hibernate.Hibernate;
import org.prosolo.app.Settings;
import org.prosolo.common.config.CommonSettings;
import org.prosolo.common.config.services.FileStoreConfig;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.util.ImageFormat;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.services.nodes.DefaultManager;
import org.prosolo.util.StringUtils;

import java.io.File;
import java.util.Date;

/**
 * @author Zoran Jeremic
 * @date Jul 6, 2012
 */

public class AvatarUtils {

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
			return getAvatarUrlInFormat(avatarUrl, format);
		}
		return null;
	}
	
	public static String getAvatarUrlInFormat(String avatarUrl, ImageFormat format) {
		String url;

		if (avatarUrl == null || avatarUrl.equals("")) {
//			url = "/" + CommonSettings.getInstance().config.services.userService.defaultAvatarPath + format + ".png";
			url = null;
		} else {
			FileStoreConfig filesConfig=CommonSettings.getInstance().config.fileStore;
			/*url = filesConfig.fileStoreServiceUrl + "/" +
					filesConfig.fileStoreBucketName + "/" +
					CommonSettings.getInstance().config.services.userService.userAvatarPath +
					avatarUrl + "/" + 
					format+".png";*/
			url = filesConfig.getFilePath()  +
					CommonSettings.getInstance().config.services.userService.userAvatarPath +
					avatarUrl + "/" +
					format+".png";
		//	url = Settings.getInstance().config.fileManagement.uploadPath + Settings.getInstance().config.services.userService.userAvatarPath + avatarUrl + File.separator + format + ".png";
			//url = getUrlFromPath(url);
		}
		return url;
	}
	
	public static String getUserFolderPath(long userId){
		String hashedUserId = StringUtils.getHashValue(String.valueOf(userId));
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
	
	public static String getUrlFromPath(String path) {
		String link = path.replaceFirst(Settings.getInstance().config.fileManagement.uploadPath, "");
		link = Settings.getInstance().config.fileManagement.urlPrefixFolder + link;
		return link;
	}
	
	public static String getPathFromUrl(String url) {
		String urlPrefix = CommonSettings.getInstance().config.appConfig.domain + Settings.getInstance().config.fileManagement.urlPrefixFolder;
		String path = url.replaceFirst(urlPrefix, Settings.getInstance().config.fileManagement.uploadPath);
		return path;
	}
	
	public static String getInitials(String fullName) {
		if (fullName == null || fullName.isEmpty()) {
			return "NN";
		}
		String[] parts = fullName.split(" ");
		
		String initials = "";
		for (String namePart : parts) {
			if (!namePart.isEmpty()) {
				initials += namePart.charAt(0);
			}
		}
		return initials.toUpperCase();
	}
	
}


