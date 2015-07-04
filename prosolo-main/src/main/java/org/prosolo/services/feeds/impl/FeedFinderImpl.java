package org.prosolo.services.feeds.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.prosolo.services.feeds.FeedFinder;
import org.springframework.stereotype.Service;

import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

/**
 * @author Zoran Jeremic Sep 30, 2014
 *
 */
@Service("org.prosolo.services.feeds.FeedFinder")
public class FeedFinderImpl implements FeedFinder {
	
	private static Logger logger = Logger.getLogger(FeedFinderImpl.class);
	
	@Override
	public Map<String, String> extractFeedsFromBlog(String blogUrl) {
		Map<String, String> feeds = new HashMap<String, String>();

		if (blogUrl == null || blogUrl.isEmpty()) {
			return feeds;
		}
		
		if (!(blogUrl.startsWith("http://")) && !(blogUrl.startsWith("https://"))) {
			blogUrl = "http://" + blogUrl;
		}
		
		Document doc = null;
		
		try {
			doc = Jsoup.parse(new URL(blogUrl), 5000);
		
			Elements linkElements = doc.select("head > link"
										+ "[rel=alternate]"
										+ "[type~=(application/(rss|(x(\\.|\\-))?atom|rdf)\\+|text/)xml]"
										+ "[href~=.+]");

			for (Element linkElem : linkElements) {
				feeds.put(linkElem.attr("title"), linkElem.attr("href"));
			}
		} catch (MalformedURLException e) {
			logger.error("MalformedURLException:" + blogUrl, e);
		} catch (IOException e) {
			logger.error("IOException:" + blogUrl, e);
		} catch (IllegalArgumentException e) {
			logger.error("IllegalArgumentException:" + blogUrl, e);
		}
		return feeds;
	}

	@Override
	public String extractFeedTitle(String feedUrl) throws IOException {
		if (!(feedUrl.startsWith("http://")) && !(feedUrl.startsWith("https://"))) {
			feedUrl = "http://" + feedUrl;
		}
		
		URL url = null;
		
		try {
			url = new URL(feedUrl);
			URLConnection con = url.openConnection();
			con.setConnectTimeout(2000);
			con.setReadTimeout(2000);
			
			@SuppressWarnings("unused")
			InputStream in = con.getInputStream();
		} catch (UnknownHostException ex) {
			logger.error(ex);
		} catch (MalformedURLException e) {
			logger.error(e);
		}
		
		String title = null;
		XmlReader xmlReader = new XmlReader(url);
		
		try {
			SyndFeedInput input = new SyndFeedInput();
			SyndFeed inFeed = input.build(xmlReader);
			
			if (inFeed != null) {
				title = inFeed.getTitle();
			}
		} catch (IllegalArgumentException e) {
			logger.error(e);
		} catch (FeedException e) {
			logger.error(e);
		}
		return title;
	}
}
