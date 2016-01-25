package org.prosolo.services.media.util;

import java.nio.charset.Charset;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.prosolo.web.activitywall.data.AttachmentPreview;
import org.prosolo.web.activitywall.data.MediaType;

/**
 * @author Zoran Jeremic 2013-09-30
 *
 */

public class VideoUtils {
	public static String convertEmbedingLinkForYouTubeVideos(AttachmentPreview attachPreview, String videoLink) {
		//String googleYoutubeApiLink = "https://youtube.googleapis.com/v/";
		String youtubeEmbedLink="https://www.youtube.com/embed/";
		String vimeoApiLink = "http://vimeo.com/moogaloop.swf?clip_id=";
		String embedLink = null;
		
		if (videoLink.contains("www.youtube.com")) {
			int paramDelimiter = videoLink.indexOf('?');
			List<NameValuePair> parameters = URLEncodedUtils.parse(videoLink.substring(paramDelimiter + 1), Charset.forName("utf-8"));
			
			for (NameValuePair nameValuePair : parameters) {
				if (nameValuePair.getName().equals("v")) {
					String videoID = nameValuePair.getValue();
					embedLink = youtubeEmbedLink + videoID+"?rel=0&amp;fs=1";
					attachPreview.setId(videoID);
					break;
				}
			}
		} else if (videoLink.contains("vimeo.com")) {
			int index = videoLink.lastIndexOf("/");
			String videoID = videoLink.substring(index + 1);
			attachPreview.setId(videoID);
			embedLink = vimeoApiLink + videoID;
		}
		if (embedLink != null && embedLink.length() > 0) {
			attachPreview.setMediaType(MediaType.VIDEO);
			attachPreview.setEmbedingLink(embedLink);
		} 
		return embedLink;
	}
	
	public static boolean isEmbedableVideo(String videoLink){
		if( videoLink != null && (videoLink.contains("youtube.com")
				|| (videoLink.contains("vimeo.com")))){
			return true;
		}else 
			return false;
	}
}
