package org.prosolo.services.twitter;

import twitter4j.Twitter;
 

/**
 * @author Zoran Jeremic 2014-04-02
 *
 */
public interface TwitterConfigurationManager {

	Twitter getTwitterInstance();

}
