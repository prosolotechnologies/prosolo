package org.prosolo.util.urigenerator;

import org.prosolo.common.config.CommonSettings;

/**
 *
 * @author Zoran Jeremic, May 28, 2014
 *
 */
public class AmazonS3Utility {
	
	//private static String prefix = CommonSettings.getInstance().config.fileStore.fileStoreServiceUrl +
				//					"/" + CommonSettings.getInstance().config.fileStore.fileStoreBucketName +
				//					"/";
	private static String prefix=CommonSettings.getInstance().config.fileStore.getFilePath();

	public static String createFullPathFromRelativePath(String relativeFilePath){
		return prefix + relativeFilePath;
	}
	
	public static String getRelativeFilePathFromFullS3Path(String fullPath) {
		if (fullPath.startsWith(prefix)) {
			return fullPath.replace(prefix, "");
		}
		return fullPath;
	}
}
