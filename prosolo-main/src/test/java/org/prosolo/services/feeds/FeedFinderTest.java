package org.prosolo.services.feeds;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;

/**
 * @author Zoran Jeremic Sep 30, 2014
 *
 */

public class FeedFinderTest {
	private static Set<URL> search(Document doc) {
		Set<URL> feeds = new HashSet<URL>();
		Elements links = doc.select("head > link" + "[rel=alternate]" + "[type~=(application/(rss|(x(\\.|\\-))?atom|rdf)\\+|text/)xml]"
				+ "[href~=.+]");
		
		for (Element link : links) {
			try {
				feeds.add(new URL(link.attr("abs:href")));
			} catch (MalformedURLException e) {
				// ignore
			}
		}
		
		return feeds;
	}
	
	public static Set<URL> search(String html) {
		return search(Jsoup.parse(html));
	}
	
	@Test
	public void testFeedFinder() {
		String link = "http://jeremiczoran.wordpress.com/";
		String link2 = "http://wptavern.com/";
		Document doc = null;
		try {
			doc = Jsoup.parse(new URL(link2), 5000);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Set<URL> urls = FeedFinderTest.search(doc);
		
	}
}
