package org.prosolo.search.impl;

import static org.elasticsearch.index.query.QueryBuilders.termQuery;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
//import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
//import org.elasticsearch.index.query.NestedFilterBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortOrder;
import org.prosolo.bigdata.common.enums.ESIndexTypes;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.ESIndexNames;
import org.prosolo.common.domainmodel.credential.LearningResourceType;
import org.prosolo.search.CredentialTextSearch;
import org.prosolo.search.util.credential.CredentialSearchFilter;
import org.prosolo.search.util.credential.CredentialSortOption;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.indexing.ESIndexer;
import org.prosolo.services.indexing.ElasticSearchFactory;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.CredentialData;
import org.springframework.stereotype.Service;


/**
 * 
 * @author stefanvuckovic
 *
 */
@Service("org.prosolo.search.CredentialTextSearch")
public class CredentialTextSearchImpl extends AbstractManagerImpl implements CredentialTextSearch {

	private static final long serialVersionUID = -3839868422620795857L;

	private static Logger logger = Logger.getLogger(CredentialTextSearchImpl.class);
	
	@Inject private ESIndexer esIndexer;
	@Inject private CredentialManager credentialManager;
	
	
	private int setStart(int page, int limit){
		int start = 0;
		if (page >= 0 && limit > 0) {
			start = page * limit;
		}
		return start;
	}
	
	@Override
	public TextSearchResponse1<CredentialData> searchCredentials(
			String searchTerm, int page, int limit, long userId, 
			CredentialSearchFilter filter, CredentialSortOption sortOption, 
			boolean includeEnrolledCredentials, boolean includeCredentialsWithViewPrivilege) {
		TextSearchResponse1<CredentialData> response = new TextSearchResponse1<>();
		try {
			int start = 0;
			start = setStart(page, limit);

			Client client = ElasticSearchFactory.getClient();
			esIndexer.addMapping(client, ESIndexNames.INDEX_NODES, ESIndexTypes.CREDENTIAL);
			
			BoolQueryBuilder bQueryBuilder = QueryBuilders.boolQuery();
			
			if(searchTerm != null && !searchTerm.isEmpty()) {
				QueryBuilder qb = QueryBuilders
						.queryStringQuery(searchTerm.toLowerCase() + "*").useDisMax(true)
						.defaultOperator(QueryStringQueryBuilder.Operator.AND)
						.field("title").field("description");
						//.field("tags.title").field("hashtags.title");
				
				bQueryBuilder.filter(qb);
			}
			
			//bQueryBuilder.minimumNumberShouldMatch(1);
			
			switch(filter) {
				case ALL:
					break;
				case BOOKMARKS:
					QueryBuilder qb = QueryBuilders.nestedQuery(
					        "bookmarkedBy",               
					        QueryBuilders.boolQuery()          
					             .filter(termQuery("bookmarkedBy.id", userId))); 
					bQueryBuilder.filter(qb);
					break;
				case FROM_CREATOR:
					bQueryBuilder.filter(termQuery("creatorId", userId));
					break;
				case BY_OTHER_STUDENTS:
					bQueryBuilder.mustNot(termQuery("creatorId", userId));
					/*
					 * Because lowercased strings are always stored in index. Alternative
					 * is to use match query that would analyze term passed.
					 */
					bQueryBuilder.filter(termQuery("type", 
							LearningResourceType.USER_CREATED.toString().toLowerCase()));
					break;
				case UNIVERSITY:
					bQueryBuilder.filter(termQuery("type", 
							LearningResourceType.UNIVERSITY_CREATED.toString().toLowerCase()));
					break;
				case BY_STUDENTS:
					//bQueryBuilder.mustNot(termQuery("creatorId", userId));
					/*
					 * Because lowercased strings are always stored in index. Alternative
					 * is to use match query that would analyze term passed.
					 */
					bQueryBuilder.filter(termQuery("type", 
							LearningResourceType.USER_CREATED.toString().toLowerCase()));
					break;
				case YOUR_CREDENTIALS:
					bQueryBuilder.filter(termQuery("instructors.id", userId));
					break;
				case ENROLLED:
					bQueryBuilder.filter(termQuery("students.id", userId));
					break;
				default:
					break;
			}
			
			BoolQueryBuilder boolFilter = QueryBuilders.boolQuery();
			
			if(includeCredentialsWithViewPrivilege) {
				//credential is published and: visible to all users or user has View privilege
				BoolQueryBuilder publishedAndVisibleFilter = QueryBuilders.boolQuery();
				publishedAndVisibleFilter.filter(QueryBuilders.termQuery("published", true));
				BoolQueryBuilder visibleFilter = QueryBuilders.boolQuery();
				visibleFilter.should(QueryBuilders.termQuery("visibleToAll", true));
				visibleFilter.should(QueryBuilders.termQuery("usersWithViewPrivilege.id", userId));
				publishedAndVisibleFilter.filter(visibleFilter);
				
				boolFilter.should(publishedAndVisibleFilter);
			}
			
			if(includeEnrolledCredentials) {
				//user is enrolled in a credential (currently learning or completed credential)
				boolFilter.should(QueryBuilders.termQuery("students.id", userId));
			}
			
			//user is owner of a credential
			boolFilter.should(QueryBuilders.termQuery("creatorId", userId));
			
			//user has Edit privilege for credential
			boolFilter.should(QueryBuilders.termQuery("usersWithEditPrivilege.id", userId));
			
			bQueryBuilder.filter(boolFilter);
//			FilteredQueryBuilder filteredQueryBuilder = QueryBuilders.filteredQuery(bQueryBuilder, 
//					boolFilter);
			
			//System.out.println("QUERY: " + filteredQueryBuilder.toString());
			
			String[] includes = {"id"};
			SearchRequestBuilder searchRequestBuilder = client.prepareSearch(ESIndexNames.INDEX_NODES)
					.setTypes(ESIndexTypes.CREDENTIAL)
					.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
					.setQuery(bQueryBuilder)
					.setFetchSource(includes, null);
			
			
			searchRequestBuilder.setFrom(start).setSize(limit);
			
			//add sorting
			SortOrder order = sortOption.getSortOrder() == 
					org.prosolo.services.util.SortingOption.ASC ? SortOrder.ASC 
					: SortOrder.DESC;
			searchRequestBuilder.addSort(sortOption.getSortField(), order);
			//System.out.println(searchRequestBuilder.toString());
			SearchResponse sResponse = searchRequestBuilder.execute().actionGet();
			
			if(sResponse != null) {
				SearchHits searchHits = sResponse.getHits();
				response.setHitsNumber(sResponse.getHits().getTotalHits());
				if(searchHits != null) {
					for (SearchHit hit : sResponse.getHits()) {
						/*
						 * long field is parsed this way because ES is returning integer although field type
						 * is specified as long in mapping file
						 */
						Long id = Long.parseLong(hit.getSource().get("id").toString());
						try {
							CredentialData cd = null;
							/*
							 * we should include user progress in this credential only
							 * if includeEnrolledCredentials is true
							 */
							if(includeEnrolledCredentials) {
								cd = credentialManager
										.getCredentialDataWithProgressIfExists(id, userId);
							} else {
								cd = credentialManager
										.getBasicCredentialData(id, userId);
							}
							
							if(cd != null) {
								response.addFoundNode(cd);
							}
						} catch (DbConnectionException e) {
							logger.error(e);
						}
					}
				}
			}
		} catch (Exception e1) {
			e1.printStackTrace();
			logger.error(e1);
		}
		return response;
	}
	
}
