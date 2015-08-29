package org.prosolo.services.es.impl;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.prosolo.bigdata.common.dal.pojo.Score;
import org.prosolo.bigdata.common.exceptions.IndexingServiceNotAvailable;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.es.RecommendedResourcesSearch;
import org.prosolo.services.indexing.ESIndexNames;
import org.prosolo.services.indexing.ElasticSearchFactory;
import org.prosolo.services.nodes.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
@author Zoran Jeremic Jun 6, 2015
 *
 */
@Service("org.prosolo.services.es.RecommendedResourcesSearch")
public class RecommendedResourcesSearchImpl implements RecommendedResourcesSearch {
	@Autowired UserManager userManager;
	@Override
	public List<User> findMostActiveRecommendedUsers(Long userId, List<Long> ignoredUsers, List<Long> userGoalsIds, int limit){
		Client client=null;
		try {
			client = ElasticSearchFactory.getClient();
		} catch (IndexingServiceNotAvailable e) {
			e.printStackTrace();
		}
		QueryBuilder qb =QueryBuilders.termsQuery("learninggoalid", userGoalsIds);
		//TermsFilterBuilder learningGoalsTermsFilter = FilterBuilders.termsFilter("learninggoalid", learninggoalsids);
		String indexName = ESIndexNames.INDEX_RECOMMENDATION_DATA;
		SearchResponse sr = client.prepareSearch(indexName).setQuery(qb).setFrom(0)
				.setSize(10).setExplain(true).execute().actionGet();
		Map<Long, Double> usersScores=new HashMap<Long,Double>();
		List<User> recommendedUsersList=new ArrayList<User>();
		if (sr != null) {
			SearchHits searchHits = sr.getHits();
			Iterator<SearchHit> hitsIter = searchHits.iterator();

			while (hitsIter.hasNext()) {
				SearchHit searchHit = hitsIter.next();
			//	Map<String, SearchHitField> hitSource = searchHit.getFields();
				//System.out.println("hits:"+hitSource.toString()+" fields.:"+hitSource.size());
				List<Object> mostactiveusersObjects=(ArrayList) searchHit.getSource().get("mostactiveusers");
					Gson gson = new Gson();
					Type listType = new TypeToken<List<Score>>() {}.getType();
					List<Score> recommendedUsers = gson.fromJson(mostactiveusersObjects.toString(), listType);
				for(Score userScore:recommendedUsers){
					 if(!ignoredUsers.contains(userScore.getId())){
						 Double score=0.0;
						 if(usersScores.containsKey(userScore.getId())){
							 score=usersScores.get(userScore.getId()+userScore.getScore());
						 }else{
							 score=userScore.getScore();
						 }
						 if(score==null){
							 score=0.0;
						 }
						 usersScores.put(userScore.getId(), score);
					 }
				}
			}
				List<Map.Entry<Long,Double>> topTenUsers = new ArrayList<Map.Entry<Long,Double>>(usersScores.entrySet());
				if(topTenUsers!=null){
				Collections.sort(topTenUsers, new Comparator<Map.Entry<Long,Double>>() {
				  public int compare(
				      Map.Entry<Long,Double> entry1, Map.Entry<Long,Double> entry2) {
					  System.out.println("ENTRY 1:"+entry1.getKey()+" entry1:"+entry1.getValue());
					  System.out.println("ENTRY 2:"+entry1.getKey()+" entry2:"+entry1.getValue());
				    return entry2.getValue().compareTo(entry1.getValue());
				  }
				});
				}
				//int limitTo=limit;
				if(topTenUsers.size()<limit){
					limit=topTenUsers.size();
				}
				topTenUsers=topTenUsers.subList(0, limit);
				for(Entry<Long, Double> topUser:topTenUsers){
					topUser.getKey();
					 
					try {
						User recommendedUser=userManager.loadResource(User.class, topUser.getKey());
						recommendedUsersList.add(recommendedUser);
					} catch (ResourceCouldNotBeLoadedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				//RecommendedDocument recDoc = new RecommendedDocument(searchHit);
				//foundDocs.add(recDoc);
		
			}
		return recommendedUsersList;
		
	}

}

