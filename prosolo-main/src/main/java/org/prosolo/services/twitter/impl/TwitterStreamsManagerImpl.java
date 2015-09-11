package org.prosolo.services.twitter.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.prosolo.common.config.CommonSettings;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.messaging.data.ServiceType;
import org.prosolo.services.annotation.TagManager;
import org.prosolo.services.messaging.SystemMessageDistributer;
import org.prosolo.common.twitter.NotFoundException;
import org.prosolo.common.twitter.PropertiesFacade;
import org.prosolo.services.twitter.TwitterHashtagsQueueHandler;
import org.prosolo.common.twitter.TwitterSiteProperties;
import org.prosolo.common.util.string.StringUtil;
import org.prosolo.services.twitter.TwitterStreamsManager;
import org.prosolo.services.twitter.UserOauthTokensManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import twitter4j.FilterQuery;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.auth.AccessToken;

/**
 * @author Zoran Jeremic 2013-08-11
 * 
 */
@Deprecated
//@Service("org.prosolo.services.twitter.TwitterStreamsManager")
public class TwitterStreamsManagerImpl implements TwitterStreamsManager {

	private static Logger logger = Logger
			.getLogger(TwitterStreamsManagerImpl.class);

	@Autowired private UserOauthTokensManager userOauthTokensManager;
	@Autowired private TagManager tagManager;
	@Autowired private TwitterHashtagsQueueHandler twitterHashtagsQueueHandler;
	@Autowired private SystemMessageDistributer systemMessageDistributer;
	@Autowired @Qualifier("taskExecutor") private ThreadPoolTaskExecutor taskExecutor;

	@SuppressWarnings("unused")
	private boolean started = false;
	private static int STREAMLIMIT = 398;
	private int streamsCounter = 0;

	private Map<Integer, TwitterStream> twitterStreams =new  HashMap<Integer, TwitterStream>(); // collection of streams

	// For hashtags
	private Map<String, StreamListData> hashTagsAndReferences = new HashMap<String, StreamListData>();
	private TwitterStream currentHashTagsStream = null;
	private List<String> currentHashTagsList = new ArrayList<String>();
	private Integer currentHashTagsStreamId = null;
	private Map<Integer, List<String>> streamsAndHashtags = new HashMap<Integer, List<String>>(); // associates
	// stream with list of hashtags followed
	private List<Integer> numberOfChangesInStream = new ArrayList<Integer>();

	// For users
	private Map<Long, Integer> usersAndReferences = new HashMap<Long, Integer>(); // maps all users to streams where they are followed

	private Map<Integer, List<Long>> streamsAndUsers = new HashMap<Integer, List<Long>>();// associates
																							// stream
	// with list of
	// users followed
	private TwitterStream currentUsersStream = null;
	private List<Long> currentUsersList = new ArrayList<Long>();
	private Integer currentUsersStreamId = null;

	@PostConstruct
	public void init() {
		started = true;
	}

	@Override
	public void start() {
		System.out.println("TwitterStreamsManager start...");
		initializeUsersToFollow();
		System.out.println("TwitterStreamsManager start... initialize next");
		initializeHashTagsToFollow();
		System.out.println("TwitterStreamsManager start... start streams for hashtags");
		startStreamsForHashTags();

		@SuppressWarnings("unused")
		TwitterHashtagsStatusConsumer hashtagsStatusConsumer = new TwitterHashtagsStatusConsumer();
	}

	private void authentificateStream(TwitterStream stream, int accountId) {
		TwitterSiteProperties properties = null;
		try {
			properties = new PropertiesFacade()
					.getTwitterSiteProperties(accountId);
		} catch (IllegalArgumentException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		} catch (NotFoundException e) {
			// TODO Auto-generated catch block
			logger.error(e);
		}

		String consumerKey = properties.getConsumerKey();
		String consumerSecret = properties.getConsumerSecret();
		String accessTokenKey = properties.getAccessToken();
		String accessTokenSecret = properties.getAccessTokenSecret();
		AccessToken accessToken = new AccessToken(accessTokenKey,
				accessTokenSecret);
		stream.setOAuthConsumer(consumerKey, consumerSecret);
		stream.setOAuthAccessToken(accessToken);
		logger.debug("Authenticated stream :" + accountId + " with token:"
				+ accessTokenKey);
	}

	private void initializeHashTagsToFollow() {
		System.out.println("initializeHashTagsToFollow called");
		// Adding hashtags from Learning goals
		Map<String, StreamListData> hashTagsLearningGoalIds = tagManager
				.readAllHashtagsAndLearningGoalsIds();
		System.out.println("LG ids:"+hashTagsLearningGoalIds.size());
		this.hashTagsAndReferences.putAll(hashTagsLearningGoalIds);
 
		// Adding hashtags from User profiles
		Map<String, List<Long>> hashTagsUserIds = tagManager
				.readAllUserPreferedHashtagsAndUserIds();
		Set<String> usertags = hashTagsUserIds.keySet();
		System.out.println("User profiles hashtags:"+hashTagsUserIds.size());
		for (String usertag : usertags) {
			List<Long> userIds = hashTagsUserIds.get(usertag);

			if (this.hashTagsAndReferences.containsKey(usertag)) {
				StreamListData listData = this.hashTagsAndReferences
						.get(usertag);

				for (Long uId : userIds) {
					listData.addUserId(uId);
				}
			} else {
				StreamListData listData = new StreamListData(usertag);
				listData.setUsersIds(userIds);
				this.hashTagsAndReferences.put(usertag, listData);
			}
		}
	}

	private void initializeNewCurrentHashTagsListAndStream() {
		System.out.println("initializeNewCurrentHashtagsListAndStream...");
		Integer streamId = createAndAuthentificateNextHashTagsStream();

		initializeHashTagsStream(this.currentHashTagsStream);
		this.streamsAndHashtags.put(streamId, currentHashTagsList);

		
		this.twitterStreams.put(streamId, currentHashTagsStream);
		//this.twitterStreams.add(this.currentHashTagsStream);

	}

	private void startStreamsForHashTags() {
		System.out.println("start streams for hashtags...");
		if (this.currentHashTagsList == null) {
			currentHashTagsList = new ArrayList<String>();
		}
		if (this.currentHashTagsStream == null
				|| currentHashTagsList.size() == 0) {
			initializeNewCurrentHashTagsListAndStream();
		}
		for (String tag : this.hashTagsAndReferences.keySet()) {
			if (tag != null) {
				currentHashTagsList.add(tag);

				if (currentHashTagsList.size() > STREAMLIMIT) {
					initializeNewCurrentHashTagsListAndStream();
					currentHashTagsList = new ArrayList<String>();
				}
			}
			StreamListData streamListData=this.hashTagsAndReferences.get(tag);

			streamListData.setStreamId(this.currentHashTagsStreamId);
		}
		
	}

	private void initializeHashTagsStream(TwitterStream twitterStream) {
		List<String> blacklist = twitterHashtagsQueueHandler
				.getHashtagsBlackList();
		List<String> queries = new ArrayList<String>();

		for (String hashTag : this.currentHashTagsList) {
			if (hashTag != null && !blacklist.contains(hashTag)) {
				if (hashTag.length() > 2) {
					if (!hashTag.startsWith("#")) {
						hashTag = "#" + hashTag;
					}
					queries.add(hashTag);
					System.out.println("added hashtag:"+hashTag);
				}
			}
		}
		String[] trackQueries = (String[]) queries.toArray(new String[queries
				.size()]);
		FilterQuery fq1 = new FilterQuery();
		fq1.track(trackQueries);
		twitterStream.filter(fq1);
	}

	private Integer createAndAuthentificateNextHashTagsStream() {
		System.out.println("Create and authentificate next Hashtags stream");
		this.currentHashTagsStream = createAndAuthentificateNextStream();
		TwitterHashtagsListenerImpl listener = new TwitterHashtagsListenerImpl();
		this.currentHashTagsStream.addListener(listener);
		this.currentHashTagsStreamId = this.streamsCounter - 1;
		return (this.streamsCounter - 1);
	}

	private TwitterStream createAndAuthentificateNextStream() {
		TwitterStream twitterStream = new TwitterStreamFactory().getInstance();
		this.authentificateStream(twitterStream, this.streamsCounter);
		this.streamsCounter++;

		this.numberOfChangesInStream.add(0);
		return twitterStream;
	}

	@Override
	public void addNewHashTagsForLearningGoalAndRestartStream(
			Collection<Tag> newHashTags, long lGoalId) {


		if (CommonSettings.getInstance().config.rabbitMQConfig.distributed
				&& !CommonSettings.getInstance().config.rabbitMQConfig.masterNode) {
			this.updateHashTagsAndRestartStreamOnMasterNode(
					new ArrayList<Tag>(), newHashTags, lGoalId, 0);
		} else {
			if (newHashTags != null && !newHashTags.isEmpty()) {
				boolean changed = false;
				for (Tag newHashTagAnn : newHashTags) {
					String hashTag = newHashTagAnn.getTitle();

					if (this.hashTagsAndReferences.containsKey(hashTag)) {
						StreamListData listData = this.hashTagsAndReferences
								.get(hashTag);
						listData.addGoalId(lGoalId);

					} else {
						StreamListData listData = new StreamListData(hashTag);
						listData.addGoalId(lGoalId);
						this.hashTagsAndReferences.put(hashTag, listData);

						if (this.currentHashTagsList.size() > STREAMLIMIT) {
							initializeNewCurrentHashTagsListAndStream();
							currentHashTagsList = new ArrayList<String>();
						}

						listData.setStreamId(this.currentHashTagsStreamId);
						changed = true;
					}
				}
				if (changed) {
					restartHashTagsStream(this.currentHashTagsStream,
							currentHashTagsList);
				}
			}
		}
	}

	@Override
	public void restartHashTagsStream(TwitterStream twitterStream,
			List<String> hashTagsList) {
		twitterStream.cleanUp();
		List<String> queries = new ArrayList<String>();
		List<String> blacklist = twitterHashtagsQueueHandler
				.getHashtagsBlackList();

		for (String hashTag : hashTagsList) {

			if (!blacklist.contains(hashTag)) {
				if (hashTag.length() > 3) {
					if (!hashTag.startsWith("#")) {
						hashTag = "#" + hashTag;
					}
					queries.add(hashTag);
				}
			}
		}
		logger.debug("QUERIES:" + queries);
		String[] trackQueries = (String[]) queries.toArray(new String[queries
				.size()]);
		FilterQuery fq1 = new FilterQuery();
		fq1.track(trackQueries);
		twitterStream.filter(fq1);
	}

	@Override
	public void updateHashTagsForResourceAndRestartStream(
			Collection<Tag> oldHashtags, Collection<Tag> newHashtags,
			long resourceId) {

		System.out
				.println("update hashtags for resource and restart stream: resourceId:"
						+ resourceId);
		this.updateHashTagsAndRestartStream(oldHashtags, newHashtags,
				resourceId, 0);
	}

	@Override
	public void updateHashTagsForUserAndRestartStream(
			Collection<Tag> oldHashtags, Collection<Tag> newHashtags,
			long userId) {
		this.updateHashTagsAndRestartStream(oldHashtags, newHashtags, 0, userId);
	}

	private void unfollowHashTagsForResourceAndRestartStreamOnMasterNode(
			List<String> hashTags, long lGoalId, long userId) {
		String removedHashTagsCommaSeparated = StringUtil
				.convertToCSV(hashTags);
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("removed", removedHashTagsCommaSeparated);
		parameters.put("added", "");
		parameters.put("goalId", String.valueOf(lGoalId));
		parameters.put("userId", String.valueOf(userId));
		systemMessageDistributer.distributeMessage(
				ServiceType.UPDATEHASHTAGSANDRESTARTSTREAM, parameters);
	}

	private void updateHashTagsAndRestartStreamOnMasterNode(
			Collection<Tag> oldHashtags, Collection<Tag> newHashtags,
			long lGoalId, long userId) {
		List<String> removedHashTags = new ArrayList<String>();
		List<String> addedHashtags = new ArrayList<String>();

		for (Tag oldHashtag : oldHashtags) {
			String hashtag = oldHashtag.getTitle();
			if (!newHashtags.contains(oldHashtag)) {
				removedHashTags.add(hashtag);
			}
		}
		for (Tag newHashtag : newHashtags) {
			String hashtag = newHashtag.getTitle();

			if (!oldHashtags.contains(newHashtag)) {
				addedHashtags.add(hashtag);
			}
		}
		String removedHashTagsCommaSeparated = StringUtil
				.convertToCSV(removedHashTags);
		String addedHashTagsCommaSeparated = StringUtil
				.convertToCSV(addedHashtags);
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("removed", removedHashTagsCommaSeparated);
		parameters.put("added", addedHashTagsCommaSeparated);
		parameters.put("goalId", String.valueOf(lGoalId));
		parameters.put("userId", String.valueOf(userId));
		systemMessageDistributer.distributeMessage(
				ServiceType.UPDATEHASHTAGSANDRESTARTSTREAM, parameters);
	}

	@Override
	public void unfollowHashTagsForResource(List<String> hashTags,
			long lGoalId, long userId) {
		if (CommonSettings.getInstance().config.rabbitMQConfig.distributed
				&& !CommonSettings.getInstance().config.rabbitMQConfig.masterNode) {
			this.unfollowHashTagsForResourceAndRestartStreamOnMasterNode(hashTags,
					lGoalId, userId);
		} else {
			for (String hashtag : hashTags) {
				if (hashTagsAndReferences.containsKey(hashtag)) {
					StreamListData streamListData = this.hashTagsAndReferences
							.get(hashtag);

					if (lGoalId > 0) {
						streamListData.removeLearningGoalId(lGoalId);
					}
					if (userId > 0) {
						streamListData.removeUserId(userId);
					}

					if (streamListData.isFreeToRemove()) {
						this.removeHashTagByStreamListData(streamListData,
								false);
					}
				}
			}
		}
	}

	@Override
	public void updateHashTagsStringsAndRestartStream(
			Collection<String> oldHashtags, Collection<String> newHashtags,
			long lGoalId, long userId) {
		logger.info("updateHashTagsAndRestartStream:" + newHashtags.size()
				+ " lGoalId:" + lGoalId + " userId:" + userId);
		List<String> removedHashTags = new ArrayList<String>();
		List<String> addedHashtags = new ArrayList<String>();

		for (String hashtag : oldHashtags) {
			// String hashtag = oldHashtag.getTitle();
			if (!newHashtags.contains(hashtag)) {
				removedHashTags.add(hashtag);

				if (hashTagsAndReferences.containsKey(hashtag)) {
					StreamListData streamListData = this.hashTagsAndReferences
							.get(hashtag);

					if (newHashtags.size() == 0 && lGoalId > 0) {
						streamListData.removeLearningGoalId(lGoalId);
					}

					if (newHashtags.size() == 0 && userId > 0) {
						streamListData.removeUserId(userId);
					}

					// List<Long> ids = hashTagsAndReferences.get(hashtag);
					if (streamListData.isFreeToRemove()) {
						this.removeHashTagByStreamListData(streamListData,
								false);
					}
				}
			}
		}
		for (String hashtag : newHashtags) {
			// String hashtag = newHashtag.getTitle();

			if (!oldHashtags.contains(hashtag)) {
				addedHashtags.add(hashtag);

				if (hashTagsAndReferences.containsKey(hashtag)) {
					StreamListData streamListData = hashTagsAndReferences
							.get(hashtag);

					if (lGoalId > 0) {
						streamListData.addGoalId(lGoalId);
					}
					if (userId > 0) {
						streamListData.addUserId(userId);
					}
				} else {
					StreamListData streamListData = new StreamListData(hashtag);

					if (lGoalId > 0) {
						streamListData.addGoalId(lGoalId);
					}

					if (userId > 0) {
						streamListData.addUserId(userId);
					}

					streamListData.setStreamId(this.currentHashTagsStreamId);
					hashTagsAndReferences.put(hashtag, streamListData);
					if (hashtag != null) {

						this.currentHashTagsList.add(hashtag);
						this.restartHashTagsStream(this.currentHashTagsStream,
								this.currentHashTagsList);
					}
				}
			}
		}
	}

	@Override
	public void updateHashTagsAndRestartStream(Collection<Tag> oldHashtags,
			Collection<Tag> newHashtags, long lGoalId, long userId) {
		logger.debug("updateHashTagsAndRestartStream:" + newHashtags.size()
				+ " lGoalId:" + lGoalId + " userId:" + userId);
		if (CommonSettings.getInstance().config.rabbitMQConfig.distributed
				&& !CommonSettings.getInstance().config.rabbitMQConfig.masterNode) {
			this.updateHashTagsAndRestartStreamOnMasterNode(oldHashtags,
					newHashtags, lGoalId, userId);
		} else {
			List<String> removedHashTags = new ArrayList<String>();
			List<String> addedHashtags = new ArrayList<String>();

			for (Tag oldHashtag : oldHashtags) {
				String hashtag = oldHashtag.getTitle();

				if (!newHashtags.contains(oldHashtag)) {
					removedHashTags.add(hashtag);
					if (hashTagsAndReferences.containsKey(hashtag)) {
						StreamListData streamListData = this.hashTagsAndReferences
								.get(hashtag);

						if (newHashtags.size() == 0 && lGoalId > 0) {
							streamListData.removeLearningGoalId(lGoalId);
						}
						if (newHashtags.size() == 0 && userId > 0) {
							streamListData.removeUserId(userId);
						}
						// List<Long> ids = hashTagsAndReferences.get(hashtag);
						if (streamListData.isFreeToRemove()) {
							this.removeHashTagByStreamListData(streamListData,
									false);
						}
					}
				}
			}
			for (Tag newHashtag : newHashtags) {
				String hashtag = newHashtag.getTitle();
				if (!oldHashtags.contains(newHashtag)) {
					addedHashtags.add(hashtag);

					if (hashTagsAndReferences.containsKey(hashtag)) {
						StreamListData streamListData = hashTagsAndReferences
								.get(hashtag);

						if (lGoalId > 0) {
							streamListData.addGoalId(lGoalId);
						}

						if (userId > 0) {
							streamListData.addUserId(userId);
						}
					} else {
						StreamListData streamListData = new StreamListData(
								hashtag);

						if (lGoalId > 0) {
							streamListData.addGoalId(lGoalId);
						}

						if (userId > 0) {
							streamListData.addUserId(userId);
						}

						streamListData.setStreamId(this.currentHashTagsStreamId);
						hashTagsAndReferences.put(hashtag, streamListData);
						if (hashtag != null) {
							this.currentHashTagsList.add(hashtag);
							this.restartHashTagsStream(
									this.currentHashTagsStream,
									this.currentHashTagsList);
						}
					}
				}
			}
		}
	}

	private void removeHashTagByStreamListData(StreamListData streamListData,
			boolean forceRestart) {

		Integer streamId = streamListData.getStreamId();
		logger.debug("REMOVING FROM STREAM:" + streamId
				+ " size of streams:" + this.streamsAndHashtags.size()
				+ " twitterStreams.size:" + this.twitterStreams.size());
		List<String> tagsInStream = this.streamsAndHashtags.get(streamId);

		if (tagsInStream.contains(streamListData.getHashtag())) {
			tagsInStream.remove(streamListData.getHashtag());
		}
		TwitterStream streamToUpdate = this.twitterStreams.get(streamId);

		hashTagsAndReferences.remove(streamListData.getHashtag());
		this.streamIdChanged(streamId, forceRestart);
		this.restartHashTagsStream(streamToUpdate, tagsInStream);
	}

	private void streamIdChanged(Integer streamId, boolean forceRestart) {
		Integer numberOfChanges = this.numberOfChangesInStream.get(streamId);
		numberOfChanges++;
		if (numberOfChanges > 0 || forceRestart) {
			numberOfChanges = 0;
			TwitterStream streamToUpdate = this.twitterStreams.get(streamId);
			List<String> tagsInStream = this.streamsAndHashtags.get(streamId);
			this.restartHashTagsStream(streamToUpdate, tagsInStream);
			this.numberOfChangesInStream.set(streamId, numberOfChanges);
		}
	}

	@Override
	public void removeBlackListedHashTag(String hashtag) {
		StreamListData streamListData = this.hashTagsAndReferences.get(hashtag);
		this.removeHashTagByStreamListData(streamListData, true);

	}

	/*
	 * @Override public void unfollowHashTagsForUser(List<String> hashTags, long
	 * userId) { for (String hashtag : hashTags) { if
	 * (hashTagsAndReferences.containsKey(hashtag)) { StreamListData
	 * streamListData = this.hashTagsAndReferences.get(hashtag);
	 * 
	 * if (userId > 0) { streamListData.removeUserId(userId); }
	 * 
	 * if (streamListData.isFreeToRemove()) {
	 * this.removeHashTagByStreamListData(streamListData, false); } } } }
	 */

	// /////////////////////////////////////////////////////////////////////////
	// ///////FOLLOWING USERS ON THE TWITTER
	// ////////////////////////////////////////
	private void initializeUsersToFollow() {

		List<Long> usersToFollow = userOauthTokensManager
				.getAllTwitterUsersTokensUserIds();
		createAndAuthentificateNextUsersStream();

		for (Long userId : usersToFollow) {
			if (!this.currentUsersList.contains(new Long(userId))) {
				this.currentUsersList.add(new Long(userId));
			}

			if (this.currentUsersList.size() > TwitterStreamsManagerImpl.STREAMLIMIT) {
				this.streamsAndUsers.put(this.currentUsersStreamId,
						this.currentUsersList);
				initializeUsersTwitterStream(this.currentUsersStream);
				this.currentUsersList = new ArrayList<Long>();
				createAndAuthentificateNextUsersStream();
			}
			// this.streamsAndUsers.put(streamId, value)
		}

		if (!this.currentUsersList.isEmpty()) {
			initializeUsersTwitterStream(this.currentUsersStream);
		}
	}

	private void initializeUsersTwitterStream(TwitterStream twitterStream) {
		if (this.currentUsersList.size() > 0) {
			long[] followArray = new long[this.currentUsersList.size()];
			int x = 0;

			for (long followId : this.currentUsersList) {
				followArray[x] = followId;
				x++;
			}

			FilterQuery fq1 = new FilterQuery(followArray);
			twitterStream.filter(fq1);
		}
	}

	public void addNewTwitterUserAndRestartStreamLocaly(long twitterId) {
		Long twitterUserId = new Long(twitterId);

		if (!usersAndReferences.containsKey(twitterUserId)) {

			if (this.currentUsersList.size() > TwitterStreamsManagerImpl.STREAMLIMIT) {
				this.usersAndReferences.put(twitterUserId,
						this.currentUsersStreamId);
				this.streamsAndUsers.put(this.currentUsersStreamId,
						this.currentUsersList);
				initializeUsersTwitterStream(this.currentUsersStream);
				this.currentUsersList = new ArrayList<Long>();
				createAndAuthentificateNextUsersStream();
			}
			if (!this.currentUsersList.contains(new Long(twitterUserId))) {
				this.currentUsersList.add(new Long(twitterUserId));
			}

			if (this.currentUsersStream == null) {
				createAndAuthentificateNextUsersStream();
			}
			initializeUsersTwitterStream(this.currentUsersStream);
		}
	}

	@Override
	public void addNewTwitterUserAndRestartStream(long twitterId) {
		if (CommonSettings.getInstance().config.rabbitMQConfig.distributed
				&& !CommonSettings.getInstance().config.rabbitMQConfig.masterNode) {
			Map<String, String> parameters = new HashMap<String, String>();
			parameters.put("twitterId", String.valueOf(twitterId));

			systemMessageDistributer.distributeMessage(
					ServiceType.ADDNEWTWITTERUSERANDRESTARTSTREAM, parameters);
		} else {
			this.addNewTwitterUserAndRestartStreamLocaly(twitterId);
		}
	}

	private void createAndAuthentificateNextUsersStream() {
		this.currentUsersStream = createAndAuthentificateNextStream();
		TwitterStatusListenerImpl listener = new TwitterStatusListenerImpl();
		this.currentUsersStream.addListener(listener);
		this.currentUsersStreamId = this.streamsCounter - 1;
	}

	@Override
	public boolean hasTwitterAccount(long twitterId) {
		return currentUsersList.contains(twitterId);
	}
}
