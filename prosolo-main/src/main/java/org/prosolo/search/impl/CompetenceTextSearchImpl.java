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
import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.common.domainmodel.credential.LearningResourceType;
import org.prosolo.common.elasticsearch.ElasticSearchConnector;
import org.prosolo.common.util.ElasticsearchUtil;
import org.prosolo.search.CompetenceTextSearch;
import org.prosolo.search.data.SortingOption;
import org.prosolo.search.util.competences.CompetenceSearchFilter;
import org.prosolo.search.util.credential.CompetenceLibrarySearchFilter;
import org.prosolo.search.util.credential.CompetenceSearchConfig;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.OrganizationManager;
import org.prosolo.services.nodes.data.competence.CompetenceData1;
import org.prosolo.services.nodes.factory.CompetenceDataFactory;
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
@Service("org.prosolo.search.CompetenceTextSearch")
public class CompetenceTextSearchImpl extends AbstractManagerImpl implements CompetenceTextSearch {

	private static final long serialVersionUID = 3681577496657092978L;

	private static Logger logger = Logger.getLogger(CompetenceTextSearchImpl.class);
	
	//@Inject private ESIndexer esIndexer;
	@Inject private Competence1Manager compManager;
	@Inject private CompetenceDataFactory compFactory;
	@Inject private OrganizationManager orgManager;
	@Inject private CredentialManager credentialManager;
	
	@Override
	public PaginatedResult<CompetenceData1> searchCompetencesForAddingToCredential(long organizationId, long userId,
                                                                                   String searchString, int page, int limit, boolean loadOneMore,
                                                                                   List<Long> unitIds, long[] toExclude, SortingOption sortTitleAsc) {
		if (unitIds == null || unitIds.isEmpty()) {
			return new PaginatedResult<>();
		}

		System.out.println("searchCompetences:"+page+" limit:"+limit);
		PaginatedResult<CompetenceData1> response = new PaginatedResult<>();
		
		try {
			int start = setStart(page, limit);
			limit = setLimit(limit, loadOneMore);
			
			BoolQueryBuilder bQueryBuilder = QueryBuilders.boolQuery();
			
			if (searchString != null && !searchString.isEmpty()) {
				QueryBuilder qb = QueryBuilders
						.queryStringQuery(ElasticsearchUtil.escapeSpecialChars(searchString.toLowerCase()) + "*")
						.defaultOperator(Operator.AND)
						.field("title");
				
				bQueryBuilder.filter(qb);
			}
		
//			if (filterTags != null) {
//				for (Tag tag : filterTags) {
//					QueryBuilder tagQB = QueryBuilders
//							.queryStringQuery(tag.getTitle()).useDisMax(true)
//							.defaultOperator(QueryStringQueryBuilder.Operator.AND)
//							.field("tags.title");
//					bQueryBuilder.filter(tagQB);
//				}
//			}

			BoolQueryBuilder unitFilter = QueryBuilders.boolQuery();
			for (long unitId : unitIds) {
				unitFilter.should(termQuery("units.id", unitId));
			}
			bQueryBuilder.filter(unitFilter);
			
			if (toExclude != null) {
				for (int i = 0; i < toExclude.length; i++) {
					bQueryBuilder.mustNot(termQuery("id", toExclude[i]));
				}
			}
			
			//university created type is set because credentail can only be university created
			bQueryBuilder.filter(configureAndGetSearchFilter(
					CompetenceSearchConfig.of(
							false, false, false, true, LearningResourceType.UNIVERSITY_CREATED), userId, null));
			
//			SearchRequestBuilder searchResultBuilder = client
//					.prepareSearch(indexName)
//					.setTypes(ESIndexTypes.COMPETENCE)
//					.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
//					.setQuery(bQueryBuilder).setFrom(start).setSize(limit);

			SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
			searchSourceBuilder
					.query(bQueryBuilder)
					.from(start)
					.size(limit);
			
			if (!sortTitleAsc.equals(SortingOption.NONE)) {
				switch (sortTitleAsc) {
					case ASC:
						searchSourceBuilder.sort(new FieldSortBuilder("title.sort").order(SortOrder.ASC));
						break;
					case DESC:
						searchSourceBuilder.sort(new FieldSortBuilder("title.sort").order(SortOrder.DESC));
						break;
					default:
						break;
				}
			}
			//System.out.println("SEARCH QUERY:"+searchResultBuilder.toString());
			SearchResponse sResponse = ElasticSearchConnector.getClient().search(searchSourceBuilder, ElasticsearchUtil.getOrganizationIndexName(ESIndexNames.INDEX_COMPETENCES, organizationId), ESIndexTypes.COMPETENCE);
			
			if (sResponse != null) {
				response.setHitsNumber(sResponse.getHits().getTotalHits());
				
				for (SearchHit hit : sResponse.getHits()) {
					Long id = ((Integer) hit.getSourceAsMap().get("id")).longValue();
					
					try {
						/*
						 * access rights are already checked when querying ES, so we don't need to do that again
						 */
						CompetenceData1 res = compManager.getCompetenceData(id, false, false, false, false, false);
						
						if (res != null) {
							response.addFoundNode(res);
						}
					} catch (DbConnectionException e) {
						logger.error(e);
					}
				}
			}
			return response;
		} catch (Exception e1) {
			logger.error(e1);
			e1.printStackTrace();
			return null;
		}
	}
	
	@Override
	public PaginatedResult<CompetenceData1> searchCompetences(
			long organizationId, String searchTerm, int page, int limit, long userId,
			List<Long> unitIds, CompetenceLibrarySearchFilter filter, CompetenceSearchConfig config) {
		PaginatedResult<CompetenceData1> response = new PaginatedResult<>();
		try {
			int start = 0;
			start = setStart(page, limit);
			
			BoolQueryBuilder bQueryBuilder = QueryBuilders.boolQuery();
			
			if (searchTerm != null && !searchTerm.isEmpty()) {
				QueryBuilder qb = QueryBuilders
						.queryStringQuery(ElasticsearchUtil.escapeSpecialChars(searchTerm.toLowerCase()) + "*")
						.defaultOperator(Operator.AND)
						.field("title").field("description");
						//.field("tags.title").field("hashtags.title");
				
				bQueryBuilder.filter(qb);
			}
			
			//bQueryBuilder.minimumNumberShouldMatch(1);
			
			switch(filter) {
				case ALL_COMPETENCES:
					break;
				case BOOKMARKS:
					bQueryBuilder.filter(termQuery("bookmarkedBy.id", userId));
					break;
//				case FROM_CREATOR:
//					bQueryBuilder.filter(termQuery("creatorId", userId));
//					//this filter returns competencies created by student with userId but only if
//					bQueryBuilder.filter(termQuery("type",
//							LearningResourceType.USER_CREATED.toString()));
//					break;
//				case BY_OTHER_STUDENTS:
//					bQueryBuilder.mustNot(termQuery("creatorId", userId));
//					bQueryBuilder.filter(termQuery("type",
//							LearningResourceType.USER_CREATED.toString()));
//					break;
//				case UNIVERSITY:
//					bQueryBuilder.filter(termQuery("type",
//							LearningResourceType.UNIVERSITY_CREATED.toString()));
//					break;
//				case BY_STUDENTS:
//					//bQueryBuilder.mustNot(termQuery("creatorId", userId));
//					bQueryBuilder.filter(termQuery("type",
//							LearningResourceType.USER_CREATED.toString()));
//					break;
				case ENROLLED:
					bQueryBuilder.filter(termQuery("students.id", userId));
					break;
				default:
					break;
			}
			
			bQueryBuilder.filter(configureAndGetSearchFilter(config, userId, unitIds));
			
//			FilteredQueryBuilder filteredQueryBuilder = QueryBuilders.filteredQuery(bQueryBuilder, 
//					boolFilter);
			
			//System.out.println("QUERY: " + filteredQueryBuilder.toString());
			
			String[] includes = {"id"};
			SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
			searchSourceBuilder
					.query(bQueryBuilder)
					.from(start)
					.size(limit)
					.fetchSource(includes, null);
			
			//add sorting
			searchSourceBuilder.sort("title.sort", SortOrder.ASC);
			//System.out.println(searchRequestBuilder.toString());
			SearchResponse sResponse = ElasticSearchConnector.getClient().search(searchSourceBuilder, ElasticsearchUtil.getOrganizationIndexName(ESIndexNames.INDEX_COMPETENCES, organizationId), ESIndexTypes.COMPETENCE);
			
			if (sResponse != null) {
				SearchHits searchHits = sResponse.getHits();
				response.setHitsNumber(sResponse.getHits().getTotalHits());
				if(searchHits != null) {
					for (SearchHit hit : sResponse.getHits()) {
						/*
						 * long field is parsed this way because ES is returning integer although field type
						 * is specified as long in mapping file
						 */
						Long id = Long.parseLong(hit.getSourceAsMap().get("id").toString());
						try {
							CompetenceData1 cd;
							/*
							 * we should include user progress for this competence only
							 * if includeEnrolledCompetences is true
							 */
							if (config.shouldIncludeEnrolledResources()) {
								cd = compManager.getCompetenceDataWithProgressIfExists(id, userId);
							} else {
								cd = compManager.getBasicCompetenceData(id, userId);
							}
							
							if (cd != null) {
								/*
								TODO hack retrieve id of a first credential where this competency is used
								and user has learn privilege for
								so we can always have a link to a credential because we don't fully support
								independent competency
								 */
								cd.setCredentialId(compManager.getIdOfFirstCredentialCompetenceIsAddedToAndStudentHasLearnPrivilegeFor(id, userId));

								response.addFoundNode(cd);
							}
						} catch (DbConnectionException e) {
							logger.error("Error", e);
						}
					}
				}
			}
		} catch (Exception e1) {
			logger.error("Error", e1);
		}
		return response;
	}
	
	@Override
	public PaginatedResult<CompetenceData1> searchCompetencesForManager(
			long organizationId, String searchTerm, int page, int limit, long userId,
			CompetenceSearchFilter filter) {
		PaginatedResult<CompetenceData1> response = new PaginatedResult<>();
		try {
			int start = 0;
			start = setStart(page, limit);
			
			BoolQueryBuilder bQueryBuilder = QueryBuilders.boolQuery();
			
			if (searchTerm != null && !searchTerm.isEmpty()) {
				QueryBuilder qb = QueryBuilders
						.queryStringQuery(ElasticsearchUtil.escapeSpecialChars(searchTerm.toLowerCase()) + "*")
						.defaultOperator(Operator.AND)
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
					bQueryBuilder.mustNot(QueryBuilders.existsQuery("datePublished"));
					bQueryBuilder.filter(termQuery("archived", false));
					break;
				case UNPUBLISHED:
					bQueryBuilder.filter(termQuery("published", false));
					bQueryBuilder.filter(QueryBuilders.existsQuery("datePublished"));
					bQueryBuilder.filter(termQuery("archived", false));
					break;
				case ARCHIVED:
					bQueryBuilder.filter(termQuery("archived", true));
					break;
				default:
					break;
			}
			
			
			bQueryBuilder.filter(configureAndGetSearchFilter(
					CompetenceSearchConfig.of(false, false, false, true, LearningResourceType.UNIVERSITY_CREATED), 
						userId, null));

			String[] includes = {"id", "title", "published", "archived", "datePublished", "learningStageId"};
			SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
			searchSourceBuilder
					.query(bQueryBuilder)
					.from(start)
					.size(limit)
					.fetchSource(includes, null);

			//add sorting
			searchSourceBuilder.sort("title.sort", SortOrder.ASC);
			//System.out.println(searchRequestBuilder.toString());
			SearchResponse sResponse = ElasticSearchConnector.getClient().search(searchSourceBuilder, ElasticsearchUtil.getOrganizationIndexName(ESIndexNames.INDEX_COMPETENCES, organizationId), ESIndexTypes.COMPETENCE);
			
			if (sResponse != null) {
				SearchHits searchHits = sResponse.getHits();
				response.setHitsNumber(sResponse.getHits().getTotalHits());
				if(searchHits != null) {
					for (SearchHit hit : sResponse.getHits()) {
						/*
						 * long field is parsed this way because ES is returning integer although field type
						 * is specified as long in mapping file
						 */
						Long id = Long.parseLong(hit.getSourceAsMap().get("id").toString());
						String title = hit.getSourceAsMap().get("title").toString();
						boolean published = Boolean.parseBoolean(hit.getSourceAsMap().get("published").toString());
						boolean archived = Boolean.parseBoolean(hit.getSourceAsMap().get("archived").toString());
						String datePublishedString = (String) hit.getSourceAsMap().get("datePublished");
						Date datePublished = null;
						if (datePublishedString != null && !datePublishedString.isEmpty()) {
							datePublished = ElasticsearchUtil.parseDate(datePublishedString);
						}
						Competence1 comp = new Competence1();
						comp.setId(id);
						comp.setTitle(title);
						comp.setPublished(published);
						comp.setArchived(archived);
						comp.setDatePublished(datePublished);

						CompetenceData1 cd = compFactory.getCompetenceData(null, comp, null, null, false);
						cd.setNumberOfStudents(compManager.countNumberOfStudentsLearningCompetence(id));
						long lStageId = Long.parseLong(hit.getSourceAsMap().get("learningStageId").toString());
						cd.setLearningStageEnabled(lStageId > 0);
						if (lStageId > 0) {
							cd.setLearningStage(orgManager.getLearningStageData(lStageId));
						}
						//TODO hack - get id of a first 'original' credential where this competency is used and
						//user has 'Edit' privilege for so we can have relation to credential which is necessary.
						cd.setCredentialId(credentialManager
								.getIdOfFirstCredentialCompetenceIsAddedToAndUserHasEditPrivilegeFor(id, userId));

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
	 * @param unitIds - if competence enrollment is opened to everyone it is returned only if it is connected
	 *                to at least one of the units from this list
	 * @return
	 */
	private QueryBuilder configureAndGetSearchFilter(CompetenceSearchConfig config, long userId, List<Long> unitIds) {
		BoolQueryBuilder boolFilter = QueryBuilders.boolQuery();
		
		if (config.shouldIncludeResourcesWithViewPrivilege()) {
			//competence is published and: visible to all users or user has View privilege
			BoolQueryBuilder publishedAndVisibleFilter = QueryBuilders.boolQuery();
			publishedAndVisibleFilter.filter(QueryBuilders.termQuery("published", true));
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
			//user is enrolled in a competence (currently learning or completed competence)
			boolFilter.should(QueryBuilders.termQuery("students.id", userId));
		}
		
		if (config.shouldIncludeResourcesWithEditPrivilege()) {
			BoolQueryBuilder editorAndTypeFilter = QueryBuilders.boolQuery();
			//user has Edit privilege for competence
			editorAndTypeFilter.filter(QueryBuilders.termQuery("usersWithEditPrivilege.id", userId));

			/*
			 * for edit privilege resource type should be included in condition
			 * for example: if competence is user created and user has edit privilege for that competence
			 * but university created type is set in config object, user should not see this competence
			 */
			editorAndTypeFilter.filter(QueryBuilders.termQuery("type", config.getResourceType().toString()));
			
			boolFilter.should(editorAndTypeFilter);
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
