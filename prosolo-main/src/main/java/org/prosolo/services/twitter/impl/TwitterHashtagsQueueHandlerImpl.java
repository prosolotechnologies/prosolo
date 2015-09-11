package org.prosolo.services.twitter.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;
import org.prosolo.app.Settings;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.config.TwitterStreamConfig;
import org.prosolo.core.spring.ServiceLocator;
import org.prosolo.services.twitter.TwitterHashtagsQueueHandler;
import org.prosolo.services.twitter.TwitterStreamsManager;
import org.springframework.stereotype.Service;

import twitter4j.HashtagEntity;
import twitter4j.Status;

/**
 * @author Zoran Jeremic 2013-10-14
 *
 */
@Service("org.prosolo.services.twitter.TwitterHashtagsQueueHandler")
public class TwitterHashtagsQueueHandlerImpl implements TwitterHashtagsQueueHandler {
	
	private static Logger logger = Logger.getLogger(TwitterHashtagsQueueHandler.class);
	
	public static Queue<Status> queue = new ConcurrentLinkedQueue<Status>();
	public static Map<String, Integer> lastPosition = new HashMap<String, Integer>();
	public static Map<String, Integer> tagsTracker = new HashMap<String, Integer>();
	public static int REDUCED_QUEUE_SIZE;
	public static int FILTER_DENSITY_SIZE;
	public static int FULLFILTERING_DENSITY_TRASH_SECONDS;
	public static int FULLFILTERING_NUMBER_CHECK;
	public static int BLACK_LIST_ENTER_CRITERIA;
	public static List<Date> fullFiltering = new ArrayList<Date>();
	public static boolean tracking = false;
	public static List<String> blackList = null;

	public int QUEUE_SIZE = 0;

	private String blackListPath = null;

	private void initializeSettings() {
		TwitterStreamConfig twitterStreamConfig = Settings.getInstance().config.twitterStreamConfig;

		QUEUE_SIZE = twitterStreamConfig.queueSize;
		REDUCED_QUEUE_SIZE = twitterStreamConfig.reducedQueueSize;
		FILTER_DENSITY_SIZE = twitterStreamConfig.filterDensitySize;
		FULLFILTERING_DENSITY_TRASH_SECONDS = twitterStreamConfig.fullFilteringDensityTrashSeconds;
		FULLFILTERING_NUMBER_CHECK = twitterStreamConfig.fullFilteringNumberCheck;
		BLACK_LIST_ENTER_CRITERIA = twitterStreamConfig.blackListEnterCriteria;
	}
	
    public TwitterHashtagsQueueHandlerImpl(){
		initializeSettings();
		
		blackListPath = Settings.getInstance().getAbsoluteConfigPath() + "twitter4jblacklist.sav";
		
		File blacklistFile = new File(blackListPath);
		blackList = new ArrayList<String>();
		
		if (!blacklistFile.exists()) {
			OutputStream output = null;
			InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream("org/prosolo/files/twitter4jblacklist.sav");
			
			try {
				output = new FileOutputStream(blacklistFile);
				byte[] buf = new byte[1024];
				int bytesRead;

				while ((bytesRead = input.read(buf)) > 0) {
					output.write(buf, 0, bytesRead);
				}
			//	blacklistFile.createNewFile();
			} catch (IOException e) {
				logger.error(e);
			}
			
			try {
				input.close();
				output.close();
			} catch (IOException e) {
				logger.error(e);
			}
		} else {
			Scanner s;
			try {
				s = new Scanner(blacklistFile);
				
				while (s.hasNext()) {
					String tag = s.next();
					
					if (tag != null && tag.length() > 0) {
						blackList.add(tag);
					}
				}
				s.close();
			} catch (FileNotFoundException e) {
				logger.error(e);
			}
		}
     }

	@Override
	public void enqueue(Status object) {
		synchronized (queue) {
			queue.add(object);
			queue.notifyAll();
			
			if (queue.size() > QUEUE_SIZE) {
				performTweetsFiltering();
			}
		}
	}
	
	private void performTweetsFiltering() {
		Iterator<Status> queueIt = queue.iterator();
		int pos = 0;
		
		while (queueIt.hasNext()) {
			Status status = queueIt.next();
			HashtagEntity[] hashtagEntities = status.getHashtagEntities();
			// List<String> hashtags = new ArrayList<String>();

			for (HashtagEntity htEntity : hashtagEntities) {
				// hashtags.add(htEntity.getText().toLowerCase());
				String tag = htEntity.getText().toLowerCase();
				trackHashtag(tag);
				
				if (lastPosition.containsKey(tag)) {
					Integer position = lastPosition.get(tag);
					lastPosition.remove(tag);
					
					if (position > FILTER_DENSITY_SIZE) {
						queueIt.remove();
						break;
					}
				}
				lastPosition.put(tag, pos);

				if (blackList.contains(tag)) {
					lastPosition.remove(tag);
				}
			}
			pos++;
		}
		
		if (queue.size() > REDUCED_QUEUE_SIZE) {
			queue = new ConcurrentLinkedQueue<Status>();
			
			if (fullFiltering.size() > FULLFILTERING_NUMBER_CHECK) {
				Date lastFiltering = fullFiltering.get(fullFiltering.size() - 1);
				
				long seconds = DateUtil.getSecondsDifference(new Date(),lastFiltering);
				
				if (seconds < FULLFILTERING_DENSITY_TRASH_SECONDS) {
					tracking = true;
				}
			}
			fullFiltering.add(new Date());
		}
	}
	
	public void trackHashtag(String hashtag) {
		if (tracking) {
			if (tagsTracker.containsKey(hashtag) && !blackList.contains(hashtag)) {
				
				Integer number = tagsTracker.get(hashtag);
				
				if (number > BLACK_LIST_ENTER_CRITERIA) {
					addToBlackList(hashtag);
					tracking = false;
					tagsTracker = new HashMap<String, Integer>();
					ServiceLocator.getInstance().getService(TwitterStreamsManager.class).removeBlackListedHashTag(hashtag);
				}
				number = number + 1;
				tagsTracker.remove(hashtag);
				tagsTracker.put(hashtag, number);
			} else {
				tagsTracker.put(hashtag, 1);
			}
		}
	}
	
	private void addToBlackList(String hashtag) {
		blackList.add(hashtag);
		Writer output;
		try {
			output = new BufferedWriter(new FileWriter(blackListPath, true));
			output.append("\r\n");
			output.append(hashtag);
			output.close();
		} catch (IOException e) {
			logger.error("ERROR appending Twitter blacklist:" + e.getLocalizedMessage());
		}
	}

	@Override
	public Status dequeue() {
		synchronized (queue) {
			while (queue.isEmpty()) {
				try {
					queue.wait();
				} catch (InterruptedException ignored) { }
			}
			return queue.remove();
		}
	}
    
	@Override
	public boolean isHashTagBlacklisted(String tag) {
		if (tag.startsWith("#")) {
			tag.replaceFirst("#", "");
		}
		if (blackList.contains(tag)) {
			return true;
		} else {
			return false;
		}
	}
	
	
	@Override
	public List<String> getHashtagsBlackList(){
		return blackList;
	}
}
