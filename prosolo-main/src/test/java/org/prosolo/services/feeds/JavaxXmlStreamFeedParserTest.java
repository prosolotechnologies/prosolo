package org.prosolo.services.feeds;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.prosolo.common.util.date.DateUtil;
//import org.prosolo.services.feeds.data.FeedData;
//import org.prosolo.services.feeds.data.FeedMessageData;
//import org.prosolo.services.feeds.impl.JavaxXmlStreamFeedParser;
import org.prosolo.util.appenders.TestAppender;

/**
 * @author Nikola Milikic
 * @version 0.5
 *
 */
public class JavaxXmlStreamFeedParserTest {
	
	private Logger logger = Logger.getLogger(JavaxXmlStreamFeedParserTest.class);
	
	//private JavaxXmlStreamFeedParser feedParser = new JavaxXmlStreamFeedParser();
	
	private String feedUrl = "http://techcrunch.com/feed/";
	//private FeedData feedData;
	
	private TestAppender appender;
	
	@Before
	public void initLogger() {
		appender = new TestAppender();
		final Logger logger = Logger.getRootLogger();
		logger.addAppender(appender);
	}
	
	@Before
	public void initFeed() {
		logger.info("init");
		Calendar cal = new GregorianCalendar();
		cal.add(Calendar.DATE, -1);
		Date yesterday = DateUtil.getDayBeginningDateTime(cal.getTime());
		
	//	this.feedData = feedParser.readFeed(feedUrl, yesterday);
	}
	
	@Test
	public void testReadFeed() {
		//Assert.assertNotNull(feedData);
	}
	
	@Test
	public void testFeedEntries() {
		//for (FeedMessageData entry : feedData.getEntries()) {
		//	System.out.println(entry);
		//}
	}
	
}
