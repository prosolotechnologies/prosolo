package org.prosolo.recommendation.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.bigdata.common.exceptions.IndexingServiceNotAvailable;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.common.domainmodel.user.TargetLearningGoal;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.recommendation.CollaboratorsRecommendation;
import org.prosolo.services.es.MoreUsersLikeThis;
import org.prosolo.services.es.RecommendedResourcesSearch;
import org.prosolo.services.interaction.FollowResourceManager;
import org.prosolo.services.logging.LoggingDBManager;
import org.prosolo.services.logging.exception.LoggingException;
import org.prosolo.services.nodes.LearningGoalManager;
import org.prosolo.services.nodes.UserManager;
import org.prosolo.similarity.ResourceTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;

@Service("org.prosolo.recommendation.CollaboratorsRecommendation")
public class CollaboratorsRecommendationImpl implements CollaboratorsRecommendation {
	
	private static Logger logger = Logger.getLogger(CollaboratorsRecommendationImpl.class);
	
	//@Autowired private MoreNodesLikeThis mnlt;
	@Autowired private MoreUsersLikeThis mult;
	@Autowired private ResourceTokenizer resTokenizer;
	@Autowired private LearningGoalManager learningGoalManager; 
	@Autowired private LoggingDBManager loggingDBManager;
	@Autowired private UserManager userManager;
	@Autowired private FollowResourceManager followResourceManager;
	@Autowired private RecommendedResourcesSearch recommendedResourcesSearch;
	
	//private long dayActivityRecommendationGenerated=0;
	//private int hourActivityRecommendationGenerated=0;
	private List<User> mostActiveRecommendedUsers=new ArrayList<User>();
	@Override
	public List<User> getRecommendedCollaboratorsForLearningGoal(User loggedUser, long targetGoalId, int limit) {
		try {
			TargetLearningGoal targetGoal = learningGoalManager.loadResource(TargetLearningGoal.class, targetGoalId);
			String inputText = resTokenizer.getTokenizedStringForUserLearningGoal(loggedUser, targetGoal);
			Collection<User> ignoredUsers = new ArrayList<User>();
			Collection<User> goalMembers = learningGoalManager.retrieveCollaborators(targetGoal.getLearningGoal().getId(), loggedUser);
			
			if (goalMembers != null && !goalMembers.isEmpty()) {
				ignoredUsers.addAll(goalMembers);
			}
			
			ignoredUsers.add(loggedUser);
			List<User> recommendedCollaborators = mult.getRecommendedCollaboratorsForLearningGoal(inputText, ignoredUsers,	limit);
			
			return recommendedCollaborators;
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
		}
		return null;
	}
	@Override
	public List<User> getRecommendedCollaboratorsBasedOnLocation(User loggedUser, int limit) {
		List<User> users = null;
		List<User> ignoredUsers = new ArrayList<User>();
		ignoredUsers.add(loggedUser);
		List<User> followingUsers = followResourceManager.getFollowingUsers(loggedUser);
		for (User fUser : followingUsers) {
			if (!ignoredUsers.contains(fUser)) {
				ignoredUsers.add(fUser);
			}
		}
		
		if (loggedUser.getLatitude() != null && loggedUser.getLatitude() != 0) {
			double lat = Double.valueOf(loggedUser.getLatitude());
			double lon = Double.valueOf(loggedUser.getLongitude());
			users = mult.getCollaboratorsBasedOnLocation(ignoredUsers, lat, lon, limit);
		}
		
		return users;
	}
	
	@Override
	public List<User> getRecommendedCollaboratorsBasedOnSimilarity(User loggedUser, int limit) {
		List<User> ignoredUsers = new ArrayList<User>();
		ignoredUsers.add(loggedUser);
		
		List<User> followingUsers = followResourceManager.getFollowingUsers(loggedUser);
	
		for (User fUser : followingUsers) {
			if (!ignoredUsers.contains(fUser)) {
				ignoredUsers.add(fUser);
			}
		}
		
		String inputText = resTokenizer.getTokenizedStringForUser(loggedUser);
		
		try {
			return mult.getRecommendedCollaboratorsBasedOnSimilarity(inputText, ignoredUsers, limit);
		} catch (IndexingServiceNotAvailable e) {
			logger.error(e);
			return new ArrayList<User>();
		}
	}
	@Override
	public List<User> getMostActiveRecommendedUsers(User loggedUser, int limit){
		List<User> users = new ArrayList<User>();
		List<Long> ignoredUsers = new ArrayList<Long>();
		ignoredUsers.add(loggedUser.getId());
		List<User> followingUsers = followResourceManager.getFollowingUsers(loggedUser);
		
		for (User fUser : followingUsers) {
			if (!ignoredUsers.contains(fUser)) {
				ignoredUsers.add(fUser.getId());
			}
		}
		
		List<Long> userGoalsIds = learningGoalManager.getUserGoalsIds(loggedUser);
		users = recommendedResourcesSearch.findMostActiveRecommendedUsers(loggedUser.getId(), ignoredUsers, userGoalsIds, limit);
		return users;
	}

/*	@Override
	public List<User> getMostActiveRecommendedUsers(User loggedUser, int limit){
		List<User> users = new ArrayList<User>();
		final long daysSinceEpoch = DateUtil.getDaysSinceEpoch();
		
		List<Long> ignoredUsers=new ArrayList<Long>();
		ignoredUsers.add(loggedUser.getId());
		List<User> followingUsers=followResourceManager.getFollowingUsers(loggedUser);
		for(User fUser:followingUsers){
			if(!ignoredUsers.contains(fUser)){
				ignoredUsers.add(fUser.getId());
			}
		}
		List<Long> userGoalsIds=learningGoalManager.getUserGoalsIds(loggedUser);
		//if (limit > mostActiveRecommendedUsers.size()) {
		//	limit = mostActiveRecommendedUsers.size();
		//}
		
		if (!mostActiveRecommendedUsers.isEmpty()) {
			for(User user:mostActiveRecommendedUsers){
				if(!ignoredUsers.contains(user.getId())){
					users.add(user);
				}
				if(users.size()==limit){
					break;
				}
			}
			 
		}

		if (daysSinceEpoch > this.dayActivityRecommendationGenerated) {		
			if(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)>this.hourActivityRecommendationGenerated){
			initializeMostActiveRecommendedUsers();
			this.dayActivityRecommendationGenerated=daysSinceEpoch;
			this.hourActivityRecommendationGenerated=Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
			}
		}
		return users;
	}*/
	
	@Override
	public void initializeMostActiveRecommendedUsers(){
		new Thread(new Runnable() {
			@Override
			public void run() {
				Session session = (Session) userManager.getPersistence().openSession();
				try {
					generateMostActiveRecommendedUsers(DateUtil.getDaysSinceEpoch(), session);
				} catch (LoggingException e) {
					logger.error(e);
				} finally {
					HibernateUtil.close(session);
				}
			 }
		}).start();
	}
	
	private void generateMostActiveRecommendedUsers(long sincedate, Session session) throws LoggingException {
		try {
			long date = sincedate;
			Map<Long, Long> activitySummary = new HashMap<Long, Long>();
			
			for (int i = 0; i < 7; i++) {
				DBCursor dbCursor = loggingDBManager.getMostActiveUsersForDate(date, 20);
				
				while (dbCursor.hasNext()) {
					DBObject userDateLog = dbCursor.next();
					Long userid = Long.valueOf(userDateLog.get("userid").toString());
					Long actNumber = Long.valueOf(userDateLog.get("counter").toString());
					Long summary = actNumber;
					
					if (activitySummary.containsKey(userid)) {
						summary = summary + activitySummary.get(userid);
						activitySummary.remove(userid);
					}
					activitySummary.put(userid, summary);
				}
				date--;
			}
			
			Map<Long, Long> sortedMap = sortByValues(activitySummary);
			
			logger.debug("GENERATE MOST ACTIVE USERS LIST" + sortedMap);
			Set<Long> users = sortedMap.keySet();
			this.mostActiveRecommendedUsers.clear();
			int numbOfRecUsers = 0;
			
			for (Long userid : users) {
				if (userid > 1) {
					User user = null;
					user = (User) session.get(User.class, userid);
					this.mostActiveRecommendedUsers.add(user);
					numbOfRecUsers++;
				}
				
				if (numbOfRecUsers > 10)
					break;
				
			}
		} catch (MongoException me) {
			throw new LoggingException("Mongo store is not available");
		}
	}
	
	public static <K extends Comparable, V extends Comparable> Map<K, V> sortByValues(Map<K, V> map) {
		List<Map.Entry<K, V>> entries = new LinkedList<Map.Entry<K, V>>(map.entrySet());
		
		Collections.sort(entries, new Comparator<Map.Entry<K, V>>() {
			@Override
			public int compare(Entry<K, V> o1, Entry<K, V> o2) {
				return o2.getValue().compareTo(o1.getValue());
			}
		});
		
		// LinkedHashMap will keep the keys in the order they are inserted
		// which is currently sorted on natural ordering
		Map<K, V> sortedMap = new LinkedHashMap<K, V>();
		
		for (Map.Entry<K, V> entry : entries) {
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		
		return sortedMap;
		
	}
 }
