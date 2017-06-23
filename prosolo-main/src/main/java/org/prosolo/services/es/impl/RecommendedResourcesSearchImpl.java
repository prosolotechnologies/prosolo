package org.prosolo.services.es.impl;

import org.prosolo.services.es.RecommendedResourcesSearch;
import org.springframework.stereotype.Service;

/**
 * @author Zoran Jeremic
 * @Deprecated since 0.7
 */
@Deprecated
@Service("org.prosolo.services.es.RecommendedResourcesSearch")
public class RecommendedResourcesSearchImpl implements RecommendedResourcesSearch {

//	private Logger logger = Logger.getLogger(RecommendedResourcesSearchImpl.class);
//
//	@Autowired
//	private UserManager userManager;
//
//	@Override
//	public List<User> findMostActiveRecommendedUsers(Long userId, List<Long> ignoredUsers, List<Long> userGoalsIds, int limit) {
//		List<User> recommendedUsersList = new ArrayList<User>();
//
//		try {
//			Client client = ElasticSearchFactory.getClient();
//
//			QueryBuilder qb = QueryBuilders.termsQuery("learninggoalid", userGoalsIds);
//			String indexName = ESIndexNames.INDEX_RECOMMENDATION_DATA;
//			SearchResponse sr = client.prepareSearch(indexName).setQuery(qb).setFrom(0).setSize(10).setExplain(true).execute().actionGet();
//			Map<Long, Double> usersScores = new HashMap<Long, Double>();
//
//			if (sr != null) {
//				SearchHits searchHits = sr.getHits();
//				Iterator<SearchHit> hitsIter = searchHits.iterator();
//
//				while (hitsIter.hasNext()) {
//					SearchHit searchHit = hitsIter.next();
//					@SuppressWarnings({"rawtypes", "unchecked"})
//					List<Object> mostactiveusersObjects = (ArrayList) searchHit.getSource().get("mostactiveusers");
//					Gson gson = new Gson();
//					Type listType = new TypeToken<List<Score>>() {
//					}.getType();
//					List<Score> recommendedUsers = gson.fromJson(mostactiveusersObjects.toString(), listType);
//
//					for (Score userScore : recommendedUsers) {
//						if (!ignoredUsers.contains(userScore.getId())) {
//							Double score = 0.0;
//							if (usersScores.containsKey(userScore.getId())) {
//								score = usersScores.get(userScore.getId() + userScore.getScore());
//							} else {
//								score = userScore.getScore();
//							}
//							if (score == null) {
//								score = 0.0;
//							}
//							usersScores.put(userScore.getId(), score);
//						}
//					}
//				}
//
//				List<Map.Entry<Long, Double>> topTenUsers = new ArrayList<Map.Entry<Long, Double>>(usersScores.entrySet());
//
//				if (topTenUsers != null) {
//					Collections.sort(topTenUsers, new Comparator<Map.Entry<Long, Double>>() {
//						public int compare(Map.Entry<Long, Double> entry1, Map.Entry<Long, Double> entry2) {
//							System.out.println("ENTRY 1:" + entry1.getKey() + " entry1:" + entry1.getValue());
//							System.out.println("ENTRY 2:" + entry1.getKey() + " entry2:" + entry1.getValue());
//							return entry2.getValue().compareTo(entry1.getValue());
//						}
//					});
//				}
//
//				if (topTenUsers.size() < limit) {
//					limit = topTenUsers.size();
//				}
//
//				topTenUsers = topTenUsers.subList(0, limit);
//
//				for (Entry<Long, Double> topUser : topTenUsers) {
//					topUser.getKey();
//
//					try {
//						User recommendedUser = userManager.loadResource(User.class, topUser.getKey());
//						recommendedUsersList.add(recommendedUser);
//					} catch (ResourceCouldNotBeLoadedException e) {
//						logger.error(e);
//					}
//				}
//			}
//		} catch (NoNodeAvailableException e) {
//			logger.error(e);
//		}
//		return recommendedUsersList;
//	}
//
//	@Override
//	public List<User> findSimilarUsers(Long userId, List<Long> ignoredUsers, int from, int limit) {
//		List<User> recommendedUsersList = new ArrayList<User>();
//
//		try {
//			Client client = ElasticSearchFactory.getClient();
//			BoolQueryBuilder bQueryBuilder = QueryBuilders.boolQuery();
//			bQueryBuilder.must(termQuery("id", userId));
//			//QueryBuilder qb = QueryBuilders.termsQuery("learninggoalid", userGoalsIds);
//			String indexName = ESIndexNames.INDEX_RECOMMENDATION_DATA;
//			SearchResponse sr = client
//					.prepareSearch(ESIndexNames.INDEX_RECOMMENDATION_DATA)
//					.setTypes(ESIndexTypes.SIMILAR_USERS)
//					.setQuery(bQueryBuilder)
//					.setFrom(from)
//					.setSize(limit)
//					.setExplain(true).execute().actionGet();
//
//			Map<Long, Double> usersScores = new HashMap<Long, Double>();
//
//			if (sr != null) {
//				SearchHits searchHits = sr.getHits();
//				Iterator<SearchHit> hitsIter = searchHits.iterator();
//
//				while (hitsIter.hasNext()) {
//					SearchHit searchHit = hitsIter.next();
//					@SuppressWarnings({"rawtypes", "unchecked"})
//					List<Object> similarUsersObjects = (ArrayList) searchHit.getSource().get("recommendedUsers");
//					Gson gson = new Gson();
//					Type listType = new TypeToken<List<Score>>() {
//					}.getType();
//					List<Score> recommendedUsers = gson.fromJson(similarUsersObjects.toString(), listType);
//					System.out.println("SOURCE" + searchHit.getSource() + " SCORE:" + searchHit.getScore() + " sc:" + searchHit.score());
//					for (Score userScore : recommendedUsers) {
//						System.out.println("USER SCORE:"+gson.toJson(userScore));
//						if (!ignoredUsers.contains(userScore.getId())) {
//							Double score = 0.0;
//							if (usersScores.containsKey(userScore.getId())) {
//								score = usersScores.get(userScore.getId() + userScore.getScore());
//							} else {
//								score = userScore.getScore();
//							}
//							if (score == null) {
//								score = 0.0;
//							}
//							usersScores.put(userScore.getId(), score);
//						}
//					}
//				}
//
//				List<Map.Entry<Long, Double>> topTenUsers = new ArrayList<Map.Entry<Long, Double>>(usersScores.entrySet());
//
//				if (topTenUsers != null) {
//					Collections.sort(topTenUsers, new Comparator<Map.Entry<Long, Double>>() {
//						public int compare(Map.Entry<Long, Double> entry1, Map.Entry<Long, Double> entry2) {
//							//System.out.println("ENTRY 1:" + entry1.getKey() + " entry1:" + entry1.getValue());
//							//System.out.println("ENTRY 2:" + entry1.getKey() + " entry2:" + entry1.getValue());
//							return entry2.getValue().compareTo(entry1.getValue());
//						}
//					});
//				}
//
//				if (topTenUsers.size() < limit) {
//					limit = topTenUsers.size();
//				}
//
//				topTenUsers = topTenUsers.subList(0, limit);
//
//				for (Entry<Long, Double> topUser : topTenUsers) {
//					topUser.getKey();
//
//					try {
//						User recommendedUser = userManager.loadResource(User.class, topUser.getKey());
//						recommendedUsersList.add(recommendedUser);
//					} catch (ResourceCouldNotBeLoadedException e) {
//						logger.error(e);
//					}
//				}
//			}
//		} catch (NoNodeAvailableException e) {
//			logger.error(e);
//		}
//		return recommendedUsersList;
//
//	}
}
