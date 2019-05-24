package org.prosolo.services.util.url;

import org.prosolo.services.media.util.LinkParserException;
import org.prosolo.services.media.util.SlideShareUtils;
import org.prosolo.services.nodes.data.statusWall.MediaData;

import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

	public static boolean checkIfSlidesharePresentationLink(String link) {
		Pattern pattern = Pattern.compile("(?:https?:\\/\\/)?(?:www\\.)?slideshare\\.net\\/(?:.+)\\/(.+)",
				Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(link);
		if (matcher.matches()) {
			return true;
		}
		return false;
	}
	
	public static boolean checkIfYoutubeLink(String link) {
		Pattern pattern = Pattern.compile("(?:https?:\\/\\/)?(?:www\\.)?youtu\\.?be(?:\\.com)?\\/?.*(?:watch|embed)?(?:.*v=|v\\/|\\/)([\\w-_]+)(&.*)?",
				Pattern.CASE_INSENSITIVE);
		String trimmedLink = link.replaceAll("\u00A0", "").trim();
		Matcher matcher = pattern.matcher(trimmedLink);
		if (matcher.matches()) {
			return true;
		} else {
			return false;
		}
	}
	
	public static String getYoutubeEmbedId(String link) {
		String id = null;
		//^https?://.*(?:youtu.be/|v/|u/\\w/|embed/|watch?v=)([^#&?]*).*$
		Pattern pattern = Pattern.compile("(?:https?:\\/\\/)?(?:www\\.)?youtu\\.?be(?:\\.com)?\\/?.*(?:watch|embed)?(?:.*v=|v\\/|\\/)([\\w-_]+)(&.*)?",
				Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(link);
		if (matcher.matches()) {
			id = matcher.group(1);
		} 
		return id;
	}
	
	/**
	 *
	 * @param link
	 * @param embedId if embedId is not known pass null
	 * @return
	 * @throws LinkParserException
	 */
	public static MediaData getSlideshareEmbedLink(String link, String embedId) throws LinkParserException {
		return SlideShareUtils.convertSlideShareURLToEmbededUrl(link, embedId);
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
