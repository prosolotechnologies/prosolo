package org.prosolo.search.impl;

import static org.elasticsearch.index.query.QueryBuilders.termQuery;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.NoNodeAvailableException;
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
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.common.domainmodel.credential.LearningResourceType;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.search.CompetenceTextSearch;
import org.prosolo.search.util.competences.CompetenceSearchFilter;
import org.prosolo.search.util.credential.LearningResourceSearchConfig;
import org.prosolo.search.util.credential.LearningResourceSearchFilter;
import org.prosolo.search.util.credential.LearningResourceSortOption;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.indexing.ESIndexer;
import org.prosolo.services.indexing.ElasticSearchFactory;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.data.CompetenceData1;
import org.prosolo.services.nodes.data.Role;
import org.prosolo.services.nodes.factory.CompetenceDataFactory;
import org.prosolo.web.search.data.SortingOption;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * 
 * @author stefanvuckovic
 *
 */
@Service("org.prosolo.search.CompetenceTextSearch")
public class CompetenceTextSearchImpl extends AbstractManagerImpl implements CompetenceTextSearch {

	private static final long serialVersionUID = 3681577496657092978L;

	private static Logger logger = Logger.getLogger(CompetenceTextSearchImpl.class);
	
	@Inject private ESIndexer esIndexer;
	@Inject private Competence1Manager compManager;
	@Inject private CompetenceDataFactory compFactory;
	
	@Override
	@Transactional
	public TextSearchResponse1<CompetenceData1> searchCompetences(long userId, Role role,
			String searchString, int page, int limit, boolean loadOneMore,
			long[] toExclude, List<Tag> filterTags, SortingOption sortTitleAsc) {
		System.out.println("searchCompetences:"+page+" limit:"+limit);
		TextSearchResponse1<CompetenceData1> response = new TextSearchResponse1<>();
		
		try {
			int start = setStart(page, limit);
			limit = setLimit(limit, loadOneMore);
			
			Client client = ElasticSearchFactory.getClient();
			esIndexer.addMapping(client, ESIndexNames.INDEX_NODES, ESIndexTypes.COMPETENCE1);
			
			BoolQueryBuilder bQueryBuilder = QueryBuilders.boolQuery();
			
			if(searchString != null && !searchString.isEmpty()) {
				QueryBuilder qb = QueryBuilders
						.queryStringQuery(searchString.toLowerCase() + "*").useDisMax(true)
						.defaultOperator(QueryStringQueryBuilder.Operator.AND)
						.field("title");
				
				bQueryBuilder.filter(qb);
			}
		
			if (filterTags != null) {
				for (Tag tag : filterTags) {
					QueryBuilder tagQB = QueryBuilders
							.queryStringQuery(tag.getTitle()).useDisMax(true)
							.defaultOperator(QueryStringQueryBuilder.Operator.AND)
							.field("tags.title");
					bQueryBuilder.filter(tagQB);
				}
			}
			
			if (toExclude != null) {
				for (int i = 0; i < toExclude.length; i++) {
					bQueryBuilder.mustNot(termQuery("id", toExclude[i]));
				}
			}
			
			BoolQueryBuilder boolFilter = QueryBuilders.boolQuery();
			
			//competence is published and: visible to all users or user has View privilege
			BoolQueryBuilder publishedAndVisibleFilter = QueryBuilders.boolQuery();
			publishedAndVisibleFilter.filter(QueryBuilders.termQuery("published", true));
			BoolQueryBuilder visibleFilter = QueryBuilders.boolQuery();
			visibleFilter.should(QueryBuilders.termQuery("visibleToAll", true));
			visibleFilter.should(QueryBuilders.termQuery("usersWithViewPrivilege.id", userId));
			publishedAndVisibleFilter.filter(visibleFilter);
			
			boolFilter.should(publishedAndVisibleFilter);
			
			//user is owner of a competence
			boolFilter.should(QueryBuilders.termQuery("creatorId", userId));
			
			//user has Edit privilege for competence
			boolFilter.should(QueryBuilders.termQuery("usersWithEditPrivilege.id", userId));
			
			bQueryBuilder.filter(boolFilter);
			
			SearchRequestBuilder searchResultBuilder = client
					.prepareSearch(ESIndexNames.INDEX_NODES)
					.setTypes(ESIndexTypes.COMPETENCE1)
					.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
					.setQuery(bQueryBuilder).setFrom(start).setSize(limit);
			
			if (!sortTitleAsc.equals(SortingOption.NONE)) {
				switch (sortTitleAsc) {
					case ASC:
						searchResultBuilder.addSort("title", SortOrder.ASC);
						break;
					case DESC:
						searchResultBuilder.addSort("title", SortOrder.DESC);
						break;
					default:
						break;
				}
			}
			//System.out.println("SEARCH QUERY:"+searchResultBuilder.toString());
			SearchResponse sResponse = searchResultBuilder
					.execute().actionGet();
			
			if (sResponse != null) {
				response.setHitsNumber(sResponse.getHits().getTotalHits());
				
				for (SearchHit hit : sResponse.getHits()) {
					Long id = ((Integer) hit.getSource().get("id")).longValue();
					
					try {
						CompetenceData1 cd = compManager.getCompetenceData(0, id, true, false, false, 
								userId, UserGroupPrivilege.None, false);
						
						if (cd != null) {
							response.addFoundNode(cd);
						}
					} catch (DbConnectionException e) {
						logger.error(e);
					}
				}
			}
			return response;
		} catch (NoNodeAvailableException e1) {
			logger.error(e1);
			e1.printStackTrace();
			return null;
		}
	}
	
	@Override
	public TextSearchResponse1<CompetenceData1> searchCompetences(
			String searchTerm, int page, int limit, long userId, 
			LearningResourceSearchFilter filter, LearningResourceSortOption sortOption, 
			LearningResourceSearchConfig config) {
		TextSearchResponse1<CompetenceData1> response = new TextSearchResponse1<>();
		try {
			int start = 0;
			start = setStart(page, limit);

			Client client = ElasticSearchFactory.getClient();
			esIndexer.addMapping(client, ESIndexNames.INDEX_NODES, ESIndexTypes.COMPETENCE1);
			
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
				case ENROLLED:
					bQueryBuilder.filter(termQuery("students.id", userId));
					break;
				default:
					break;
			}
			
			bQueryBuilder.filter(configureAndGetSearchFilter(config, userId));
			
//			FilteredQueryBuilder filteredQueryBuilder = QueryBuilders.filteredQuery(bQueryBuilder, 
//					boolFilter);
			
			//System.out.println("QUERY: " + filteredQueryBuilder.toString());
			
			String[] includes = {"id"};
			SearchRequestBuilder searchRequestBuilder = client.prepareSearch(ESIndexNames.INDEX_NODES)
					.setTypes(ESIndexTypes.COMPETENCE1)
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
							CompetenceData1 cd = null;
							/*
							 * we should include user progress for this competence only
							 * if includeEnrolledCompetences is true
							 */
							if(config.shouldIncludeEnrolledResources()) {
								cd = compManager
										.getCompetenceDataWithProgressIfExists(id, userId);
							} else {
								cd = compManager
										.getBasicCompetenceData(id, userId);
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
	
	@Override
	public TextSearchResponse1<CompetenceData1> searchCompetencesForManager(
			String searchTerm, int page, int limit, long userId, 
			CompetenceSearchFilter filter, LearningResourceSortOption sortOption) {
		TextSearchResponse1<CompetenceData1> response = new TextSearchResponse1<>();
		try {
			int start = 0;
			start = setStart(page, limit);

			Client client = ElasticSearchFactory.getClient();
			esIndexer.addMapping(client, ESIndexNames.INDEX_NODES, ESIndexTypes.COMPETENCE1);
			
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
				case PUBLISHED:
					bQueryBuilder.filter(termQuery("published", true));
					bQueryBuilder.filter(termQuery("archived", false));
					break;
				case DRAFT:
					bQueryBuilder.filter(termQuery("published", false));
					bQueryBuilder.filter(termQuery("archived", false));
					break;
				case ARCHIVED:
					bQueryBuilder.filter(termQuery("archived", true));
					break;
				default:
					break;
			}
			
			
			bQueryBuilder.filter(configureAndGetSearchFilter(
					LearningResourceSearchConfig.of(false, false, false, true), userId));
			
			String[] includes = {"id", "title", "published", "archived", "datePublished"};
			SearchRequestBuilder searchRequestBuilder = client.prepareSearch(ESIndexNames.INDEX_NODES)
					.setTypes(ESIndexTypes.COMPETENCE1)
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
						boolean published = Boolean.parseBoolean(hit.getSource().get("published").toString());
						boolean archived = Boolean.parseBoolean(hit.getSource().get("archived").toString());
						String datePublishedString = (String) hit.getSource().get("datePublished");
						Date datePublished = null;
						if(datePublishedString != null && !datePublishedString.isEmpty()) {
							DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
							datePublished = df.parse(datePublishedString);
						}
						Competence1 comp = new Competence1();
						comp.setId(id);
						comp.setTitle(title);
						comp.setPublished(published);
						comp.setArchived(archived);
						comp.setDatePublished(datePublished);

						CompetenceData1 cd = compFactory.getCompetenceData(null, comp, null, false);
						cd.setNumberOfStudents(compManager.countNumberOfStudentsLearningCompetence(id));
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
	
	private QueryBuilder configureAndGetSearchFilter(LearningResourceSearchConfig config, long userId) {
		BoolQueryBuilder boolFilter = QueryBuilders.boolQuery();
		
		if(config.shouldIncludeResourcesWithViewPrivilege()) {
			//competence is published and: visible to all users or user has View privilege
			BoolQueryBuilder publishedAndVisibleFilter = QueryBuilders.boolQuery();
			publishedAndVisibleFilter.filter(QueryBuilders.termQuery("published", true));
			BoolQueryBuilder visibleFilter = QueryBuilders.boolQuery();
			visibleFilter.should(QueryBuilders.termQuery("visibleToAll", true));
			visibleFilter.should(QueryBuilders.termQuery("usersWithViewPrivilege.id", userId));
			publishedAndVisibleFilter.filter(visibleFilter);
			
			boolFilter.should(publishedAndVisibleFilter);
		}
		
		if(config.shouldIncludeEnrolledResources()) {
			//user is enrolled in a competence (currently learning or completed competence)
			boolFilter.should(QueryBuilders.termQuery("students.id", userId));
		}
		
		if(config.shouldIncludeResourcesWithEditPrivilege()) {
			//user is owner of a competence
			boolFilter.should(QueryBuilders.termQuery("creatorId", userId));
			
			//user has Edit privilege for competence
			boolFilter.should(QueryBuilders.termQuery("usersWithEditPrivilege.id", userId));
		}
		
		return boolFilter;
	}

	private int setStart(int page, int limit){
		int start = 0;
		if (page >= 0 && limit > 0) {
			start = page * limit;
		}
		return start;
	}
	
	private int setLimit(int limit, boolean loadOneMore){
		if (limit > 0) {
			if (loadOneMore) {
				limit = limit + 1;
			}
		}
		return limit;
	}
	
}
