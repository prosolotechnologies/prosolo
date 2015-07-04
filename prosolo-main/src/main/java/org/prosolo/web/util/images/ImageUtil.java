package org.prosolo.web.util.images;

import java.io.File;

import org.prosolo.app.Settings;
import org.prosolo.web.activitywall.data.FileType;

public class ImageUtil {

	public static String getFileTypeIcon(String fileName, ImageSize imageSize) {
		FileType fileType = FileType.getFileType(fileName);
		
		return getFileTypeIcon(fileType, imageSize);
	}
	
	public static String getFileTypeIcon(FileType fileType, ImageSize imageSize) {
		String fileTypeName;
		if (fileType == null) {
			fileTypeName= FileType._BLANK.name();
		} else {
			fileTypeName= fileType.name();
		}
		
		String fileTypeIconsLocation = Settings.getInstance().config.fileManagement.imagesConfig.fileTypeIconsPath;
		
		return fileTypeIconsLocation + imageSize + File.separator + fileTypeName.toLowerCase() + ".png";
	}
}