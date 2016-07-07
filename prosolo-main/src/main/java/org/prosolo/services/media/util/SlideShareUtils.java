package org.prosolo.services.media.util;

import org.apache.log4j.Logger;
import org.primefaces.json.JSONException;
import org.primefaces.json.JSONObject;
import org.prosolo.services.util.GetRequestClient;
import org.prosolo.services.util.impl.GetRequestClientImpl;

/**
 * @author Zoran Jeremic 2013-09-30
 *
 */

public class SlideShareUtils {
	private static Logger logger = Logger.getLogger(SlideShareUtils.class);
	public static String convertSlideShareURLToEmbededUrl(String slideShareUrl){
		GetRequestClient client = new GetRequestClientImpl();
		String startingUrl = "http://www.slideshare.net/api/oembed/2?url=";
		String format = "&format=json";
		String url = startingUrl + slideShareUrl + format;
		//http://www.slideshare.net/api/oembed/2?url=http://www.slideshare.net/haraldf/business-quotes-for-2011&format=json
		String jsonString = client.sendRestGetRequest(url);
		if(jsonString==null || jsonString.equals("")){
			return null;
		}
		String slideShareId = null;

		try {
			JSONObject jsonObject = new JSONObject(jsonString);
			
			if (jsonObject.has("slideshow_id")) {
				slideShareId = jsonObject.getString("slideshow_id");
			}
		} catch (JSONException e) {
			logger.error("Exception to convert url:"+slideShareUrl+" json string is:"+jsonString);
			e.printStackTrace();
		}
		
		if (slideShareId == null) {
			return null;
		}
		
		String slideUrl = "http://www.slideshare.net/slideshow/embed_code/"	+ slideShareId;
		return slideUrl;
	}
	
	public static boolean isSlideSharePresentation(String presentationLink){
		if( presentationLink != null && 
				presentationLink.contains("http://www.slideshare.net")) {
			return true;
		} else {
			return false;
		}
	}

}
