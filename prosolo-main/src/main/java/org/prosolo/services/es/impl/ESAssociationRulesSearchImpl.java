package org.prosolo.services.es.impl;

import org.prosolo.services.es.ESAssociationRulesSearch;
import org.springframework.stereotype.Service;

/**
 * @author Zoran Jeremic
 * @Deprecated since 0.7
 */
@Deprecated
@Service("org.prosolo.services.es.ESAssociationRulesSearch")
public class ESAssociationRulesSearchImpl implements ESAssociationRulesSearch {
	
//	private Logger logger = Logger.getLogger(ESAssociationRulesSearchImpl.class);
//	
//	@SuppressWarnings({ "rawtypes", "unchecked" })
//	@Override
//	public List<ActivityAccessCount> findMatchingActivitiesForCompetenceInAssociationRules(Collection<Long> alreadyAddedActivities, long competenceId, int limit)
//			throws IndexingServiceNotAvailable {
//		
//		List<ActivityAccessCount> recommendedActivities = new ArrayList<ActivityAccessCount>();
//		
//		try {
//			Client client = ElasticSearchFactory.getClient();
//		
//			BoolQueryBuilder bQueryBuilder = QueryBuilders.boolQuery();
//			bQueryBuilder.must(termQuery("id", competenceId));
//			
//			if (alreadyAddedActivities != null) {
//				for (Long activityId : alreadyAddedActivities) {
//					bQueryBuilder.should(termQuery("itemset1.id", activityId));
//				}
//			}
//			SearchRequestBuilder finalBuilder = client
//					.prepareSearch(ESIndexNames.INDEX_ASSOCRULES)
//					.setTypes(ESIndexTypes.COMPETENCE_ACTIVITIES)
//					.setQuery(bQueryBuilder)
//					.setFrom(0)
//					.setSize(limit);
//	
//			SearchResponse sResponse = finalBuilder.execute().actionGet();
//			
//			if (sResponse != null) {
//				for (SearchHit hit : sResponse.getHits()) {
//					// int id = (int) hit.getSource().get("id");
//					List activities = (ArrayList) hit.getSource().get("itemset2");
//					List itemset1 = (ArrayList) hit.getSource().get("itemset1");
//					
//					// Checking first if there is some activity in itemset1 that is
//					// not already passed
//					for (Object item1 : itemset1) {
//						Number activityIdNumber = ((Map<String, Integer>) item1).get("id");
//						Long activityId = activityIdNumber.longValue();
//						
//						if (!alreadyAddedActivities.contains(activityId)) {
//							ActivityAccessCount activityAccessCount = new ActivityAccessCount(activityId, competenceId, 0);
//							recommendedActivities.add(activityAccessCount);
//						}
//					}
//					
//					// Adding other activities from Itemset2
//					for (Object activity : activities) {
//						Number activityIdNumber = ((Map<String, Integer>) activity).get("id");
//						Long activityId = activityIdNumber.longValue();
//						
//						if (!alreadyAddedActivities.contains(activityId)) {
//							ActivityAccessCount activityAccessCount = new ActivityAccessCount(activityId, competenceId, 0);
//							recommendedActivities.add(activityAccessCount);
//						}
//						
//						if (!recommendedActivities.isEmpty()) {
//							return recommendedActivities;
//						}
//					}
//				}
//			}
//		} catch (NoNodeAvailableException e) {
//			logger.error(e);
//		}
//		return recommendedActivities;
//	}
//	
//	@SuppressWarnings({ "rawtypes", "unchecked" })
//	@Override
//	public List<ActivityAccessCount> findRelatedActivitiesForCompetenceAndActivityInAssociationRules(Collection<Long> alreadyAddedActivities, 
//			Long competenceId, Long activityId, int limit) throws IndexingServiceNotAvailable {
//		
//		List<ActivityAccessCount> relatedActivities = new ArrayList<ActivityAccessCount>();
//		
//		try {
//			Client client = ElasticSearchFactory.getClient();
//		
//			BoolQueryBuilder bQueryBuilder = QueryBuilders.boolQuery();
//			bQueryBuilder.must(termQuery("id", competenceId));
//			bQueryBuilder.must(termQuery("itemset1.id", activityId));
//			bQueryBuilder.must(termQuery("itemset1_size", 1));
//	
//			SearchRequestBuilder finalBuilder = client
//					.prepareSearch(ESIndexNames.INDEX_ASSOCRULES)
//					.setTypes(ESIndexTypes.COMPETENCE_ACTIVITIES)
//					.setQuery(bQueryBuilder)
//					.setFrom(0)
//					.setSize(limit);
//	
//			SearchResponse sResponse = finalBuilder.execute().actionGet();
//			
//			if (sResponse != null) {
//				for (SearchHit hit : sResponse.getHits()) {
//					// int id = (int) hit.getSource().get("id");
//					List activities = (ArrayList) hit.getSource().get("itemset2");
//					System.out.println("SOURCE" + hit.getSource() + " SCORE:" + hit.getScore() + " sc:" + hit.score());
//					// Adding other activities from Itemset2
//					
//					for (Object activity : activities) {
//						Number activityIdNumber = ((Map<String, Integer>) activity).get("id");
//						Long relActivityId = activityIdNumber.longValue();
//						System.out.println("FOUND RELATED ACTIVITY:" + relActivityId);
//						
//						if (!alreadyAddedActivities.contains(relActivityId)) {
//							ActivityAccessCount activityAccessCount = new ActivityAccessCount(relActivityId, competenceId, 0);
//							relatedActivities.add(activityAccessCount);
//							System.out.println("ADDED RELATED ACTIVITY:" + relActivityId);
//						}
//						
//						if (!relatedActivities.isEmpty()) {
//							return relatedActivities;
//						}
//					}
//				}
//			}
//		} catch (NoNodeAvailableException e) {
//			logger.error(e);
//		}
//		return relatedActivities;
//	}
//	
//	@SuppressWarnings({ "unchecked", "rawtypes" })
//	@Override
//	public List<ActivityAccessCount> findFrequentCompetenceActivities(long competenceId, int limit) throws IndexingServiceNotAvailable {
//		logger.debug("FIND FREQUENT ACTIVITIES FOR:" + competenceId);
//
//		List<ActivityAccessCount> frequentActivities = new ArrayList<ActivityAccessCount>();
//		
//		try {
//			Client client = ElasticSearchFactory.getClient();
//		
//			QueryBuilder qb = QueryBuilders.termQuery("id", competenceId);
//			String indexName = ESIndexNames.INDEX_RECOMMENDATION_DATA;
//			SearchRequestBuilder srBuilder = client
//					.prepareSearch(indexName)
//					.setTypes(ESIndexTypes.FREQ_COMPETENCE_ACTIVITIES)
//					.setQuery(qb)
//					.setFrom(0)
//					.setSize(10)
//					.setExplain(true);
//			
//			logger.debug("findFrequentCompetenceActivities QUERY:" + srBuilder.toString());
//			SearchResponse sr = srBuilder.execute().actionGet();
//			
//			if (sr != null) {
//				SearchHits searchHits = sr.getHits();
//				Iterator<SearchHit> hitsIter = searchHits.iterator();
//				
//				if (hitsIter.hasNext()) {
//					SearchHit searchHit = hitsIter.next();
//					List<Object> activities = (ArrayList) searchHit.getSource().get("activities");
//					
//					for (Object activityObj : activities) {
//						Number activityIdNumber = ((Map<String, Integer>) activityObj).get("id");
//						Long activityId = activityIdNumber.longValue();
//						logger.debug("found activity:" + activityId);
//						ActivityAccessCount activityAccess = new ActivityAccessCount(activityId, competenceId, 0);
//						frequentActivities.add(activityAccess);
//					}
//					
//				}
//			}
//		} catch (NoNodeAvailableException e) {
//			logger.error(e);
//		}
//		return frequentActivities;
//	}
}