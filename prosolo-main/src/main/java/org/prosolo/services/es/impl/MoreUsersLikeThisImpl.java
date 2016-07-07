package org.prosolo.services.es.impl;

import static org.elasticsearch.index.query.QueryBuilders.termQuery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.common.geo.GeoDistance;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.BoolQueryBuilder;
//import org.elasticsearch.index.query.FilterBuilder;
//import org.elasticsearch.index.query.FilterBuilders;

import org.elasticsearch.index.query.FilteredQueryBuilder;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.GeoDistanceSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.prosolo.bigdata.common.enums.ESIndexTypes;
import org.prosolo.bigdata.common.exceptions.IndexingServiceNotAvailable;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.es.MoreUsersLikeThis;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.indexing.ESIndexNames;
import org.prosolo.services.indexing.ElasticSearchFactory;
import org.prosolo.services.nodes.UserManager;
//import org.prosolo.bigdata.common.exceptions.IndexingServiceNotAvailable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Zoran Jeremic, Aug 16, 2014
 *
 */
@Service("org.prosolo.services.es.MoreUsersLikeThis")
@Transactional
public class MoreUsersLikeThisImpl extends AbstractManagerImpl implements MoreUsersLikeThis{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8345103443587134183L;
 
	@Autowired UserManager userManager;
	String[] moreUsersLikeThisFields = new String[]{
			"learninggoals.title",
			"learninggoals.description"};
	
	@Override
	public List<User> getRecommendedCollaboratorsForLearningGoal(
			String likeText, Collection<User> ignoredUsers, int limit) {
		
		List<User> foundUsers = new ArrayList<User>();
		
		try {
			Client client = ElasticSearchFactory.getClient();
			
			
			QueryBuilder qb = null;
			
	        qb = QueryBuilders.moreLikeThisQuery(moreUsersLikeThisFields)//moreLikeThisFieldQuery(moreNodesLikeThisFields)
	                .likeText(likeText)
	                .minTermFreq(1)
	                .minDocFreq(1)
	                .maxQueryTerms(1);
	        
			BoolQueryBuilder bQueryBuilder = QueryBuilders.boolQuery();
			bQueryBuilder.should(qb);
			
			for (User ignUser : ignoredUsers) {
				if (ignUser != null) {
					bQueryBuilder.mustNot(termQuery("id", ignUser.getId()));
				}
			}
			SearchResponse sResponse = client.prepareSearch(ESIndexNames.INDEX_USERS)
	    		  	.setTypes(ESIndexTypes.USER)
		  			.setQuery(bQueryBuilder)
		  			.setFrom(0)
		  			.setSize(limit)
			        .execute()
			        .actionGet();
			
			if (sResponse != null) {
				for (SearchHit hit : sResponse.getHits()) {
					// String url = (String) hit.getSource().get("url");
					int id = (int) hit.getSource().get("id");
					try {
						User user = userManager.loadResource(User.class, id);
						if (user != null) {
							foundUsers.add(user);
						}
					} catch (ResourceCouldNotBeLoadedException e) {
						logger.error("User was not found: " + id);
					}
				}
			}
		} catch (NoNodeAvailableException e) {
			logger.error(e);
		}
		return foundUsers;
	}
	@Override
	public List<User> getCollaboratorsBasedOnLocation(Collection<User> ignoredUsers, double lat, double lon, int limit){
		List<User> foundUsers = new ArrayList<User>();
		try {
			Client client = ElasticSearchFactory.getClient();
			QueryBuilder qb = new MatchAllQueryBuilder();
			BoolQueryBuilder bQueryBuilder = QueryBuilders.boolQuery();
			bQueryBuilder.should(qb);
			
			for (User ignUser : ignoredUsers) {
				if (ignUser != null) {
					bQueryBuilder.mustNot(termQuery("id", ignUser.getId()));
				}
			}
			GeoDistanceSortBuilder sortBuilder = SortBuilders.geoDistanceSort("user.location")
					.point(lat, lon)
					.unit(DistanceUnit.KILOMETERS)
					.order(SortOrder.ASC);
			
			SearchResponse sResponse = client
					.prepareSearch(ESIndexNames.INDEX_USERS)
					.setTypes(ESIndexTypes.USER)
					.setQuery(bQueryBuilder)
					.addSort(sortBuilder)
					.setFrom(0)
					.setSize(limit)
					.execute()
					.actionGet();
			
			foundUsers = this.convertSearchResponseToUsersList(sResponse);
		} catch (NoNodeAvailableException e1) {
			logger.error(e1);
		}
		return foundUsers;
	}
	@Override
	public List<User> getRecommendedCollaboratorsBasedOnLocation(
			String likeText, Collection<User> ignoredUsers, double lat, double lon, int limit) {
	
		List<User> foundUsers = new ArrayList<User>();
		
		try {
			Client client = ElasticSearchFactory.getClient();
			QueryBuilder qb = null;
			
	        qb = QueryBuilders.moreLikeThisQuery(moreUsersLikeThisFields)//moreLikeThisFieldQuery(moreNodesLikeThisFields)
	                .likeText(likeText)
	                .minTermFreq(1)
	                .minDocFreq(1)
	                .maxQueryTerms(1);
	        BoolQueryBuilder bQueryBuilder = QueryBuilders.boolQuery();
			bQueryBuilder.should(qb);
			
			for (User ignUser : ignoredUsers) {
				if (ignUser != null) {
					bQueryBuilder.mustNot(termQuery("id", ignUser.getId()));
				}
			}
	    	QueryBuilder filter = QueryBuilders.geoDistanceQuery("user.location")
					.point(lat, lon)
					.distance(200, DistanceUnit.KILOMETERS)
					.optimizeBbox("memory")
					.geoDistance(GeoDistance.ARC);
	    	
	    	FilteredQueryBuilder filteredQueryBuilder = QueryBuilders
					.filteredQuery(bQueryBuilder, filter);
	System.out.println("INDEX USERS:"+ESIndexNames.INDEX_USERS);
			SearchResponse sResponse = client.prepareSearch(ESIndexNames.INDEX_USERS)
	    		  	.setTypes(ESIndexTypes.USER)
		  			.setQuery(filteredQueryBuilder)
		  			.setFrom(0)
		  			.setSize(limit)
			        .execute()
			        .actionGet();
			foundUsers = this.convertSearchResponseToUsersList(sResponse);
		} catch (NoNodeAvailableException e1) {
			logger.error(e1);
		}
		return foundUsers;
	}
	
	private List<User> convertSearchResponseToUsersList(SearchResponse sResponse){
		List<User> foundUsers = new ArrayList<User>();
		if(sResponse!=null){
			for (SearchHit hit : sResponse.getHits()) {
				int id = (int) hit.getSource().get("id");
				try {
					User user = userManager.loadResource(User.class, id);
					if (user != null) {
						foundUsers.add(user);
					}
				} catch (ResourceCouldNotBeLoadedException e) {
					logger.error("User was not found: " + id);
				}
			}
			}
		return foundUsers;
	}
	
	@Override
	public List<User> getRecommendedCollaboratorsBasedOnSimilarity(
			String likeText, Collection<User> ignoredUsers, int limit) throws IndexingServiceNotAvailable {
		
		List<User> foundUsers = new ArrayList<User>();
		
		try {
			Client client = ElasticSearchFactory.getClient();
			
			QueryBuilder qb = null;
			
	        qb = QueryBuilders.moreLikeThisQuery(moreUsersLikeThisFields)//moreLikeThisFieldQuery(moreNodesLikeThisFields)
	                .likeText(likeText)
	                .minTermFreq(1)
	                .minDocFreq(1)
	                .maxQueryTerms(1);
	        
	        BoolQueryBuilder bQueryBuilder = QueryBuilders.boolQuery();
			bQueryBuilder.should(qb);
			
			for (User ignUser : ignoredUsers) {
				if (ignUser != null) {
					bQueryBuilder.mustNot(termQuery("id", ignUser.getId()));
				}
			}
	        
			SearchResponse sResponse = client.prepareSearch(ESIndexNames.INDEX_USERS)
			  	.setTypes(ESIndexTypes.USER)
	  			.setQuery(bQueryBuilder)
	  			.setFrom(0)
	  			.setSize(limit)
		        .execute()
		        .actionGet();
			
			this.convertSearchResponseToUsersList(sResponse);
		} catch (NoNodeAvailableException e) {
			logger.error(e);
		}
		return foundUsers;
	}
}
