package org.prosolo.web.activitywall.util;

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

public class PostUtil {

	public static String cleanHTMLTagsExceptBr(String text) {
		return cleanHTMLTags(text, new String[]{"br"});
	}
	
	public static String cleanHTMLTags(String text, String[] exceptions) {
		Whitelist whitelist = Whitelist.none();
        whitelist.addTags(exceptions);

        String safe = Jsoup.clean(text, whitelist);
//        return StringEscapeUtils.unescapeXml(safe);
        return safe;
	}
	
	

}
