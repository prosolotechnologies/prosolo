package org.prosolo.services.media.util;

import org.apache.log4j.Logger;
import org.primefaces.json.JSONException;
import org.primefaces.json.JSONObject;
import org.prosolo.services.nodes.data.statusWall.MediaData;
import org.prosolo.services.nodes.data.statusWall.MediaType1;
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
	 * @throws LinkParserException
	 */
	public static MediaData convertSlideShareURLToEmbededUrl(String slideShareUrl, String embedId) throws LinkParserException {
		String presentationId = "";
		if(embedId != null && !embedId.isEmpty()) {
			presentationId = embedId;
		} else {
			GetRequestClient client = new GetRequestClientImpl();
			String startingUrl = "https://www.slideshare.net/api/oembed/2?url=";
			String format = "&format=json";
			String url = startingUrl + slideShareUrl + format;
			//https://www.slideshare.net/api/oembed/2?url=http://www.slideshare.net/haraldf/business-quotes-for-2011&format=json
			String jsonString = client.sendRestGetRequest(url);
			
			if (jsonString==null || jsonString.equals("")){
				return null;
			}
	
			try {
				JSONObject jsonObject = new JSONObject(jsonString);
				
				if (jsonObject.has("slideshow_id")) {
					Object id = jsonObject.get("slideshow_id");
					presentationId = String.valueOf(id);
				}
			} catch (JSONException e) {
				e.printStackTrace();
				throw new LinkParserException("Exception retrieving JSON description for:"+slideShareUrl+". Returned json string is:"+jsonString);
			}
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
