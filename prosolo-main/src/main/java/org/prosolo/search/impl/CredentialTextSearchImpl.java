package org.prosolo.search.impl;

import org.apache.log4j.Logger;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortOrder;
import org.prosolo.bigdata.common.enums.ESIndexTypes;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.ESIndexNames;
import org.prosolo.common.domainmodel.credential.CredentialType;
import org.prosolo.search.CredentialTextSearch;
import org.prosolo.search.util.credential.CredentialSearchConfig;
import org.prosolo.search.util.credential.CredentialSearchFilterManager;
import org.prosolo.search.util.credential.CredentialSearchFilterUser;
import org.prosolo.search.util.credential.LearningResourceSortOption;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.indexing.ESIndexer;
import org.prosolo.services.indexing.ElasticSearchFactory;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.CredentialData;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Date;

import static org.elasticsearch.index.query.QueryBuilders.termQuery;

//import org.elasticsearch.index.query.BoolQueryBuilder;
//import org.elasticsearch.index.query.NestedFilterBuilder;


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
	public TextSearchResponse1<CredentialData> searchCredentialsForUser(
			String searchTerm, int page, int limit, long userId, 
			CredentialSearchFilterUser filter, LearningResourceSortOption sortOption) {
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
					bQueryBuilder.filter(termQuery("bookmarkedBy.id", userId));
					break;
				case ENROLLED:
					bQueryBuilder.filter(termQuery("students.id", userId));
					break;
				default:
					break;
			}
			
			bQueryBuilder.filter(configureAndGetSearchFilter(
					CredentialSearchConfig.forDelivery(true, true, false, false), userId));
			
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
			
			if (sResponse != null) {
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
							CredentialData cd = credentialManager
									.getCredentialDataWithProgressIfExists(id, userId);
							
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
	
	@Override
	public TextSearchResponse1<CredentialData> searchCredentialsForManager(
			String searchTerm, int page, int limit, long userId, 
			CredentialSearchFilterManager filter, LearningResourceSortOption sortOption) {
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
				
				bQueryBuilder.filter(qb);
			}
			
			switch(filter) {
				case ACTIVE:
					bQueryBuilder.filter(termQuery("archived", false));
					break;
				case ARCHIVED:
					bQueryBuilder.filter(termQuery("archived", true));
					break;
				default:
					break;
			}
			
			
			bQueryBuilder.filter(configureAndGetSearchFilter(
					CredentialSearchConfig.forOriginal(true), userId));
			
			String[] includes = {"id", "title", "archived"};
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
						String title = hit.getSource().get("title").toString();
						boolean archived = Boolean.parseBoolean(hit.getSource().get("archived").toString());
						CredentialData cd = new CredentialData(false);
						cd.setId(id);
						cd.setTitle(title);
						cd.setArchived(archived);
						cd.setDeliveries(credentialManager.getActiveDeliveries(id));
						response.addFoundNode(cd);
					}
				}
			}
		} catch (Exception e1) {
			e1.printStackTrace();
			logger.error(e1);
		}
		return response;
	}
	
	private QueryBuilder configureAndGetSearchFilter(CredentialSearchConfig config, long userId) {
		BoolQueryBuilder bf = QueryBuilders.boolQuery();
		bf.filter(QueryBuilders.termQuery("type", config.getType().toString().toLowerCase()));
		BoolQueryBuilder boolFilter = QueryBuilders.boolQuery();
		if(config.getType() == CredentialType.Delivery) {
			if(config.shouldIncludeResourcesWithViewPrivilege()) {
				/*
				 * users with learn privilege (or when credential is visible to all) can see credential delivery
				 * if delivery is scheduled (delivery start is set) and not ended.
				 */
				Date now = new Date();
				BoolQueryBuilder publishedAndVisibleFilter = QueryBuilders.boolQuery();
				publishedAndVisibleFilter.filter(QueryBuilders.existsQuery("deliveryStart"));
//				publishedAndVisibleFilter.filter(QueryBuilders.rangeQuery("deliveryStart")
//						.lte(org.prosolo.services.indexing.utils.ElasticsearchUtil.getDateStringRepresentation(now)));
				BoolQueryBuilder endDateFilter = QueryBuilders.boolQuery();
				endDateFilter.should(QueryBuilders.boolQuery().mustNot(QueryBuilders.existsQuery("deliveryEnd")));
				endDateFilter.should(QueryBuilders.rangeQuery("deliveryEnd")
						.gt(org.prosolo.services.indexing.utils.ElasticsearchUtil.getDateStringRepresentation(now)));
				publishedAndVisibleFilter.filter(endDateFilter);
				BoolQueryBuilder visibleFilter = QueryBuilders.boolQuery();
				visibleFilter.should(QueryBuilders.termQuery("visibleToAll", true));
				visibleFilter.should(QueryBuilders.termQuery("usersWithViewPrivilege.id", userId));
				publishedAndVisibleFilter.filter(visibleFilter);
				
				boolFilter.should(publishedAndVisibleFilter);
			}
			
			if(config.shouldIncludeEnrolledResources()) {
				//user is enrolled in a credential (currently learning or completed competence)
				boolFilter.should(QueryBuilders.termQuery("students.id", userId));
			}
			
			if(config.shouldIncludeResourcesWithInstructPrivilege()) {
				/*
				 * we don't need to store users with instruct privilege separately because we already store 
				 * collection of instructors for credential
				 */
				boolFilter.should(QueryBuilders.termQuery("instructors.id", userId));
			}
		}
		
		if(config.shouldIncludeResourcesWithEditPrivilege()) {
			BoolQueryBuilder editorFilter = QueryBuilders.boolQuery();
			//user is owner of a credential
			editorFilter.should(QueryBuilders.termQuery("creatorId", userId));
			
			//user has Edit privilege for credential
			editorFilter.should(QueryBuilders.termQuery("usersWithEditPrivilege.id", userId));
			
			BoolQueryBuilder editorAndTypeFilter = QueryBuilders.boolQuery();
			editorAndTypeFilter.filter(editorFilter);
			
			boolFilter.should(editorAndTypeFilter);
		}
		
		bf.filter(boolFilter);
		return bf;
	}
	
}
