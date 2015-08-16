package org.prosolo.bigdata.twitter;

import java.io.IOException;

import org.junit.Test;
import org.prosolo.common.twitter.NotFoundException;
import org.prosolo.common.twitter.PropertiesFacade;
import org.prosolo.common.twitter.TwitterSiteProperties;

/**
 * @author Zoran Jeremic Jun 20, 2015
 *
 */

public class PropertiesFacadeTest {

	@Test
	public void testGetTwitterSiteProperties() {
		PropertiesFacade pFacade = new PropertiesFacade();
		boolean hasmore = true;
		int accountId = 0;
		while (hasmore) {
			try {
				TwitterSiteProperties twitterSiteProperties = pFacade
						.getTwitterSiteProperties(accountId);
				System.out.println("found:"
						+ twitterSiteProperties.getAccessToken()
						+ " for account:" + accountId);
				accountId++;
			} catch (IllegalArgumentException | IOException e) {
				e.printStackTrace();
			} catch (NotFoundException e) {
				hasmore = false;
			}

		}

	}

}
