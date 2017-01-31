package org.prosolo.web.util;

import org.jsoup.Jsoup;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Whitelist;

public class HTMLUtil {

	public static String cleanHTMLTagsExceptBrA(String text) {
		Whitelist whitelist = Whitelist.none();
        whitelist.addTags(new String[]{"br", "a"});
        whitelist.addAttributes("a", "data-id");

        String safe = Jsoup.clean(text, whitelist);
        return safe;
	}
	
	public static String cleanHTMLTags(String text) {
		String noTags = Jsoup.clean(text, Whitelist.none());
		return Parser.unescapeEntities(noTags, false);
	}

}
