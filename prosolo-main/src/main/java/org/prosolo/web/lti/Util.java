package org.prosolo.web.lti;

import java.util.Map;
import java.util.Map.Entry;

public class Util {

	public static String buildURLWithParams(String baseURL, Map<String,String> params){
		String urlParameters = null;
		if(baseURL.indexOf("?") == -1){
			urlParameters = "?";
		}else{
			urlParameters = "&";
		}
		boolean first = true;
		for(Entry<String, String> entry:params.entrySet()){
			if(!first){
				urlParameters += "&";
			}else{
				first = false;
			}
			String keyValue = entry.getKey()+"="+entry.getValue();
			urlParameters += keyValue;
		}
		
		return baseURL+urlParameters;
	}
}
