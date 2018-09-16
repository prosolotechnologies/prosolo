package org.prosolo.web.util;

import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.services.idencoding.IdEncoder;
import org.prosolo.services.urlencoding.UrlIdEncoder;

public class UserIdEncoder {

	public static String encodeId(long id){
		return ServiceLocator.getInstance().getService(IdEncoder.class).encodeId(id);
	}

}
