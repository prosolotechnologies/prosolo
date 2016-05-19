package org.prosolo.web.util;

import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.services.urlencoding.UrlIdEncoder;

public class UrlDbIdEncoder {

	public static String encodeId(long id){
		return ServiceLocator.getInstance().getService(UrlIdEncoder.class).encodeId(id);
	}
	
	public static long decodeId(String id){
		return ServiceLocator.getInstance().getService(UrlIdEncoder.class).decodeId(id);
	}
}
