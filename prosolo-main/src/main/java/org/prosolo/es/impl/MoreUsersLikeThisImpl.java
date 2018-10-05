package org.prosolo.es.impl;

import org.prosolo.es.MoreUsersLikeThis;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.springframework.stereotype.Service;

/**
 * @author Zoran Jeremic
 * @Deprecated since 0.7
 */
@Deprecated
@Service("org.prosolo.es.MoreUsersLikeThis")
//@Transactional
public class MoreUsersLikeThisImpl extends AbstractManagerImpl implements MoreUsersLikeThis{

	private static final long serialVersionUID = 8345103443587134183L;
 
//	@Autowired UserManager userManager;
//	String[] moreUsersLikeThisFields = new String[]{
//			"learninggoals.title",
//			"learninggoals.description"};
//	
//	@Override
//	public List<User> getRecommendedCollaboratorsForLearningGoal(
//			String likeText, Collection<Long> ignoredUsers, int limit) {
//		
//		List<User> foundUsers = new ArrayList<User>();
//		
//		try {
//			Client client = ElasticSearchFactory.getClient();
//			
//			
//			QueryBuilder qb = null;
//			
//	        qb = QueryBuilders.moreLikeThisQuery(moreUsersLikeThisFields)//moreLikeThisFieldQuery(moreNodesLikeThisFields)
//	                .likeText(likeText)
//	                .minTermFreq(1)
//	                .minDocFreq(1)
//	                .maxQueryTerms(1);
//	        
//			BoolQueryBuilder bQueryBuilder = QueryBuilders.boolQuery();
//			bQueryBuilder.should(qb);
//			
//			for (long ignUser : ignoredUsers) {
//				if (ignUser > 0) {
//					bQueryBuilder.mustNot(termQuery("id", ignUser));
//				}
//			}
//			SearchResponse sResponse = client.prepareSearch(ESIndexNames.INDEX_USERS)
//	    		  	.setTypes(ESIndexTypes.USER)
//		  			.setQuery(bQueryBuilder)
//		  			.setFrom(0)
//		  			.setSize(limit)
//			        .execute()
//			        .actionGet();
//			
//			if (sResponse != null) {
//				for (SearchHit hit : sResponse.getHits()) {
//					int id = (int) hit.getSource().get("id");
//					try {
//						User user = userManager.loadResource(User.class, id);
//						if (user != null) {
//							foundUsers.add(user);
//						}
//					} catch (ResourceCouldNotBeLoadedException e) {
//						logger.error("User was not found: " + id);
//					}
//				}
//			}
//		} catch (NoNodeAvailableException e) {
//			logger.error(e);
//		}
//		return foundUsers;
//	}
//	@Override
//	public List<User> getCollaboratorsBasedOnLocation(Collection<User> ignoredUsers, double lat, double lon, int limit){
//		List<User> foundUsers = new ArrayList<User>();
//		try {
//			Client client = ElasticSearchFactory.getClient();
//			QueryBuilder qb = new MatchAllQueryBuilder();
//			BoolQueryBuilder bQueryBuilder = QueryBuilders.boolQuery();
//			bQueryBuilder.should(qb);
//			
//			for (User ignUser : ignoredUsers) {
//				if (ignUser != null) {
//					bQueryBuilder.mustNot(termQuery("id", ignUser.getId()));
//				}
//			}
//			GeoDistanceSortBuilder sortBuilder = SortBuilders.geoDistanceSort("location")
//					.point(lat, lon)
//					.unit(DistanceUnit.KILOMETERS)
//					.order(SortOrder.ASC);
//			
//			SearchResponse sResponse = client
//					.prepareSearch(ESIndexNames.INDEX_USERS)
//					.setTypes(ESIndexTypes.USER)
//					.setQuery(bQueryBuilder)
//					.addSort(sortBuilder)
//					.setFrom(0)
//					.setSize(limit)
//					.execute()
//					.actionGet();
//			
//			foundUsers = this.convertSearchResponseToUsersList(sResponse);
//		} catch (NoNodeAvailableException e1) {
//			logger.error(e1);
//		}
//		return foundUsers;
//	}
//	@Override
//	public List<User> getRecommendedCollaboratorsBasedOnLocation(
//			String likeText, Collection<User> ignoredUsers, double lat, double lon, int limit) {
//	
//		List<User> foundUsers = new ArrayList<User>();
//		
//		try {
//			Client client = ElasticSearchFactory.getClient();
//			QueryBuilder qb = null;
//			
//	        qb = QueryBuilders.moreLikeThisQuery(moreUsersLikeThisFields)//moreLikeThisFieldQuery(moreNodesLikeThisFields)
//	                .likeText(likeText)
//	                .minTermFreq(1)
//	                .minDocFreq(1)
//	                .maxQueryTerms(1);
//	        BoolQueryBuilder bQueryBuilder = QueryBuilders.boolQuery();
//			bQueryBuilder.should(qb);
//			
//			for (User ignUser : ignoredUsers) {
//				if (ignUser != null) {
//					bQueryBuilder.mustNot(termQuery("id", ignUser.getId()));
//				}
//			}
//	    	QueryBuilder filter = QueryBuilders.geoDistanceQuery("location")
//					.point(lat, lon)
//					.distance(200, DistanceUnit.KILOMETERS)
//					.optimizeBbox("memory")
//					.geoDistance(GeoDistance.ARC);
//	    	
//	    	FilteredQueryBuilder filteredQueryBuilder = QueryBuilders
//					.filteredQuery(bQueryBuilder, filter);
//	System.out.println("INDEX USERS:"+ESIndexNames.INDEX_USERS);
//			SearchResponse sResponse = client.prepareSearch(ESIndexNames.INDEX_USERS)
//	    		  	.setTypes(ESIndexTypes.USER)
//		  			.setQuery(filteredQueryBuilder)
//		  			.setFrom(0)
//		  			.setSize(limit)
//			        .execute()
//			        .actionGet();
//			foundUsers = this.convertSearchResponseToUsersList(sResponse);
//		} catch (NoNodeAvailableException e1) {
//			logger.error(e1);
//		}
//		return foundUsers;
//	}
//	
//	private List<User> convertSearchResponseToUsersList(SearchResponse sResponse){
//		List<User> foundUsers = new ArrayList<User>();
//		if(sResponse!=null){
//			for (SearchHit hit : sResponse.getHits()) {
//				int id = (int) hit.getSource().get("id");
//				try {
//					User user = userManager.loadResource(User.class, id);
//					if (user != null) {
//						foundUsers.add(user);
//					}
//				} catch (ResourceCouldNotBeLoadedException e) {
//					logger.error("User was not found: " + id);
//				}
//			}
//			}
//		return foundUsers;
//	}
//	
//	@Override
//	public List<User> getRecommendedCollaboratorsBasedOnSimilarity(
//			String likeText, Collection<User> ignoredUsers, int limit) throws IndexingServiceNotAvailable {
//		
//		List<User> foundUsers = new ArrayList<User>();
//		
//		try {
//			Client client = ElasticSearchFactory.getClient();
//			
//			QueryBuilder qb = null;
//			
//	        qb = QueryBuilders.moreLikeThisQuery(moreUsersLikeThisFields)
//	                .likeText(likeText)
//	                .minTermFreq(1)
//	                .minDocFreq(1)
//	                .maxQueryTerms(1);
//	        
//	        BoolQueryBuilder bQueryBuilder = QueryBuilders.boolQuery();
//			bQueryBuilder.should(qb);
//			
//			for (User ignUser : ignoredUsers) {
//				if (ignUser != null) {
//					bQueryBuilder.mustNot(termQuery("id", ignUser.getId()));
//				}
//			}
//	        
//			SearchResponse sResponse = client.prepareSearch(ESIndexNames.INDEX_USERS)
//			  	.setTypes(ESIndexTypes.USER)
//	  			.setQuery(bQueryBuilder)
//	  			.setFrom(0)
//	  			.setSize(limit)
//		        .execute()
//		        .actionGet();
//			
//			this.convertSearchResponseToUsersList(sResponse);
//		} catch (NoNodeAvailableException e) {
//			logger.error(e);
//		}
//		return foundUsers;
//	}
}
