package org.prosolo.bigdata.dal.persistence.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.bigdata.dal.persistence.TwitterStreamingDAO;
import org.prosolo.bigdata.twitter.StreamListData;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.activitywall.SocialActivity;
import org.prosolo.common.domainmodel.activitywall.TwitterPostSocialActivity;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.content.TwitterPost;
import org.prosolo.common.domainmodel.organization.VisibilityType;
import org.prosolo.common.domainmodel.user.AnonUser;
import org.prosolo.common.domainmodel.user.ServiceType;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.UserType;
 
 

 


/**
@author Zoran Jeremic Jun 21, 2015
 *
 */

public class TwitterStreamingDAOImpl extends DAOImpl implements TwitterStreamingDAO{

	private static Logger logger = Logger.getLogger(TwitterStreamingDAOImpl.class);
	
	public TwitterStreamingDAOImpl(){
		super();
	}
	

	@Override
	public Map<String, StreamListData> readAllHashtagsAndLearningGoalsIds() {
		System.out.println("read all hashtags and learning goals ids...");
		//EntityManager em = org.prosolo.bigdata.dal.persistence.EntityManagerUtil.getEntityManagerFactory()
				//.createEntityManager();
		String query = 
			"SELECT DISTINCT hashtag.title, lGoal.id " +
			"FROM LearningGoal lGoal " +
			"LEFT JOIN lGoal.hashtags hashtag WHERE hashtag.id > 0";
		
		logger.info("hb query:" + query);
		@SuppressWarnings("unchecked")
		List<Object> result =  getEntityManager().createQuery(query).getResultList();
		
		Map<String, StreamListData> hashtagsLearningGoalIds = new HashMap<String, StreamListData>();
			 
		if (result != null) {
			Iterator<Object> resultIt = result.iterator();
			
			while (resultIt.hasNext()) {
				Object[] object = (Object[]) resultIt.next();
				String title = (String) object[0];
				Long lgId = (Long) object[1];
				if (hashtagsLearningGoalIds.containsKey(title)) {
					hashtagsLearningGoalIds.get(title).addGoalId(lgId);
				} else {
					StreamListData listData = new StreamListData(title);
					listData.addGoalId(lgId);
					hashtagsLearningGoalIds.put("#"+title, listData);
				}
			}
		}
		return hashtagsLearningGoalIds;
	}
	@Override
	public Map<String, List<Long>> readAllUserPreferedHashtagsAndUserIds() {
		//EntityManager em = org.prosolo.bigdata.dal.persistence.EntityManagerUtil.getEntityManagerFactory()
			//	.createEntityManager();
		String query = 
			"SELECT DISTINCT hashtag.title, user.id " +
			"FROM TopicPreference topicPreference " +
			"LEFT JOIN topicPreference.user user " +
			"LEFT JOIN topicPreference.preferredHashtags hashtag  WHERE hashtag.id > 0";
			 
		@SuppressWarnings("unchecked")
		List<Object> result = getEntityManager().createQuery(query).getResultList();
		
		Map<String, List<Long>> hashtagsUserIds = new HashMap<String, List<Long>>();
		
		if (result != null) {
			Iterator<Object> resultIt = result.iterator();
			
			while (resultIt.hasNext()) {
				Object[] object = (Object[]) resultIt.next();
				String title = (String) object[0];
				Long lgId = (Long) object[1];
				if (hashtagsUserIds.containsKey(title)) {
					List<Long> ids = hashtagsUserIds.get(title);
					ids.add(lgId);
				} else {
					List<Long> ids = new ArrayList<Long>();
					ids.add(lgId);
					hashtagsUserIds.put("#"+title, ids);
				}
			}
		}
		//em.close();
		return hashtagsUserIds;
	}
	@Override
	public TwitterPost createNewTwitterPost(User maker, Date created, String postLink, long tweetId, String creatorName,
			String screenName, String userUrl, String profileImage, String text, VisibilityType visibility, 
			Collection<String> hashtags, boolean toSave)  {

		TwitterPost twitterPost = new TwitterPost();
//		twitterPost.setTitle(text);
		twitterPost.setDateCreated(created);
		twitterPost.setLink(postLink);
		twitterPost.setMaker(maker);
		twitterPost.setContent(text);
		twitterPost.setHashtags(getOrCreateTags(hashtags));
		twitterPost.setVisibility(visibility);
		twitterPost.setTweetId(tweetId);
		twitterPost.setCreatorName(creatorName);
		twitterPost.setScreenName(screenName);
		twitterPost.setUserUrl(userUrl);
		twitterPost.setProfileImage(profileImage);		
		if (toSave) {
			twitterPost = (TwitterPost) persist(twitterPost);
		}		
		return twitterPost;
	}
	@Override
	public SocialActivity createTwitterPostSocialActivity(TwitterPost tweet) {
		User actor = tweet.getMaker();
		EventType action = EventType.TwitterPost;
		 
		
		TwitterPostSocialActivity twitterPostSA = new TwitterPostSocialActivity();
		
		if (actor instanceof AnonUser) {
			AnonUser poster = (AnonUser) actor;
			
			twitterPostSA.setName(poster.getName());
			twitterPostSA.setNickname(poster.getNickname());
			twitterPostSA.setProfileUrl(poster.getProfileUrl());
			twitterPostSA.setAvatarUrl(poster.getAvatarUrl());
			twitterPostSA.setUserType(UserType.TWITTER_USER);
		} else {
			twitterPostSA.setMaker(actor);
			twitterPostSA.setUserType(UserType.REGULAR_USER);
		}
		
		twitterPostSA.setPostUrl(tweet.getLink());
		twitterPostSA.setAction(action);
		twitterPostSA.setText(tweet.getContent());
		twitterPostSA.setServiceType(ServiceType.TWITTER);
		twitterPostSA.setDateCreated(tweet.getDateCreated());
		twitterPostSA.setLastAction(tweet.getDateCreated());
		twitterPostSA.setHashtags(tweet.getHashtags());
		twitterPostSA.setVisibility(VisibilityType.PUBLIC);
		
		persist(twitterPostSA);
		return twitterPostSA;
	}
	
	public Set<Tag> getOrCreateTags(Collection<String> titles) {
		Set<Tag> tags = new HashSet<Tag>();
		
		if (titles != null) {
			for (String t : titles) {
				tags.add(getOrCreateTag(t));
			}
		}
		
		return tags;
	}
	public Tag getOrCreateTag(String title) {
		
		Tag tag = getTag(title);

		if (tag != null) {
			return (Tag) merge(tag);
		} else {
			return createTag(title);
		}
	}
	public Tag createTag(String title) {

		Tag newTag = new Tag();
		newTag.setTitle(title);
		newTag = (Tag) persist(newTag);
		return newTag;
	}
	@SuppressWarnings("unchecked")
	public Tag getTag(String title) {
		title = title.toLowerCase();
		
		String query = 
			"SELECT DISTINCT tag " +
			"FROM Tag tag " +
			"WHERE tag.title = :title";
		List<Tag> tags =null;
 
		//try{
			tags =  getEntityManager().createQuery(query).
					setParameter("title", title).getResultList();
		//}catch(NoResultException nre){
			
			if (tags != null && !tags.isEmpty()) {
				return tags.iterator().next();
			}
			return null;
	}
	@Override
	public User getUserByTwitterUserId(long userId) {
		String query = 
				"SELECT user " + 
				"FROM OauthAccessToken userToken " + 
				"WHERE userToken.userId=:userId";
		
		logger.debug("hb query:" + query);
		try{
		return (User) getEntityManager().createQuery(query).setParameter("userId", userId).getSingleResult();
		}catch(NoResultException nre){
			return null;
		}
	}

}

