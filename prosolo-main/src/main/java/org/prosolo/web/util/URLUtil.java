/**
 * 
 */
package org.prosolo.web.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

/**
 * @author nikolamilikic
 *
 */
public class URLUtil {

	public static String getDomainName(String url) {
		// look for http://
		int protocolIndex = url.indexOf("http://");
		
		if (protocolIndex == -1) {
			protocolIndex = url.indexOf("https://");
		}
		
		if (protocolIndex == -1) {
			return null;
		}
		
		int firstSlashIndex = url.indexOf("/", protocolIndex+8);
		
		if (firstSlashIndex == -1) {
			// url contains only domain
			return url;
		} else {
			return url.substring(0, firstSlashIndex);
		}
	}
	
	public static String decode(String s) {
		try {
			return URLDecoder.decode(s, "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return "";
	}
	
	public static String getPagePath(String pageUrl) {
		int slashIndex = pageUrl.lastIndexOf("/");
		
		if (slashIndex >= 0) {
			pageUrl = pageUrl.substring(slashIndex+1);
		}
		
		int extensionIndex = pageUrl.indexOf(".xhtml");
		
		if (extensionIndex >= 0) {
			pageUrl = pageUrl.substring(0, extensionIndex);
		} else {
			int qMarkIndex = pageUrl.indexOf("?");
			
			if (qMarkIndex >= 0) {
				pageUrl = pageUrl.substring(0, qMarkIndex);
			}
		}
		return pageUrl;
	}
	
	public static String getContextUrl() {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		String viewId = facesContext.getViewRoot().getViewId();
		String url = ((HttpServletRequest) facesContext.getExternalContext().getRequest()).getRequestURL().toString();
		
		return url.substring(0, url.length() - viewId.length() + 1); // added -1 to include leading slash (/)
	}
	
}
