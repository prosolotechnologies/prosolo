package org.prosolo.services.feeds.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.prosolo.services.feeds.FeedParser;
import org.prosolo.services.feeds.data.FeedData;
import org.prosolo.services.feeds.data.FeedMessageData;
import org.prosolo.services.htmlparser.impl.JSOUPParser;
import org.springframework.beans.factory.annotation.Autowired;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

/**
 * @author Nikola Milikic
 * @version 0.5
 *
 */
//@Service ("org.prosolo.services.feeds.FeedParser")
public class RomeFeedParser implements FeedParser, Serializable {
	
	private static final long serialVersionUID = 1434145601960654877L;
	
	private Logger logger = Logger.getLogger(RomeFeedParser.class);
	
	@Autowired private JSOUPParser jsoupParser;

	@Override
	public FeedData readFeed(String feedUrl, Date fromDate) {
		FeedData feed = null;
		
		SyndFeedInput input = new SyndFeedInput();
		SyndFeed inFeed = null;

		URLConnection con = null;
		InputStream in = null;
		
		try {
			
			int attempt = 0;
			
			while (attempt < 5) {
				try {
					URL url = new URL(feedUrl);
					con = url.openConnection();
					con.setConnectTimeout(10000);
					con.setReadTimeout(10000);
					in = con.getInputStream();
					
					XmlReader xmlReader = null;
					xmlReader = new XmlReader(url);
					inFeed = input.build(xmlReader);
					break;
				} catch (FeedException e) {
					logger.info("FeedException for:" + feedUrl + e.getLocalizedMessage());
					attempt++;
				}
			}
			
			if (inFeed != null && !inFeed.getEntries().isEmpty()) {
				feed = new FeedData(null, feedUrl, "", "", "");
				
				List<?> syndEntries = inFeed.getEntries();
				
				for (Object object : syndEntries) {
					SyndEntry syndEntry = (SyndEntry) object;
					Date publishedDate = syndEntry.getPublishedDate();
					
					if (fromDate == null || (publishedDate != null && publishedDate.after(fromDate))) {
						FeedMessageData feedEntry = new FeedMessageData();
						feedEntry.setPubDate(publishedDate);
						feedEntry.setTitle(syndEntry.getTitle());
						feedEntry.setLink(syndEntry.getLink());
						
						String description = syndEntry.getDescription().getValue();
						
						if (description != null) {
							Document document = Jsoup.parse(description);
							List<String> images = jsoupParser.getImages(document);
							
							if (images.size() > 0) {
								String imageLink = images.get(0);
								feedEntry.setThumbnail(imageLink);
							}

							String htmlFreeDescription = document.body().text();
							feedEntry.setDescription(htmlFreeDescription);
						}
						
						feed.getEntries().add(feedEntry);
					}
				}
			}
		} catch (IllegalArgumentException e) {
			logger.error("IllegalArgumentException for:" + feedUrl,
					e);
		} catch (IOException e) {
			logger.error("IOException for:" + feedUrl, e);
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (IOException e) {
				logger.error(e);
			}
		}
		return feed;
	}
	
}
