package org.prosolo.services.media.util;

import org.apache.log4j.Logger;
import org.primefaces.json.JSONException;
import org.primefaces.json.JSONObject;
import org.prosolo.services.nodes.data.activity.attachmentPreview.MediaData;
import org.prosolo.services.nodes.data.activity.attachmentPreview.MediaType1;
import org.prosolo.services.util.GetRequestClient;
import org.prosolo.services.util.impl.GetRequestClientImpl;

/**
 * @author Zoran Jeremic 2013-09-30
 *
 */

public class SlideShareUtils {
	private static Logger logger = Logger.getLogger(SlideShareUtils.class);
	
	/**
	 * 
	 * @param slideShareUrl
	 * @param embedId - if not known pass null
	 * @return
	 */
	public static MediaData convertSlideShareURLToEmbededUrl(String slideShareUrl, String embedId){
		String presentationId = null;
		if(embedId != null && !embedId.isEmpty()) {
			presentationId = embedId;
		} else {
			GetRequestClient client = new GetRequestClientImpl();
			String startingUrl = "http://www.slideshare.net/api/oembed/2?url=";
			String format = "&format=json";
			String url = startingUrl + slideShareUrl + format;
			//http://www.slideshare.net/api/oembed/2?url=http://www.slideshare.net/haraldf/business-quotes-for-2011&format=json
			String jsonString = client.sendRestGetRequest(url);
			if(jsonString==null || jsonString.equals("")){
				return null;
			}
	
			try {
				JSONObject jsonObject = new JSONObject(jsonString);
				
				if (jsonObject.has("slideshow_id")) {
					presentationId = jsonObject.getString("slideshow_id");
				}
			} catch (JSONException e) {
				logger.error("Exception to convert url:"+slideShareUrl+" json string is:"+jsonString);
				e.printStackTrace();
			}
		}
		
		if (presentationId == null) {
			return null;
		}
		
		String slideUrl = "http://www.slideshare.net/slideshow/embed_code/"	+ presentationId;
		return new MediaData(MediaType1.Slideshare, slideUrl, presentationId);
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
