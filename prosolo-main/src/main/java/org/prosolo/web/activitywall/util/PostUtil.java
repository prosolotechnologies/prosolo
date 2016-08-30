package org.prosolo.web.activitywall.util;

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

public class PostUtil {

	public static String cleanHTMLTagsExceptBrA(String text) {
		Whitelist whitelist = Whitelist.none();
        whitelist.addTags(new String[]{"br", "a"});
        whitelist.addAttributes("a", "data-id");

        String safe = Jsoup.clean(text, whitelist);
        return safe;
	}
	
	

}
