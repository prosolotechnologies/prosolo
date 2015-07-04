package org.prosolo.services.logging;

/**
 * 
 * @author Zoran Jeremic, Aug 25, 2014
 * 
 */
public interface AccessResolver {
	
	String findRemoteIPAddress();

	String findServerIPAddress();

}
