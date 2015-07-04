package org.prosolo.web.util;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Zoran Jeremic 2013-09-21
 *
 */

public class TwitterUtils {
	public static String parseTweetText(String tweetText) {
	     // Search for URLs
	     if (tweetText!=null && tweetText.contains("http:")) {
	    	    Pattern p = Pattern.compile("http:");
	    	    Matcher m = p.matcher(tweetText);
	    	    HashMap<String,String> urlReplace=new HashMap<String,String>();
	    	    while (m.find()){
	    	       	int indexOfHttp =  	m.start();
	    	        int endPoint = (tweetText.indexOf(' ', indexOfHttp) != -1) ? tweetText.indexOf(' ', indexOfHttp) : tweetText.length();
	    	        String url = tweetText.substring(indexOfHttp, endPoint);
	   	         String targetUrlHtml=  "<a href='"+url+"' target='_blank' class='twitter-url'>"+url+"</a>";
	   	         urlReplace.put(url,targetUrlHtml);
	    	    }
	    	    for(String key:urlReplace.keySet()){
	    	    	tweetText=tweetText.replace(key, urlReplace.get(key));
	    	    }
	    	    
	    	    
	     }
	     String patternStr = "(?:\\s|\\A)[##]+([A-Za-z0-9-_]+)";
	     Pattern pattern = Pattern.compile(patternStr);
	     Matcher matcher = pattern.matcher(tweetText);
	     String result = "";

	     // Search for Hashtags
	     while (matcher.find()) {
	         result = matcher.group();
	         result = result.replace(" ", "");
	         String search = result.replace("#", "");
	         String searchHTML="<a href='https://twitter.com/search?q=%23" + search + "' class='twitter-hashtag'>" + result + "</a>";
	         tweetText = tweetText.replace(result,searchHTML);
	     }

	     // Search for Users
	     patternStr = "(?:\\s|\\A)[@]+([A-Za-z0-9-_]+)";
	     pattern = Pattern.compile(patternStr);
	     matcher = pattern.matcher(tweetText);
	     while (matcher.find()) {
	         result = matcher.group();
	         result = result.replace(" ", "");
	         String rawName = result.replace("@", "");
	         String userHTML="<a href='http://twitter.com/"+rawName+"' class='username'>" + result + "</a>";
	         tweetText = tweetText.replace(result,userHTML);
	     }
	     return tweetText;
	 }

}
