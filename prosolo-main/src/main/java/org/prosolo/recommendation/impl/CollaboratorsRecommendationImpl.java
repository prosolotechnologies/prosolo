package org.prosolo.recommendation.impl;

import org.prosolo.recommendation.CollaboratorsRecommendation;
import org.springframework.stereotype.Service;

/**
 * @author Zoran Jeremic
 * @version 0.5
 * @deprecated since 0.7
 */
@Deprecated
@Service("org.prosolo.recommendation.CollaboratorsRecommendation")
public class CollaboratorsRecommendationImpl 
	implements CollaboratorsRecommendation 
{
	
//	private static Logger logger = Logger.getLogger(CollaboratorsRecommendationImpl.class);
//	
//	@Inject
//	private MoreUsersLikeThis mult;
//	@Inject
//	private ResourceTokenizer resTokenizer;
//	@Inject
//	private LearningGoalManager learningGoalManager;
//	@Inject
//	private FollowResourceManager followResourceManager;
//	@Inject
//	private RecommendedResourcesSearch recommendedResourcesSearch;
//	
//	@Override
//	public List<User> getRecommendedCollaboratorsForLearningGoal(long userId, long targetGoalId, int limit) {
//		try {
//			TargetLearningGoal targetGoal = learningGoalManager.loadResource(TargetLearningGoal.class, targetGoalId);
//			String inputText = resTokenizer.getTokenizedStringForUserLearningGoal(targetGoal);
//			Collection<Long> ignoredUsers = new ArrayList<Long>();
//			Collection<User> goalMembers = learningGoalManager.retrieveCollaborators(targetGoal.getLearningGoal().getId(), userId);
//			
//			if (goalMembers != null && !goalMembers.isEmpty()) {
//				for (User goalMember : goalMembers) {
//					ignoredUsers.add(goalMember.getId());
//				}
//			}
//			ignoredUsers.add(userId);
//			
//			List<User> recommendedCollaborators = mult.getRecommendedCollaboratorsForLearningGoal(inputText, ignoredUsers,	limit);
//			
//			return recommendedCollaborators;
//		} catch (ResourceCouldNotBeLoadedException e) {
//			logger.error(e);
//		}
//		return null;
//	}
//	@Override
//	public List<User> getRecommendedCollaboratorsBasedOnLocation(long userId, int limit) throws ResourceCouldNotBeLoadedException {
//		User user = learningGoalManager.loadResource(User.class, userId);
//		
//		List<User> users = null;
//		List<User> ignoredUsers = new ArrayList<User>();
//		
//		ignoredUsers.add(user);
//		
//		List<User> followingUsers = followResourceManager.getFollowingUsers(userId);
//		for (User fUser : followingUsers) {
//			if (!ignoredUsers.contains(fUser)) {
//				ignoredUsers.add(fUser);
//			}
//		}
//		
//		if (user.getLatitude() != null && user.getLatitude() != 0) {
//			double lat = Double.valueOf(user.getLatitude());
//			double lon = Double.valueOf(user.getLongitude());
//			users = mult.getCollaboratorsBasedOnLocation(ignoredUsers, lat, lon, limit);
//		}
//		if(users==null){
//			System.out.println("USERS IS NULL");
//		}
//		return users;
//	}
//	
//	@Override
//	public List<User> getRecommendedCollaboratorsBasedOnSimilarity(long userId, int limit) throws ResourceCouldNotBeLoadedException {
//		User user = learningGoalManager.loadResource(User.class, userId);
//		
//		List<User> ignoredUsers = new ArrayList<User>();
//		ignoredUsers.add(user);
//		
//		List<User> followingUsers = followResourceManager.getFollowingUsers(userId);
//	
//		for (User fUser : followingUsers) {
//			if (!ignoredUsers.contains(fUser)) {
//				ignoredUsers.add(fUser);
//			}
//		}
//		
//		String inputText = resTokenizer.getTokenizedStringForUser(user);
//		
//		try {
//			return mult.getRecommendedCollaboratorsBasedOnSimilarity(inputText, ignoredUsers, limit);
//		} catch (IndexingServiceNotAvailable e) {
//			logger.warn(e);
//			return new ArrayList<User>();
//		}
//	}
//	@Override
//	public List<User> getMostActiveRecommendedUsers(long userId, int limit){
//		List<User> users = new ArrayList<User>();
//		List<Long> ignoredUsers = new ArrayList<Long>();
//		ignoredUsers.add(userId);
//		List<User> followingUsers = followResourceManager.getFollowingUsers(userId);
//		
//		for (User fUser : followingUsers) {
//			if (!ignoredUsers.contains(fUser)) {
//				ignoredUsers.add(fUser.getId());
//			}
//		}
//		
//		List<Long> userGoalsIds = learningGoalManager.getUserGoalsIds(userId);
//		users = recommendedResourcesSearch.findMostActiveRecommendedUsers(userId, ignoredUsers, userGoalsIds, limit);
//		return users;
//	}
//
///*	@Override
//	public List<User> getMostActiveRecommendedUsers(User loggedUser, int limit){
//		List<User> users = new ArrayList<User>();
//		final long daysSinceEpoch = DateUtil.getDaysSinceEpoch();
//		
//		List<Long> ignoredUsers=new ArrayList<Long>();
//		ignoredUsers.add(loggedUser.getId());
//		List<User> followingUsers=followResourceManager.getFollowingUsers(loggedUser);
//		for(User fUser:followingUsers){
//			if(!ignoredUsers.contains(fUser)){
//				ignoredUsers.add(fUser.getId());
//			}
//		}
//		List<Long> userGoalsIds=learningGoalManager.getUserGoalsIds(loggedUser);
//		//if (limit > mostActiveRecommendedUsers.size()) {
//		//	limit = mostActiveRecommendedUsers.size();
//		//}
//		
//		if (!mostActiveRecommendedUsers.isEmpty()) {
//			for(User user:mostActiveRecommendedUsers){
//				if(!ignoredUsers.contains(user.getId())){
//					users.add(user);
//				}
//				if(users.size()==limit){
//					break;
//				}
//			}
//			 
//		}
//
//		if (daysSinceEpoch > this.dayActivityRecommendationGenerated) {		
//			if(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)>this.hourActivityRecommendationGenerated){
//			initializeMostActiveRecommendedUsers();
//			this.dayActivityRecommendationGenerated=daysSinceEpoch;
//			this.hourActivityRecommendationGenerated=Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
//			}
//		}
//		return users;
//	}*/
//	
//	/*@Override
//	public void initializeMostActiveRecommendedUsers(){
//		new Thread(new Runnable() {
//			@Override
//			public void run() {
//				Session session = (Session) userManager.getPersistence().openSession();
//				try {
//					generateMostActiveRecommendedUsers(DateUtil.getDaysSinceEpoch(), session);
//				} catch (LoggingException e) {
//					logger.error(e);
//				} finally {
//					HibernateUtil.close(session);
//				}
//			 }
//		}).start();
//	}
//	
//	private void generateMostActiveRecommendedUsers(long sincedate, Session session) throws LoggingException {
//		try {
//			long date = sincedate;
//			Map<Long, Long> activitySummary = new HashMap<Long, Long>();
//			
//			for (int i = 0; i < 7; i++) {
//				DBCursor dbCursor = loggingDBManager.getMostActiveUsersForDate(date, 20);
//				
//				while (dbCursor.hasNext()) {
//					DBObject userDateLog = dbCursor.next();
//					Long userid = Long.valueOf(userDateLog.get("userid").toString());
//					Long actNumber = Long.valueOf(userDateLog.get("counter").toString());
//					Long summary = actNumber;
//					
//					if (activitySummary.containsKey(userid)) {
//						summary = summary + activitySummary.get(userid);
//						activitySummary.remove(userid);
//					}
//					activitySummary.put(userid, summary);
//				}
//				date--;
//			}
//			
//			Map<Long, Long> sortedMap = sortByValues(activitySummary);
//			
//			logger.debug("GENERATE MOST ACTIVE USERS LIST" + sortedMap);
//			Set<Long> users = sortedMap.keySet();
//			this.mostActiveRecommendedUsers.clear();
//			int numbOfRecUsers = 0;
//			
//			for (Long userid : users) {
//				if (userid > 1) {
//					User user = null;
//					user = (User) session.get(User.class, userid);
//					this.mostActiveRecommendedUsers.add(user);
//					numbOfRecUsers++;
//				}
//				
//				if (numbOfRecUsers > 10)
//					break;
//				
//			}
//		} catch (MongoException me) {
//			throw new LoggingException("Mongo store is not available");
//		}
//	}
//	*/
//	public static <K extends Comparable, V extends Comparable> Map<K, V> sortByValues(Map<K, V> map) {
//		List<Map.Entry<K, V>> entries = new LinkedList<Map.Entry<K, V>>(map.entrySet());
//		
//		Collections.sort(entries, new Comparator<Map.Entry<K, V>>() {
//			@Override
//			public int compare(Entry<K, V> o1, Entry<K, V> o2) {
//				return o2.getValue().compareTo(o1.getValue());
//			}
//		});
//		
//		// LinkedHashMap will keep the keys in the order they are inserted
//		// which is currently sorted on natural ordering
//		Map<K, V> sortedMap = new LinkedHashMap<K, V>();
//		
//		for (Map.Entry<K, V> entry : entries) {
//			sortedMap.put(entry.getKey(), entry.getValue());
//		}
//		
//		return sortedMap;
//		
//	}
 }
