package org.prosolo.services.feeds.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.Date;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.log4j.Logger;
import org.prosolo.services.feeds.FeedParser;
import org.prosolo.services.feeds.data.FeedData;
import org.prosolo.services.feeds.data.FeedMessageData;
import org.prosolo.util.date.DateUtil;
import org.prosolo.util.string.StringUtil;
import org.springframework.stereotype.Service;

/**
 * Code taken from tutorial at URL http://www.vogella.com/tutorials/RSSFeed/article.html and adjusted as needed.
 * Source code in this tutorial is distributed under the Eclipse Public License.
 * 
 * @version 0.5
 *
 */
@Service ("org.prosolo.services.feeds.FeedParser")
public class JavaxXmlStreamFeedParser implements FeedParser, Serializable {
	
	private static final long serialVersionUID = 4793726993322891286L;
	
	private Logger logger = Logger.getLogger(JavaxXmlStreamFeedParser.class);
	
	private static final int TOTAL_ATTEMPTS = 5;
	
	private static final String TITLE = "title";
	private static final String DESCRIPTION = "description";
	private static final String LANGUAGE = "language";
	private static final String COPYRIGHT = "copyright";
	private static final String THUMBNAIL = "thumbnail";
	private static final String LINK = "link";
	private static final String AUTHOR = "author";
	private static final String ITEM = "item";
	private static final String PUB_DATE = "pubDate";
	
	// Sun, 07 Jun 2015 15:36:59 +0000
	private String dateFormat = "EEE, dd MMM yyyy HH:mm:ss z";
	
	public FeedData readFeed(String feedUrl, Date fromDate) {
		logger.info("Parsing RSS feed entries from the feed: " + feedUrl);
		
		FeedData feed = new FeedData();
		int attempts = 0;
		
		while (attempts < TOTAL_ATTEMPTS) {
			attempts++;
			logger.info("Attempt no " + attempts);
			
			try {
				URL url = new URL(feedUrl);
				
				boolean isFeedHeader = true;
				// Set header values intial to the empty string
				String description = "";
				String title = "";
				String link = "";
				String language = "";
				String copyright = "";
				String author = "";
				Date pubdate = null;
				String thumbnail = "";
				
				// First create a new XMLInputFactory
				XMLInputFactory inputFactory = XMLInputFactory.newInstance();
				
				// Setup a new eventReader
				InputStream in = url.openStream();
				
				XMLEventReader eventReader = inputFactory.createXMLEventReader(in);
				
				// read the XML document
				while (eventReader.hasNext()) {
					XMLEvent event = eventReader.nextEvent();
					
					if (event.isStartElement()) {
						String localPart = event.asStartElement().getName().getLocalPart();
						String prefix = event.asStartElement().getName().getPrefix();
						
						if (prefix.length() > 0 && !localPart.equals(THUMBNAIL)) {
							continue;
						}
						
						switch (localPart) {
							case ITEM:
								if (isFeedHeader) {
									isFeedHeader = false;
									feed.setTitle(title);
									feed.setLink(link);
									feed.setDescription(description);
									feed.setLanguage(language);
									feed.setCopyright(copyright);
								}
								event = eventReader.nextEvent();
								break;
							case TITLE:
								title = getCharacterData(event, eventReader);
								break;
							case DESCRIPTION:
								description = getCharacterData(event, eventReader);
								description = StringUtil.cleanHtml(description);
								description = description.trim();
								
								if (description.endsWith("Read More")) {
									description = description.substring(0, description.lastIndexOf("Read More"));
									description = description.trim();
								}
								break;
							case LINK:
								link = getCharacterData(event, eventReader);
								break;
							case LANGUAGE:
								language = getCharacterData(event, eventReader);
								break;
							case AUTHOR:
								author = getCharacterData(event, eventReader);
								break;
							case PUB_DATE:
								String pubdateString = getCharacterData(event, eventReader);
								
								pubdate = DateUtil.parseDate(pubdateString, dateFormat);
								
								break;
							case COPYRIGHT:
								copyright = getCharacterData(event, eventReader);
								break;
							case THUMBNAIL:
								thumbnail = getUrlData(event, eventReader);
								break;
						}
					} else if (event.isEndElement()) {
						if (event.asEndElement().getName().getLocalPart() == (ITEM)) {
							FeedMessageData message = new FeedMessageData();
							message.setAuthor(author);
							message.setDescription(description);
							message.setLink(link);
							message.setTitle(title);
							message.setThumbnail(thumbnail);
							message.setPubDate(pubdate);
							
							if (fromDate == null || pubdate.after(fromDate)) {
								feed.getEntries().add(message);
							}
							
							event = eventReader.nextEvent();
							
							description = "";
							title = "";
							link = "";
							language = "";
							copyright = "";
							author = "";
							pubdate = null;
							thumbnail = "";
							
							continue;
						}
					}
				}
			} catch (XMLStreamException e) {
				logger.error("Error parsing feed source: " + feedUrl, e);
				continue;
			} catch (IOException e) {
				logger.error("Error parsing feed source: " + feedUrl, e);
				continue;
			}
			
			break;
		}

		return feed;
	}
	
	private String getCharacterData(XMLEvent event, XMLEventReader eventReader) throws XMLStreamException {
		String result = "";
		event = eventReader.nextEvent();
		
		while (event instanceof Characters) {
			result += event.asCharacters().getData();
			
			event = eventReader.nextEvent();
		}
		return result;
	}
	
	private String getUrlData(XMLEvent event, XMLEventReader eventReader) throws XMLStreamException {
		StartElement startElem = (StartElement) event;
		
		return startElem.getAttributeByName(new QName("url")).getValue();
	}
	
}
