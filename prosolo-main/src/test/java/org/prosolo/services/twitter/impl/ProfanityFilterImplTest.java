package org.prosolo.services.twitter.impl;

import org.junit.Test;
import org.prosolo.services.twitter.ProfanityFilter;

/**
 * @author Zoran Jeremic Sep 18, 2014
 *
 */

public class ProfanityFilterImplTest  {

	@Test
	public void testDetectProfanity() {
		ProfanityFilter filter=new ProfanityFilterImpl();
		filter.detectProfanity("xvideos: Blonde teen Britney big tits fuck and hot  #movie #music #ass #lol #video http://t.co/PofBrp9ans");
		filter.detectProfanity("It's a beautiful rockandrol day");
		filter.detectProfanity("I love this song");
		filter.detectProfanity("How do I match a word unless it's surrounded by quotes?");
		filter.detectProfanity("How do I match xyz except in contexts a, b or c?");
		filter.detectProfanity("How do I match ass every word except those on a blacklist (or other contexts)?");
		filter.detectProfanity("How do I ignore all on content that is bolded (… and other contexts)?");
		filter.detectProfanity("It will not butter the reverse side of a toast.");
		filter.detectProfanity(" It will not make small talk with your mother-in-law.");
		filter.detectProfanity("It on your "			//	+ "ability to inspect Group "
				//+ "1 captures (at least in the generic flavor), "
				//+ "so it will not work in a non-programming environment, "
				//+ "such as a text editor's search-and-replace function or a grep command."
				+ "");
		
	}

}
