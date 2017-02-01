package org.prosolo.web.courses.activity.util;

import java.io.File;
import java.net.URL;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.prosolo.app.Settings;
import org.prosolo.common.config.CommonSettings;
import org.prosolo.services.nodes.data.ResourceLinkData;
import org.prosolo.util.urigenerator.MD5HashUtility;

public class ActivityUtil {

	private static Logger logger = Logger.getLogger(ActivityUtil.class);
	/**
	 * This method creates temp files for each caption and sets urls to those files
	 * @param captions
	 * @param userId
	 */
	public static void createTempFilesAndSetUrlsForCaptions(List<ResourceLinkData> captions, long userId) {
		for(ResourceLinkData c : captions) {
			try {
				String key = MD5HashUtility.generateKeyForFilename(c.getFetchedTitle());
				File tempFile = new File(Settings.getInstance().config.fileManagement.uploadPath + 
						File.separator + userId + File.separator + key);
				int ind = c.getUrl().lastIndexOf("/");
				
				String url = c.getUrl().substring(0, ind) + "/" + java.net.URLEncoder.encode(c.getFetchedTitle(),"UTF-8");
				FileUtils.copyInputStreamToFile(
						new URL(url).openStream(), tempFile);
				
				String link = tempFile
						.getAbsolutePath()
						.replaceFirst(
								Settings.getInstance().config.fileManagement.uploadPath,
								"");
				link = Settings.getInstance().config.fileManagement.urlPrefixFolder
						+ link;
				link = CommonSettings.getInstance().config.appConfig.domain + link;
				c.setTempUrl(link);
			} catch(Exception e) {
				e.printStackTrace();
				logger.error(e);
			}
		}
	}
}
