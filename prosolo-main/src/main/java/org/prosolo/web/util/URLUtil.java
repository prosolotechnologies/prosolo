/**
 * 
 */
package org.prosolo.web.util;

import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

	public static String getForwardedUriWithoutContextWithAttachedQueryString(String queryStringToAdd) {
		HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
		return getForwardedUriWithoutContextWithAttachedQueryString(req, queryStringToAdd);
	}

	public static String getForwardedUriWithoutContextWithAttachedQueryString(HttpServletRequest request, String queryStringToAdd) {
		String uri = (String) request.getAttribute("javax.servlet.forward.request_uri");
		uri = uri.substring(request.getContextPath().length());
		String queryString = (String) request.getAttribute("javax.servlet.forward.query_string");
		boolean originalQueryStringBlank = StringUtils.isBlank(queryString);
		boolean queryStringToAddBlank = StringUtils.isBlank(queryStringToAdd);
		if (originalQueryStringBlank && queryStringToAddBlank) {
			return uri;
		}
		if (!queryStringToAddBlank && !originalQueryStringBlank) {
			//from original query string remove parameters that are passed in query string to attach
			queryString = removeParamsFromQueryStringAndReturn(queryString, getParamNamesFromQueryString(queryStringToAdd));
			originalQueryStringBlank = StringUtils.isBlank(queryString);
			if (!originalQueryStringBlank) {
				queryStringToAdd = "&" + queryStringToAdd;
			}
		}
		String finalQueryString =
				"?" +
						(originalQueryStringBlank ? "" : queryString) +
						(queryStringToAddBlank ? "" : queryStringToAdd);
		return uri + finalQueryString;
	}

	public static String[] getParamNamesFromQueryString(String queryString) {
		if (StringUtils.isBlank(queryString)) {
			return null;
		}
		List<String> paramsToReturn = new ArrayList<>();
		Matcher m = Pattern.compile("(?<=(?:^|&)).*?(?==)").matcher(queryString);
		while (m.find()) {
			paramsToReturn.add(m.group());
		}
		return paramsToReturn.toArray(new String[0]);
	}

	public static String removeParamsFromQueryStringAndReturn(String queryString, String... paramsToRemove) {
		if (StringUtils.isBlank(queryString) || paramsToRemove == null || paramsToRemove.length == 0) {
			return queryString;
		}
		String paramsToRemoveAlternatives = String.join("|", paramsToRemove);
		String regex = "(?<=(?:^|&))(?:"+ paramsToRemoveAlternatives + ")(?:=.*?)(?:$|&)";
		String res = queryString.replaceAll(regex, "");
		res = res.trim();
		//remove trailing '&' which is present if last param was removed
		return res.endsWith("&") ? res.substring(0, res.length() - 1) : res;
	}
	
}
