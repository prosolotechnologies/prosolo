package org.prosolo.services.logging;

import javax.servlet.http.HttpServletRequest;

/**
 * 
 * @author Zoran Jeremic, Aug 25, 2014
 * 
 */
public interface AccessResolver {
	
	String findRemoteIPAddress();
	
	String findRemoteIPAddress(HttpServletRequest request);

	String findServerIPAddress();

}
