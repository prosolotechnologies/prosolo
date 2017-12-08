package org.prosolo.search.impl;

import org.apache.log4j.Logger;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.prosolo.bigdata.common.enums.ESIndexTypes;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.ESIndexNames;
import org.prosolo.common.domainmodel.credential.CredentialType;
import org.prosolo.common.elasticsearch.ElasticSearchConnector;
import org.prosolo.common.util.ElasticsearchUtil;
import org.prosolo.search.CredentialTextSearch;
import org.prosolo.search.util.credential.CredentialSearchConfig;
import org.prosolo.search.util.credential.CredentialSearchFilterManager;
import org.prosolo.search.util.credential.CredentialSearchFilterUser;
import org.prosolo.search.util.credential.LearningResourceSortOption;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.indexing.ESIndexer;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.CredentialData;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;

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
	public PaginatedResult<CredentialData> searchCredentialsForUser(
			long organizationId, String searchTerm, int page, int limit, long userId,
			List<Long> unitIds, CredentialSearchFilterUser filter, LearningResourceSortOption sortOption) {
		PaginatedResult<CredentialData> response = new PaginatedResult<>();
		try {
			int start = 0;
			start = setStart(page, limit);
			
			BoolQueryBuilder bQueryBuilder = QueryBuilders.boolQuery();
			
			if (searchTerm != null && !searchTerm.isEmpty()) {
				QueryBuilder qb = QueryBuilders
						.queryStringQuery(ElasticsearchUtil.escapeSpecialChars(searchTerm.toLowerCase()) + "*").useDisMax(true)
						.defaultOperator(Operator.AND)
						.field("title").field("description");
						//.field("tags.title").field("hashtags.title");
				
				bQueryBuilder.filter(qb);
			}
			
			//bQueryBuilder.minimumNumberShouldMatch(1);
			
			switch (filter) {
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
					CredentialSearchConfig.forDelivery(true, true, false, false), userId, unitIds));
			
			String[] includes = {"id"};
			SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
			searchSourceBuilder
					.query(bQueryBuilder)
					.from(start)
					.size(limit)
					.fetchSource(includes, null);

			//add sorting
			SortOrder order = sortOption.getSortOrder() ==
					org.prosolo.services.util.SortingOption.ASC ? SortOrder.ASC
					: SortOrder.DESC;
			searchSourceBuilder.sort(new FieldSortBuilder(sortOption.getSortField()).order(order));
			//System.out.println(searchRequestBuilder.toString());
			SearchResponse sResponse = ElasticSearchConnector.getClient().search(searchSourceBuilder, ElasticsearchUtil.getOrganizationIndexName(ESIndexNames.INDEX_NODES, organizationId), ESIndexTypes.CREDENTIAL);
			
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
	public PaginatedResult<CredentialData> searchCredentialsForManager(
			long organizationId, String searchTerm, int page, int limit, long userId,
			CredentialSearchFilterManager filter, LearningResourceSortOption sortOption) {

		BoolQueryBuilder bQueryBuilder = QueryBuilders.boolQuery();
		bQueryBuilder.filter(configureAndGetSearchFilter(
				CredentialSearchConfig.forOriginal(true), userId, null));

		return searchCredentials(bQueryBuilder, organizationId, searchTerm, page, limit, filter, sortOption);
	}

	@Override
	public PaginatedResult<CredentialData> searchCredentialsForAdmin(
			long organizationId, long unitId, String searchTerm, int page, int limit,
			CredentialSearchFilterManager filter, LearningResourceSortOption sortOption) {

			BoolQueryBuilder bQueryBuilder = QueryBuilders.boolQuery();
			//admin should see all credentials from unit with passed id
			bQueryBuilder.filter(termQuery("units.id", unitId));
			bQueryBuilder.filter(termQuery("type", CredentialType.Original.name().toLowerCase()));

			return searchCredentials(bQueryBuilder, organizationId, searchTerm, page, limit, filter, sortOption);
	}

	private PaginatedResult<CredentialData> searchCredentials(
			BoolQueryBuilder bQueryBuilder, long organizationId, String searchTerm, int page, int limit,
			CredentialSearchFilterManager filter, LearningResourceSortOption sortOption) {
		PaginatedResult<CredentialData> response = new PaginatedResult<>();
		try {
			int start = 0;
			start = setStart(page, limit);

			if (searchTerm != null && !searchTerm.isEmpty()) {
				QueryBuilder qb = QueryBuilders
						.queryStringQuery(ElasticsearchUtil.escapeSpecialChars(searchTerm.toLowerCase()) + "*").useDisMax(true)
						.defaultOperator(Operator.AND)
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

			String[] includes = {"id", "title", "archived"};
			SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
			searchSourceBuilder
					.query(bQueryBuilder)
					.from(start)
					.size(limit)
					.fetchSource(includes, null);

			//add sorting
			SortOrder order = sortOption.getSortOrder() ==
					org.prosolo.services.util.SortingOption.ASC ? SortOrder.ASC
					: SortOrder.DESC;
			searchSourceBuilder.sort(new FieldSortBuilder(sortOption.getSortField()).order(order));
			//System.out.println(searchRequestBuilder.toString());
			SearchResponse sResponse = ElasticSearchConnector.getClient().search(searchSourceBuilder, ElasticsearchUtil.getOrganizationIndexName(ESIndexNames.INDEX_NODES, organizationId), ESIndexTypes.CREDENTIAL);

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
						String title = hit.getSource().get("title").toString();
						boolean archived = Boolean.parseBoolean(hit.getSource().get("archived").toString());
						CredentialData cd = new CredentialData(false);
						cd.setId(id);
						cd.setTitle(title);
						cd.setArchived(archived);
						cd.setDeliveries(credentialManager.getActiveDeliveries(id));
						cd.startObservingChanges();
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

	/**
	 *
	 * @param config
	 * @param userId
	 * @param unitIds - if credential enrollment is opened to everyone it is returned only if it is connected
	 *                  to at least one of the units from this list
	 * @return
	 */
	private QueryBuilder configureAndGetSearchFilter(CredentialSearchConfig config, long userId, List<Long> unitIds) {
		BoolQueryBuilder bf = QueryBuilders.boolQuery();
		bf.filter(QueryBuilders.termQuery("type", config.getType().toString().toLowerCase()));
		BoolQueryBuilder boolFilter = QueryBuilders.boolQuery();
		if (config.getType() == CredentialType.Delivery) {
			if (config.shouldIncludeResourcesWithViewPrivilege()) {
				/*
				 * users with learn privilege (or when credential is visible to all, all users that have student role
				 * in at least one of the units connected to credential) can see credential delivery
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
						.gt(ElasticsearchUtil.getDateStringRepresentation(now)));
				publishedAndVisibleFilter.filter(endDateFilter);
				BoolQueryBuilder visibleFilter = QueryBuilders.boolQuery();
				visibleFilter.should(QueryBuilders.termQuery("usersWithViewPrivilege.id", userId));
				if (unitIds != null && !unitIds.isEmpty()) {
					BoolQueryBuilder visibleToAllFilter = QueryBuilders.boolQuery();

					visibleToAllFilter.filter(QueryBuilders.termQuery("visibleToAll", true));

					BoolQueryBuilder unitFilter = QueryBuilders.boolQuery();
					for (long unitId : unitIds) {
						unitFilter.should(QueryBuilders.termQuery("units.id", unitId));
					}
					visibleToAllFilter.filter(unitFilter);

					visibleFilter.should(visibleToAllFilter);
				}

				publishedAndVisibleFilter.filter(visibleFilter);
				
				boolFilter.should(publishedAndVisibleFilter);
			}
			
			if (config.shouldIncludeEnrolledResources()) {
				//user is enrolled in a credential (currently learning or completed competence)
				boolFilter.should(QueryBuilders.termQuery("students.id", userId));
			}
			
			if (config.shouldIncludeResourcesWithInstructPrivilege()) {
				/*
				 * we don't need to store users with instruct privilege separately because we already store 
				 * collection of instructors for credential
				 */
				boolFilter.should(QueryBuilders.termQuery("instructors.id", userId));
			}
		}
		
		if (config.shouldIncludeResourcesWithEditPrivilege()) {
			//user has Edit privilege for credential
			boolFilter.should(QueryBuilders.termQuery("usersWithEditPrivilege.id", userId));
		}
		
		bf.filter(boolFilter);
		return bf;
	}
	
}
