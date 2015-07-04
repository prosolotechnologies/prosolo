package org.prosolo.util.net;

import java.util.Enumeration;

import javax.servlet.http.HttpSession;

/**
 * @author Nikola Milikic
 * @version 0.5
 *
 */
public class HttpSessionUtil {
	
	public static Object getAttributeEndsWith(HttpSession httpSession, String endsWith) {
		@SuppressWarnings("unchecked")
		Enumeration<String> en = httpSession.getAttributeNames();
		
		while (en.hasMoreElements()) {
			String attr = (String) en.nextElement();
			
			if (attr.endsWith(endsWith)) {
				return httpSession.getAttribute(attr);
			}
		}
		
		return null;
	}
}
