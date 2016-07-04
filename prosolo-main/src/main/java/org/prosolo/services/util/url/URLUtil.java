package org.prosolo.services.util.url;

import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.prosolo.services.media.util.SlideShareUtils;

public class URLUtil {

	public static boolean checkIfSlideshareLink(String link) {
		Pattern pattern = Pattern.compile("(?:https?:\\/\\/)?(?:www\\.)?slideshare\\.net\\/.*",
				Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(link);
		if (matcher.matches()) {
			return true;
		}
		return false;
	}
	
	public static boolean checkIfYoutubeLink(String link) {
		Pattern pattern = Pattern.compile("(?:https?:\\/\\/)?(?:www\\.)?youtu\\.?be(?:\\.com)?\\/?.*(?:watch|embed)?(?:.*v=|v\\/|\\/)([\\w-_]+)",
				Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(link);
		if (matcher.matches()) {
			return true;
		} else {
			return false;
		}
	}
	
	public static String getYoutubeEmbedLink(String link) {
		String embedLink = null;
		String youtubeEmbedLink="https://www.youtube.com/embed/";
		String id = null;
		//
		//^https?://.*(?:youtu.be/|v/|u/\\w/|embed/|watch?v=)([^#&?]*).*$
		Pattern pattern = Pattern.compile("(?:https?:\\/\\/)?(?:www\\.)?youtu\\.?be(?:\\.com)?\\/?.*(?:watch|embed)?(?:.*v=|v\\/|\\/)([\\w-_]+)",
				Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(link);
		if (matcher.matches()) {
			id = matcher.group(1);
			embedLink = youtubeEmbedLink + id +"?rel=0&amp;fs=1";
		} 
		return embedLink;
	}
	
	public static String getSlideshareEmbedLink(String link) {
		return SlideShareUtils.convertSlideShareURLToEmbededUrl(link);
	}
	
	public static String getDomainFromUrl(String url) {
		try {
			String fullUrl = url;
			if(!fullUrl.matches("^(https?|ftp)://.*$")) {
				fullUrl = "http://" + fullUrl;
			}
		    URI uri = new URI(fullUrl);
		    String domain = uri.getHost();
		    return domain.startsWith("www.") ? domain.substring(4) : domain;
		} catch(Exception e) {
			return null;
		}
	}

}
