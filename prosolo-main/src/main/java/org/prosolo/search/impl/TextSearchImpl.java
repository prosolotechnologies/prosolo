package org.prosolo.search.impl;

import static org.elasticsearch.index.query.QueryBuilders.termQuery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.elasticsearch.action.search.SearchPhaseExecutionException;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.NoNodeAvailableException;
//import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
//import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.FilteredQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
//import org.elasticsearch.index.query.NestedFilterBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.index.query.support.QueryInnerHitBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.filter.Filter;
import org.elasticsearch.search.aggregations.bucket.nested.Nested;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.sort.SortOrder;
import org.prosolo.bigdata.common.enums.ESIndexTypes;
import org.prosolo.common.domainmodel.activities.Activity;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.competences.Competence;
import org.prosolo.common.domainmodel.course.Course;
import org.prosolo.common.domainmodel.course.CreatorType;
import org.prosolo.common.domainmodel.credential.LearningResourceType;
import org.prosolo.common.domainmodel.user.LearningGoal;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.reminders.Reminder;
import org.prosolo.common.domainmodel.user.reminders.ReminderStatus;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.search.TextSearch;
import org.prosolo.search.util.credential.CredentialMembersSortOption;
import org.prosolo.search.util.credential.CredentialSearchFilter;
import org.prosolo.search.util.credential.CredentialSortOption;
import org.prosolo.search.util.credential.InstructorAssignFilter;
import org.prosolo.search.util.credential.InstructorAssignFilterValue;
import org.prosolo.search.util.credential.InstructorSortOption;
import org.prosolo.services.common.exception.DbConnectionException;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.indexing.ESIndexNames;
import org.prosolo.services.indexing.ESIndexer;
import org.prosolo.services.indexing.ElasticSearchFactory;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.CredentialInstructorManager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.DefaultManager;
import org.prosolo.services.nodes.RoleManager;
import org.prosolo.services.nodes.data.CompetenceData1;
import org.prosolo.services.nodes.data.CredentialData;
import org.prosolo.services.nodes.data.LearningResourceReturnResultType;
import org.prosolo.services.nodes.data.Role;
import org.prosolo.services.nodes.data.StudentData;
import org.prosolo.services.nodes.data.UserData;
import org.prosolo.services.nodes.data.instructor.InstructorData;
import org.prosolo.web.administration.data.RoleData;
import org.prosolo.web.search.data.SortingOption;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * @author Zoran Jeremic
 * @date Jul 1, 2012
 */
@Service("org.prosolo.search.TextSearch")
public class TextSearchImpl extends AbstractManagerImpl implements TextSearch {

	private static final long serialVersionUID = -8919953696206394473L;
	private static Logger logger = Logger.getLogger(TextSearchImpl.class);
	
	@Autowired private DefaultManager defaultManager;
	@Autowired private ESIndexer esIndexer;
	@Inject private Competence1Manager compManager;
	@Inject private CredentialManager credentialManager;
	@Inject private CredentialInstructorManager credInstructorManager;
	@Inject private RoleManager roleManager;

	@Override
	@Transactional
	public TextSearchResponse searchUsers (
			String searchString, int page, int limit, boolean loadOneMore,
			Collection<Long> excludeUserIds) {
		
		TextSearchResponse response = new TextSearchResponse();
		
		try {
			int start = setStart(page, limit);
			limit = setLimit(limit, loadOneMore);
			
			Client client = ElasticSearchFactory.getClient();
			esIndexer.addMapping(client,ESIndexNames.INDEX_USERS, ESIndexTypes.USER);
			
			QueryBuilder qb = QueryBuilders
					.queryStringQuery(searchString.toLowerCase() + "*").useDisMax(true)
					.defaultOperator(QueryStringQueryBuilder.Operator.AND)
					.field("name").field("lastname");
			
			BoolQueryBuilder bQueryBuilder = QueryBuilders.boolQuery();
			bQueryBuilder.should(qb);
			bQueryBuilder.mustNot(termQuery("system", true));
			
			if (excludeUserIds != null) {
				for (Long exUserId : excludeUserIds) {
					bQueryBuilder.mustNot(termQuery("id", exUserId));
				}
			}
			SearchResponse sResponse = null;
			
			try {
				SearchRequestBuilder srb = client.prepareSearch(ESIndexNames.INDEX_USERS)
						.setTypes(ESIndexTypes.USER)
						.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
						.setQuery(bQueryBuilder)
						.setFrom(start).setSize(limit)
						.addSort("name", SortOrder.ASC);
				//System.out.println(srb.toString());
				sResponse = srb.execute().actionGet();
			} catch (SearchPhaseExecutionException spee) {
				
			}
	
			if (sResponse != null) {
				response.setHitsNumber(sResponse.getHits().getTotalHits());
			
				for (SearchHit hit : sResponse.getHits()) {
					Long id = ((Integer) hit.getSource().get("id")).longValue();
					try {
						User user = defaultManager.loadResource(User.class, id);
						
						if (user != null) {
							response.addFoundNode(user);
						}
					} catch (ResourceCouldNotBeLoadedException e) {
						logger.error("User was not found: " + id);
					}
				}
			}
		} catch (NoNodeAvailableException e1) {
			logger.error(e1);
		}
		return response;
	}
	
	@Override
	@Transactional
	public TextSearchResponse1<UserData> searchUsers1 (
			String term, int page, int limit, boolean paginate, List<Long> excludeIds) {
		
		TextSearchResponse1<UserData> response = new TextSearchResponse1<>();
		
		try {
			int start = 0;
			int size = 1000;
			if(paginate) {
				start = setStart(page, limit);
				size = limit;
			}
			
			Client client = ElasticSearchFactory.getClient();
			esIndexer.addMapping(client,ESIndexNames.INDEX_USERS, ESIndexTypes.USER);
			
			QueryBuilder qb = QueryBuilders
					.queryStringQuery(term.toLowerCase() + "*").useDisMax(true)
					.defaultOperator(QueryStringQueryBuilder.Operator.AND)
					.field("name").field("lastname");
			
			BoolQueryBuilder bQueryBuilder = QueryBuilders.boolQuery();
			bQueryBuilder.should(qb);
			bQueryBuilder.mustNot(termQuery("system", true));
			
			if (excludeIds != null) {
				for (Long exUserId : excludeIds) {
					bQueryBuilder.mustNot(termQuery("id", exUserId));
				}
			}
			SearchResponse sResponse = null;
			
			String[] includes = {"id", "name", "lastname", "avatar"};
			SearchRequestBuilder srb = client.prepareSearch(ESIndexNames.INDEX_USERS)
					.setTypes(ESIndexTypes.USER)
					.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
					.setQuery(bQueryBuilder)
					.setFrom(start).setSize(size)
					.addSort("name", SortOrder.ASC)
					.setFetchSource(includes, null);
			//System.out.println(srb.toString());
			sResponse = srb.execute().actionGet();
			
			if (sResponse != null) {
				response.setHitsNumber(sResponse.getHits().getTotalHits());
				
				for(SearchHit sh : sResponse.getHits()) {
					Map<String, Object> fields = sh.getSource();
					User user = new User();
					user.setId(Long.parseLong(fields.get("id") + ""));
					user.setName((String) fields.get("name"));
					user.setLastname((String) fields.get("lastname"));
					user.setAvatarUrl((String) fields.get("avatar"));
					UserData userData = new UserData(user);
					
					response.addFoundNode(userData);			
				}
			}
		} catch (Exception e1) {
			e1.printStackTrace();
			logger.error(e1);
		}
		return response;
	}

	@Override
	@Transactional
	public TextSearchResponse1<org.prosolo.web.administration.data.UserData> getUsersWithRoles(
			String term, int page, int limit, boolean paginate) {
		
		TextSearchResponse1<org.prosolo.web.administration.data.UserData> response = 
				new TextSearchResponse1<>();
		
		try {
			int start = 0;
			int size = 1000;
			if(paginate) {
				start = setStart(page, limit);
				size = limit;
			}
			
			Client client = ElasticSearchFactory.getClient();
			esIndexer.addMapping(client,ESIndexNames.INDEX_USERS, ESIndexTypes.USER);
			
			QueryBuilder qb = QueryBuilders
					.queryStringQuery(term.toLowerCase() + "*").useDisMax(true)
					.defaultOperator(QueryStringQueryBuilder.Operator.AND)
					.field("name").field("lastname");
			
			BoolQueryBuilder bQueryBuilder = QueryBuilders.boolQuery();
			bQueryBuilder.should(qb);
			
			SearchResponse sResponse = null;
			
			String[] includes = {"id", "name", "lastname", "avatar", "roles"};
			SearchRequestBuilder srb = client.prepareSearch(ESIndexNames.INDEX_USERS)
					.setTypes(ESIndexTypes.USER)
					.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
					.setQuery(bQueryBuilder)
					.setFrom(start).setSize(size)
					.addSort("name", SortOrder.ASC)
					.setFetchSource(includes, null);
			//System.out.println(srb.toString());
			sResponse = srb.execute().actionGet();
			
			if (sResponse != null) {
				response.setHitsNumber(sResponse.getHits().getTotalHits());
				List<org.prosolo.common.domainmodel.organization.Role> roles = roleManager.getAllRoles();
				for(SearchHit sh : sResponse.getHits()) {
					Map<String, Object> fields = sh.getSource();
					User user = new User();
					user.setId(Long.parseLong(fields.get("id") + ""));
					user.setName((String) fields.get("name"));
					user.setLastname((String) fields.get("lastname"));
					user.setAvatarUrl((String) fields.get("avatar"));
					@SuppressWarnings("unchecked")
					List<Map<String, Object>> rolesList = (List<Map<String, Object>>) fields.get("roles");
					List<org.prosolo.common.domainmodel.organization.Role> userRoles = new ArrayList<>();
					if(rolesList != null) {
						for(Map<String, Object> map : rolesList) {
							org.prosolo.common.domainmodel.organization.Role r = getRoleDataForId(roles, Long.parseLong(map.get("id") + ""));
							if(r != null) {
								userRoles.add(r);
							}
						}
					}
					org.prosolo.web.administration.data.UserData userData = 
							new org.prosolo.web.administration.data.UserData(user, userRoles);
					
					response.addFoundNode(userData);			
				}
			}
		} catch (Exception e1) {
			e1.printStackTrace();
			logger.error(e1);
		}
		return response;
	}
	
	private org.prosolo.common.domainmodel.organization.Role getRoleDataForId(List<org.prosolo.common.domainmodel.organization.Role> roles, 
			long roleId) {
		if(roles != null) {
			for(org.prosolo.common.domainmodel.organization.Role r : roles) {
				if(roleId == r.getId()) {
					return r;
				}
			}
		}
		return null;
	}

	@Override
	@Transactional
	public TextSearchResponse searchLearningGoals(
			String searchString, int page, int limit, boolean loadOneMore,
			Collection<LearningGoal> excludeGoals) {
		
		TextSearchResponse response = new TextSearchResponse();
		
		try {
			int start = setStart(page, limit);
			limit = setLimit(limit, loadOneMore);
			
			Client client = ElasticSearchFactory.getClient();
			esIndexer.addMapping(client,ESIndexNames.INDEX_NODES,ESIndexTypes.LEARNINGGOAL);
			
			QueryBuilder qb = QueryBuilders
					.queryStringQuery(searchString.toLowerCase() + "*").useDisMax(true)
					.defaultOperator(QueryStringQueryBuilder.Operator.AND)
					.field("title");
			
			BoolQueryBuilder bQueryBuilder = QueryBuilders.boolQuery();
			bQueryBuilder.should(qb);
			
			if (excludeGoals != null) {
				for (LearningGoal lGoal : excludeGoals) {
					if (lGoal != null) {
						bQueryBuilder.mustNot(termQuery("id", lGoal.getId()));
					}
				}
			}
			
			SearchResponse sResponse = client
					.prepareSearch(ESIndexNames.INDEX_NODES)
					.setTypes(ESIndexTypes.LEARNINGGOAL)
					.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
					.setQuery(bQueryBuilder).setFrom(start).setSize(limit)
					.addSort("title", SortOrder.ASC)
					.execute().actionGet();
			
			
			if (sResponse != null) {
				response.setHitsNumber(sResponse.getHits().getTotalHits());
			
				for (SearchHit hit : sResponse.getHits()) {
					Long id = ((Integer) hit.getSource().get("id")).longValue();
					
					try {
						LearningGoal learningGoal = defaultManager.loadResource(LearningGoal.class, id);
						
						if (learningGoal != null) {
							response.addFoundNode(learningGoal);
						}
					} catch (ResourceCouldNotBeLoadedException e) {
						logger.error("LearningGoal was not found: " + id);
					}
				}
			}
		} catch (NoNodeAvailableException e1) {
			logger.error(e1);
		}
		return response;
	}

	@Override
	@Transactional
	public TextSearchResponse searchCompetences(
			String searchString, int page, int limit, boolean loadOneMore,
			long[] toExclude, List<Tag> filterTags, SortingOption sortTitleAsc) {
		System.out.println("searchCompetences:"+page+" limit:"+limit);
		
		TextSearchResponse response = new TextSearchResponse();
		
		try {
			int start = setStart(page, limit);
			limit = setLimit(limit, loadOneMore);
			
			Client client = ElasticSearchFactory.getClient();
			esIndexer.addMapping(client, ESIndexNames.INDEX_NODES, ESIndexTypes.COMPETENCE);
			
			QueryBuilder qb = QueryBuilders
					.queryStringQuery(searchString.toLowerCase() + "*").useDisMax(true)
					.defaultOperator(QueryStringQueryBuilder.Operator.AND)
					.field("title");
	
			BoolQueryBuilder bQueryBuilder = QueryBuilders.boolQuery();
			bQueryBuilder.should(qb);
			
	//		if (filterTags != null) {
	//			for (Annotation tag : filterTags) {
	//				bQueryBuilder.must(termQuery("tags.title", tag.getTitle()));
	//			}
	//		}
			if (filterTags != null) {
				for (Tag tag : filterTags) {
					QueryBuilder tagQB = QueryBuilders
							.queryStringQuery(tag.getTitle()).useDisMax(true)
							.defaultOperator(QueryStringQueryBuilder.Operator.AND)
							.field("tags.title");
					bQueryBuilder.must(tagQB);
				}
			}
			
			if (toExclude != null) {
				for (int i = 0; i < toExclude.length; i++) {
					bQueryBuilder.mustNot(termQuery("id", toExclude[i]));
				}
			}
			
			SearchRequestBuilder searchResultBuilder = client
					.prepareSearch(ESIndexNames.INDEX_NODES)
					.setTypes(ESIndexTypes.COMPETENCE)
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
						Competence competence = defaultManager.loadResource(Competence.class, id);
						competence.getMaker();
						
						if (competence != null) {
							response.addFoundNode(competence);
						}
					} catch (ResourceCouldNotBeLoadedException e) {
						logger.error("Competence was not found: " + id);
					}
				}
			}
		} catch (NoNodeAvailableException e1) {
			logger.error(e1);
		}
		return response;
	}
	
	//query for new competence
	@Override
	@Transactional
	public TextSearchResponse1<CompetenceData1> searchCompetences1(long userId, Role role,
			String searchString, int page, int limit, boolean loadOneMore,
			long[] toExclude, List<Tag> filterTags, SortingOption sortTitleAsc) {
		System.out.println("searchCompetences1:"+page+" limit:"+limit);
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
				
				bQueryBuilder.should(qb);
			}
		
			if (filterTags != null) {
				for (Tag tag : filterTags) {
					QueryBuilder tagQB = QueryBuilders
							.queryStringQuery(tag.getTitle()).useDisMax(true)
							.defaultOperator(QueryStringQueryBuilder.Operator.AND)
							.field("tags.title");
					bQueryBuilder.must(tagQB);
				}
			}
			
			if (toExclude != null) {
				for (int i = 0; i < toExclude.length; i++) {
					bQueryBuilder.mustNot(termQuery("id", toExclude[i]));
				}
			}
			
			//BoolQueryBuilder boolFilter = QueryBuilders.boolQuery();
			
			/*
			 * include all published competences, draft competences that have draft version and first time
			 * drafts
			 */
			BoolQueryBuilder boolFilter = QueryBuilders.boolQuery();
			boolFilter.should(QueryBuilders.termQuery("published", true));
			BoolQueryBuilder hasDraft = QueryBuilders.boolQuery();
			hasDraft.must(QueryBuilders.termQuery("published", false));
			hasDraft.must(QueryBuilders.termQuery("hasDraft", true));
			boolFilter.should(hasDraft);
			BoolQueryBuilder firstTimeDraft = QueryBuilders.boolQuery();
			if(role == Role.Manager) {
				firstTimeDraft.must(QueryBuilders.termQuery(
						"type", LearningResourceType.UNIVERSITY_CREATED.toString().toLowerCase()));
			} else {
				firstTimeDraft.must(QueryBuilders.termQuery("creatorId", userId));
			}
			firstTimeDraft.must(QueryBuilders.termQuery("isDraft", false));
			firstTimeDraft.must(QueryBuilders.termQuery("published", false));
			firstTimeDraft.must(QueryBuilders.termQuery("hasDraft", false));
			boolFilter.should(firstTimeDraft);
			
			QueryBuilder filteredQueryBuilder = QueryBuilders.filteredQuery(bQueryBuilder,
					boolFilter);
			
			SearchRequestBuilder searchResultBuilder = client
					.prepareSearch(ESIndexNames.INDEX_NODES)
					.setTypes(ESIndexTypes.COMPETENCE1)
					.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
					.setQuery(filteredQueryBuilder).setFrom(start).setSize(limit);
			
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
						CompetenceData1 cd = compManager.getCompetenceData(0, id, true, 
								false, false, 0, LearningResourceReturnResultType.ANY, 
								false);
						
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
	@Transactional
	public TextSearchResponse searchActivities(
			String searchString, int page, int limit, boolean loadOneMore,
			long[] activitiesToExclude) {
		
		TextSearchResponse response = new TextSearchResponse();
		
		try {
			int start = setStart(page, limit);
			limit = setLimit(limit, loadOneMore);
			
			Client client = ElasticSearchFactory.getClient();
			esIndexer.addMapping(client,ESIndexNames.INDEX_NODES, ESIndexTypes.ACTIVITY);
			
			QueryBuilder qb = QueryBuilders
					.queryStringQuery(searchString.toLowerCase() + "*").useDisMax(true)
					.defaultOperator(QueryStringQueryBuilder.Operator.AND)
					.field("title");
			
			BoolQueryBuilder bQueryBuilder = QueryBuilders.boolQuery();
			bQueryBuilder.should(qb);
			
			if (activitiesToExclude != null) {
				for (long activityId : activitiesToExclude) {
					bQueryBuilder.mustNot(termQuery("id", activityId));
				}
			}
			
			SearchResponse sResponse = client
					.prepareSearch(ESIndexNames.INDEX_NODES)
					.setTypes(ESIndexTypes.ACTIVITY)
					.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
					.setQuery(bQueryBuilder).setFrom(start).setSize(limit)
					.addSort("title", SortOrder.ASC)
					.setExplain(true).execute().actionGet();
			
			if (sResponse != null) {
				response.setHitsNumber(sResponse.getHits().getTotalHits());
				
				for (SearchHit hit : sResponse.getHits()) {
					Long id = ((Integer) hit.getSource().get("id")).longValue();
					try {
						Activity activity = defaultManager.get(Activity.class, id);
						//activity = HibernateUtil.initializeAndUnproxy(activity);
						if (activity != null) {
							response.addFoundNode(activity);
						}
					} catch (ResourceCouldNotBeLoadedException e) {
						logger.error("Activity was not found: " + id);
					}
				}
			}
		} catch (NoNodeAvailableException e1) {
			logger.error(e1);
		}
		return response;
	}

	@Override
	public List<Reminder> searchReminders(String searchString,
			ReminderStatus status, int page, int limit, boolean loadOneMore) {
		return null;
	}

	@Override
	public TextSearchResponse searchCourses(
			String searchQuery, CreatorType creatorType, int page, int limit, boolean loadOneMore,
			Collection<Course> excludeCourses, boolean published, List<Tag> filterTags, List<Long> courseIds,
			SortingOption sortTitleAsc, SortingOption sortDateAsc) {
		
		TextSearchResponse response = new TextSearchResponse();
		
		try {
			int start = setStart(page, limit);
			limit = setLimit(limit, loadOneMore);
			
			Client client = ElasticSearchFactory.getClient();
			esIndexer.addMapping(client, ESIndexNames.INDEX_NODES, ESIndexTypes.COURSE );
			
			QueryBuilder qb = QueryBuilders
					.queryStringQuery(searchQuery.toLowerCase() + "*").useDisMax(true)
					.defaultOperator(QueryStringQueryBuilder.Operator.AND)
					.field("description")
					.field("tags.title")
					.field("title");
			
			BoolQueryBuilder bQueryBuilder = QueryBuilders.boolQuery();
			bQueryBuilder.must(qb);
			
			if(courseIds != null && !courseIds.isEmpty()) {
				bQueryBuilder.must(QueryBuilders.termsQuery("id", courseIds));
			}
			
			if (filterTags != null) {
				for (Tag tag : filterTags) {
					QueryBuilder tagQB = QueryBuilders
							.queryStringQuery(tag.getTitle()).useDisMax(true)
							.defaultOperator(QueryStringQueryBuilder.Operator.AND)
							.field("tags.title");
					bQueryBuilder.must(tagQB);
				}
			}
			
			if (creatorType != null) {
				bQueryBuilder.must(termQuery("creatorType", creatorType.name()));
			}
			
			if (published) {
				bQueryBuilder.must(termQuery("publisher", published));
			}
			
			if (excludeCourses != null) {
				for (Course course : excludeCourses) {
					if (course != null) {
						bQueryBuilder.mustNot(termQuery("id", course.getId()));
					}
				}
			}
			SearchRequestBuilder searchResultBuilder = client
					.prepareSearch(ESIndexNames.INDEX_NODES)
					.setTypes(ESIndexTypes.COURSE)
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
			
			if (!sortDateAsc.equals(SortingOption.NONE)) {
				// TODO Zoran: dateCreated should also be in indexes
	//			switch (sortDateAsc) {
	//				case ASC:
	//					searchResultBuilder.addSort("dateCreated", SortOrder.ASC);
	//					break;
	//				case DESC:
	//					searchResultBuilder.addSort("dateCreated", SortOrder.DESC);
	//					break;
	//				default:
	//					break;
	//			}
			}
			
			SearchResponse sResponse = searchResultBuilder
					.execute().actionGet();
			
			if (sResponse != null) {
				response.setHitsNumber(sResponse.getHits().getTotalHits());
				
				for (SearchHit hit : sResponse.getHits()) {
					Long id = ((Integer) hit.getSource().get("id")).longValue();
					
					try {
						Course course = defaultManager.loadResource(Course.class, id);
						
						course = HibernateUtil.initializeAndUnproxy(course);
						
						if (course != null) {
							response.addFoundNode(course);
						}
					} catch (ResourceCouldNotBeLoadedException e) {
						logger.error("Course was not found: " + id);
					}
				}
			}
		} catch (NoNodeAvailableException e1) {
			logger.error(e1);
		}
		return response;
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
	
	@Override
	public TextSearchResponse searchTags(String searchQuery, int page, int limit,
			boolean loadOneMore, Collection<Tag> tagsToExclude) {
		
		TextSearchResponse response = new TextSearchResponse();
		
		try {
			int start = setStart(page, limit);
			limit = setLimit(limit, loadOneMore);
			Client client = ElasticSearchFactory.getClient();
			esIndexer.addMapping(client, ESIndexNames.INDEX_NODES, ESIndexTypes.TAGS);
			
			QueryBuilder qb = QueryBuilders
					.queryStringQuery(searchQuery.toLowerCase() + "*").useDisMax(true)
					.defaultOperator(QueryStringQueryBuilder.Operator.AND)
					.field("title");
			
			BoolQueryBuilder bQueryBuilder = QueryBuilders.boolQuery();
			bQueryBuilder.should(qb);
			
			if (tagsToExclude != null) {
				for (Tag tag :tagsToExclude) {
					if (tag != null) {
						bQueryBuilder.mustNot(termQuery("id", tag.getId()));
					}
				}
			}
			
			SearchResponse sResponse = client
					.prepareSearch(ESIndexNames.INDEX_NODES)
					.setTypes(ESIndexTypes.TAGS)
					.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
					.setQuery(bQueryBuilder).setFrom(start).setSize(limit)
					.addSort("title", SortOrder.ASC)
					.setExplain(true).execute().actionGet();
			
			
			if (sResponse != null) {
				response.setHitsNumber(sResponse.getHits().getTotalHits());
				
				for (SearchHit hit : sResponse.getHits()) {
					Long id = ((Integer) hit.getSource().get("id")).longValue();
					try {
						Tag annotation = defaultManager.loadResource(Tag.class, id);
						
						if (annotation != null) {
							response.addFoundNode(annotation);
						}
					} catch (ResourceCouldNotBeLoadedException e) {
						logger.error("Annotation was not found: " + id);
					}
				}
			}
		} catch (NoNodeAvailableException e1) {
			logger.error(e1);
		}
		return response;
	}
	
	@Override
	public TextSearchResponse1<StudentData> searchCredentialMembers (
			String searchTerm, InstructorAssignFilterValue filter, int page, int limit, long credId, 
			long instructorId, CredentialMembersSortOption sortOption) {
		TextSearchResponse1<StudentData> response = new TextSearchResponse1<>();
		try {
			int start = 0;
			start = setStart(page, limit);
		
			Client client = ElasticSearchFactory.getClient();
			esIndexer.addMapping(client, ESIndexNames.INDEX_USERS, ESIndexTypes.USER);
			
			BoolQueryBuilder bQueryBuilder = QueryBuilders.boolQuery();
			if(searchTerm != null && !searchTerm.isEmpty()) {
				QueryBuilder qb = QueryBuilders
						.queryStringQuery(searchTerm.toLowerCase() + "*").useDisMax(true)
						.defaultOperator(QueryStringQueryBuilder.Operator.AND)
						.field("name").field("lastname");
				
				bQueryBuilder.must(qb);
			}
			
			//bQueryBuilder.minimumNumberShouldMatch(1);
			
			//using filter instead
//			BoolQueryBuilder nestedBQBuilder = QueryBuilders.boolQuery();
//			nestedBQBuilder.must(termQuery("credentials.id", credId));
//			if(instructorId != -1) {
//				nestedBQBuilder.must(termQuery("credentials.instructorId", instructorId));
//			}
//			QueryBuilder nestedQB = QueryBuilders.nestedQuery(
//	        "credentials", nestedBQBuilder).innerHit(new QueryInnerHitBuilder());
//			bQueryBuilder.must(nestedQB);
			
			BoolQueryBuilder nestedFB = QueryBuilders.boolQuery();
			nestedFB.must(QueryBuilders.termQuery("credentials.id", credId));
			if(instructorId != -1) {
				nestedFB.must(QueryBuilders.termQuery("credentials.instructorId", instructorId));
			}
			NestedQueryBuilder nestedFilter1 = QueryBuilders.nestedQuery("credentials",
					nestedFB);
//					.innerHit(new QueryInnerHitBuilder());
			FilteredQueryBuilder filteredQueryBuilder = QueryBuilders.filteredQuery(bQueryBuilder, 
					nestedFilter1);
			//bQueryBuilder.must(termQuery("credentials.id", credId));
			
			try {
				String[] includes = {"id", "name", "lastname", "avatar", "position"};
				SearchRequestBuilder searchRequestBuilder = client.prepareSearch(ESIndexNames.INDEX_USERS)
						.setTypes(ESIndexTypes.USER)
						.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
						.setQuery(filteredQueryBuilder)
						.addAggregation(AggregationBuilders.nested("nestedAgg").path("credentials")
								.subAggregation(AggregationBuilders.filter("filtered")
										.filter(QueryBuilders.termQuery("credentials.id", credId))
								.subAggregation(
										AggregationBuilders.terms("unassigned")
										.field("credentials.instructorId")
										.include(new long[] {0}))))
						.setFetchSource(includes, null);
				
				/*
				 * set instructor assign filter as a post filter so it does not influence
				 * aggregation results
				 */
				if(filter == InstructorAssignFilterValue.Unassigned) {
					BoolQueryBuilder assignFilter = QueryBuilders.boolQuery();
					assignFilter.must(QueryBuilders.termQuery("credentials.instructorId", 0));
					/*
					 * need to add this condition again or post filter will be applied on other 
					 * credentials for users matched by query
					 */
					assignFilter.must(QueryBuilders.termQuery("credentials.id", credId));
					NestedQueryBuilder nestedFilter = QueryBuilders.nestedQuery("credentials",
							assignFilter).innerHit(new QueryInnerHitBuilder());
					searchRequestBuilder.setPostFilter(nestedFilter);
//					QueryBuilder qBuilder = termQuery("credentials.instructorId", 0);
//					nestedBQBuilder.must(qBuilder);
				} else {
					nestedFilter1.innerHit(new QueryInnerHitBuilder());
				}
				
				searchRequestBuilder.setFrom(start).setSize(limit);	
				
				//add sorting
				for(String field : sortOption.getSortFields()) {
					SortOrder sortOrder = sortOption.getSortOrder() == 
							org.prosolo.services.util.SortingOption.ASC ? 
							SortOrder.ASC : SortOrder.DESC;
					searchRequestBuilder.addSort(field, sortOrder);
				}
				//System.out.println(searchRequestBuilder.toString());
				SearchResponse sResponse = searchRequestBuilder.execute().actionGet();
				
				if(sResponse != null) {
					SearchHits searchHits = sResponse.getHits();
					response.setHitsNumber(searchHits.getTotalHits());
					
					if(searchHits != null) {
						for(SearchHit sh : searchHits) {
							StudentData student = new StudentData();
							Map<String, Object> fields = sh.getSource();
							User user = new User();
							user.setId(Long.parseLong(fields.get("id") + ""));
							user.setName((String) fields.get("name"));
							user.setLastname((String) fields.get("lastname"));
							user.setAvatarUrl((String) fields.get("avatar"));
							user.setPosition((String) fields.get("position"));
							UserData userData = new UserData(user);
							student.setUser(userData);
							
							SearchHits innerHits = sh.getInnerHits().get("credentials");
							long totalInnerHits = innerHits.getTotalHits();
							if(totalInnerHits == 1) {
								Map<String, Object> credential = innerHits.getAt(0).getSource();
								
								if(credential != null) {
									long instrId = Long.parseLong(credential.get("instructorId").toString());
									InstructorData instructor = null;
									if(instrId != 0) {
										try {
											instructor = credInstructorManager.getCredentialInstructor(
													instrId, credId, false, false);
										} catch(Exception e) {
											e.printStackTrace();
											logger.error(e);
										}
									}
									student.setInstructor(instructor);
									student.setCredProgress(Integer.parseInt(
											credential.get("progress").toString()));
//									@SuppressWarnings("unchecked")
//									Map<String, Object> profile = (Map<String, Object>) course.get("profile");
//								    if(profile != null && !profile.isEmpty()) {
//								    	resMap.put("profileType", profile.get("profileType"));
//								    	resMap.put("profileTitle", profile.get("profileTitle"));
//								    }
									response.addFoundNode(student);
								}
							}				
						}
						
						//get number of unassigned students
						Nested nestedAgg = sResponse.getAggregations().get("nestedAgg");
						Filter filtered = nestedAgg.getAggregations().get("filtered");
						Terms terms = filtered.getAggregations().get("unassigned");
						//Terms terms = nestedAgg.getAggregations().get("unassigned");
						Iterator<Terms.Bucket> it = terms.getBuckets().iterator();
						long unassignedNo = 0;
						if(it.hasNext()) {
							unassignedNo = it.next().getDocCount();
						}
						
						InstructorAssignFilter[] filters = new InstructorAssignFilter[2];
						filters[0] = new InstructorAssignFilter(InstructorAssignFilterValue.All, 
								filtered.getDocCount());
								//nestedAgg.getDocCount());
						filters[1] = new InstructorAssignFilter(InstructorAssignFilterValue.Unassigned, 
								unassignedNo);
						Map<String, Object> additionalInfo = new HashMap<>();
						additionalInfo.put("filters", filters);
						additionalInfo.put("selectedFilter", filters[0].getFilter() == filter
								? filters[0] : filters[1]);
						response.setAdditionalInfo(additionalInfo);
						
						return response;
					}
				}
			} catch (SearchPhaseExecutionException spee) {
				spee.printStackTrace();
				logger.error(spee);
			}
	
		} catch (NoNodeAvailableException e1) {
			logger.error(e1);
		}
		return null;
	}
	
	/*
	 * if pagination is not wanted and all results should be returned
	 * -1 should be passed as a page parameter 
	 */
	@Override
	public TextSearchResponse1<InstructorData> searchInstructors (
			String searchTerm, int page, int limit, long credId, 
			InstructorSortOption sortOption, List<Long> excludedIds) {
		TextSearchResponse1<InstructorData> response = new TextSearchResponse1<>();
		try {
			Client client = ElasticSearchFactory.getClient();
			esIndexer.addMapping(client, ESIndexNames.INDEX_USERS, ESIndexTypes.USER);
			
			BoolQueryBuilder bQueryBuilder = QueryBuilders.boolQuery();
			
			if(searchTerm != null && !searchTerm.isEmpty()) {
				QueryBuilder qb = QueryBuilders
						.queryStringQuery(searchTerm.toLowerCase() + "*").useDisMax(true)
						.defaultOperator(QueryStringQueryBuilder.Operator.AND)
						.field("name").field("lastname");
				bQueryBuilder.must(qb);
			}

			//bQueryBuilder.minimumNumberShouldMatch(1);
			
			bQueryBuilder.must(termQuery("credentialsWithInstructorRole.id", credId));

			if (excludedIds != null) {
				for (long id : excludedIds) {
					bQueryBuilder.mustNot(QueryBuilders.matchQuery("id", id));
				}
			}
			try {
				String[] includes = {"id"};
				SearchRequestBuilder searchRequestBuilder = client.prepareSearch(ESIndexNames.INDEX_USERS)
						.setTypes(ESIndexTypes.USER)
						.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
						.setQuery(bQueryBuilder)
						//.setFrom(start).setSize(limit)
						.setFetchSource(includes, null);
				
				int start = 0;
				int size = 1000;
				if(page != -1) {
					start = setStart(page, limit);
					size = limit;
				}
				searchRequestBuilder.setFrom(start).setSize(size);

				//add sorting
				for(String field : sortOption.getSortFields()) {
					SortOrder sortOrder = sortOption.getSortOrder() == 
							org.prosolo.services.util.SortingOption.ASC ? 
							SortOrder.ASC : SortOrder.DESC;
					searchRequestBuilder.addSort(field, sortOrder);
				}
			
				//System.out.println(searchRequestBuilder.toString());
				SearchResponse sResponse = searchRequestBuilder.execute().actionGet();
				if(sResponse != null) {
					SearchHits searchHits = sResponse.getHits();
					
					response.setHitsNumber(searchHits.getTotalHits());
					
					if(searchHits != null) {
						for(SearchHit sh : searchHits) {
							Map<String, Object> fields = sh.getSource();
							long id = Long.parseLong(fields.get("id") + "");
							InstructorData instructor = credInstructorManager.getCredentialInstructor(
									id, credId, true, true);
							if(instructor != null) {
								response.addFoundNode(instructor);
							}
						}
					}
				}
			} catch (SearchPhaseExecutionException spee) {
				spee.printStackTrace();
				logger.error(spee);
			}
	
		} catch (NoNodeAvailableException e1) {
			logger.error(e1);
		} catch(DbConnectionException dbce) {
			logger.error(dbce);
		}
		return response;
	}
	
	@Override
	public Map<String, Object> searchUnassignedCourseMembers (
			String searchTerm, long courseId) {
		
		Map<String, Object> resultMap = new HashMap<>();
		try {
			Client client = ElasticSearchFactory.getClient();
			esIndexer.addMapping(client, ESIndexNames.INDEX_USERS, ESIndexTypes.USER);
			
			QueryBuilder qb = QueryBuilders
					.queryStringQuery(searchTerm.toLowerCase() + "*").useDisMax(true)
					.defaultOperator(QueryStringQueryBuilder.Operator.AND)
					.field("name").field("lastname");
			
			BoolQueryBuilder bQueryBuilder = QueryBuilders.boolQuery();
			//bQueryBuilder.minimumNumberShouldMatch(1);
			
			QueryBuilder nestedQB = QueryBuilders.nestedQuery(
			        "courses",               
			        QueryBuilders.boolQuery()           
			                .must(QueryBuilders.matchQuery("courses.id", courseId))
							.must(QueryBuilders.matchQuery("courses.instructorId", 0)));		
			bQueryBuilder.must(qb);
			bQueryBuilder.must(nestedQB);

			try {
				String[] includes = {"id", "name", "lastname", "avatar"};
				SearchRequestBuilder searchRequestBuilder = client.prepareSearch(ESIndexNames.INDEX_USERS)
						.setTypes(ESIndexTypes.USER)
						.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
						.setQuery(bQueryBuilder)
						.setFetchSource(includes, null)
						.setFrom(0).setSize(1000);
				
				searchRequestBuilder.addSort("name", SortOrder.ASC);
				searchRequestBuilder.addSort("lastname", SortOrder.ASC);
				//System.out.println(searchRequestBuilder.toString());
				SearchResponse sResponse = searchRequestBuilder.execute().actionGet();
				if(sResponse != null) {
					SearchHits searchHits = sResponse.getHits();
					
					resultMap.put("resultNumber", searchHits.getTotalHits());
					
					List<Map<String, Object>> data = new LinkedList<>();
					
					if(searchHits != null) {
						for(SearchHit sh : searchHits) {
							Map<String, Object> resMap = new HashMap<>();
							Map<String, Object> fields = sh.getSource();
							
							resMap.put("id", Long.parseLong(fields.get("id") + ""));
				    		resMap.put("firstName", (String) fields.get("name"));
				    		resMap.put("lastName", (String) fields.get("lastname"));
				    		resMap.put("avatarUrl", (String) fields.get("avatar"));
					
							data.add(resMap);
						}
					}
					
					resultMap.put("data", data);
				
				}
			} catch (SearchPhaseExecutionException spee) {
				spee.printStackTrace();
				logger.error(spee);
			}
	
		} catch (NoNodeAvailableException e1) {
			logger.error(e1);
		}
		return resultMap;
	}
	
	@Override
	public TextSearchResponse1<UserData> searchUsersWithInstructorRole (String searchTerm, 
			long credId, long roleId, List<Long> excludedUserIds) {
		TextSearchResponse1<UserData> response = new TextSearchResponse1<>();
		try {
			Client client = ElasticSearchFactory.getClient();
			esIndexer.addMapping(client, ESIndexNames.INDEX_USERS, ESIndexTypes.USER);
			
			BoolQueryBuilder bQueryBuilder = QueryBuilders.boolQuery();
			
			if(searchTerm != null && !searchTerm.isEmpty()) {
				QueryBuilder qb = QueryBuilders
						.queryStringQuery(searchTerm.toLowerCase() + "*").useDisMax(true)
						.defaultOperator(QueryStringQueryBuilder.Operator.AND)
						.field("name").field("lastname");
				
				bQueryBuilder.must(qb);
			}
			
			for (Long id : excludedUserIds) {
				bQueryBuilder.mustNot(termQuery("id", id));
			}
			
			//bQueryBuilder.minimumNumberShouldMatch(1);
			
			bQueryBuilder.mustNot(termQuery("coursesWithInstructorRole.id", credId));
			bQueryBuilder.must(termQuery("roles.id", roleId));

			try {
				String[] includes = {"id", "name", "lastname", "avatar", "position"};
				SearchRequestBuilder searchRequestBuilder = client.prepareSearch(ESIndexNames.INDEX_USERS)
						.setTypes(ESIndexTypes.USER)
						.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
						.setQuery(bQueryBuilder)
						.setFetchSource(includes, null)
						.setFrom(0).setSize(1000);
				
				searchRequestBuilder.addSort("name", SortOrder.ASC);
				searchRequestBuilder.addSort("lastname", SortOrder.ASC);
				//System.out.println(searchRequestBuilder.toString());
				SearchResponse sResponse = searchRequestBuilder.execute().actionGet();
				if(sResponse != null) {
					SearchHits searchHits = sResponse.getHits();
					
					response.setHitsNumber(searchHits.getTotalHits());
					
					if(searchHits != null) {
						for(SearchHit sh : searchHits) {
							Map<String, Object> fields = sh.getSource();
							User user = new User();
							user.setId(Long.parseLong(fields.get("id") + ""));
				    		user.setName((String) fields.get("name"));
				    		user.setLastname((String) fields.get("lastname"));
				    		user.setAvatarUrl((String) fields.get("avatar"));
				    		user.setPosition((String) fields.get("position"));
					
							response.addFoundNode(new UserData(user));
						}
					}		
				}
			} catch (SearchPhaseExecutionException spee) {
				spee.printStackTrace();
				logger.error(spee);
			}
	
		} catch (NoNodeAvailableException e1) {
			logger.error(e1);
		}
		return response;
	}
		
	@Override
	public List<Long> getInstructorCourseIds (long userId) {
		try {
			Client client = ElasticSearchFactory.getClient();
			esIndexer.addMapping(client, ESIndexNames.INDEX_USERS, ESIndexTypes.USER);
			
			BoolQueryBuilder bQueryBuilder = QueryBuilders.boolQuery();
	
			
//			QueryBuilder nestedQB = QueryBuilders.nestedQuery(
//			        "courses", termQuery("courses.id", 1)).innerHit(new QueryInnerHitBuilder().setFetchSource(new String[] {"id"}, null));
//			bQueryBuilder.must(nestedQB);
			
			bQueryBuilder.must(termQuery("id", userId));

			
			String[] includes = {"coursesWithInstructorRole.id"};
			SearchRequestBuilder searchRequestBuilder = client.prepareSearch(ESIndexNames.INDEX_USERS)
					.setTypes(ESIndexTypes.USER)
					.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
					.setQuery(bQueryBuilder)
					.setFetchSource(includes, null)
					.setFrom(0).setSize(1000);
			
			SearchResponse sResponse = searchRequestBuilder.execute().actionGet();
			if(sResponse != null) {
				SearchHits searchHits = sResponse.getHits();
				long numberOfResults = searchHits.getTotalHits();
				
				if(searchHits != null && numberOfResults == 1) {
					List<Long> ids = new ArrayList<>();
					SearchHit hit = searchHits.getAt(0);
					Map<String, Object> source = hit.getSource();
					@SuppressWarnings("unchecked")
					List<Map<String, Object>> courses =  (List<Map<String, Object>>) source.get("coursesWithInstructorRole");	
					for(Map<String, Object> courseMap : courses) {
						ids.add(Long.parseLong(courseMap.get("id") + ""));
					}
					return ids;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
		}
		return null;
	}
	
	@Override
	public TextSearchResponse1<CredentialData> searchCredentials(
			String searchTerm, int page, int limit, long userId, 
			CredentialSearchFilter filter, CredentialSortOption sortOption) {
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
				
				bQueryBuilder.must(qb);
			}
			
			//bQueryBuilder.minimumNumberShouldMatch(1);
			
			switch(filter) {
				case ALL:
					break;
				case BOOKMARKS:
					QueryBuilder qb = QueryBuilders.nestedQuery(
					        "bookmarkedBy",               
					        QueryBuilders.boolQuery()          
					             .must(termQuery("bookmarkedBy.id", userId))); 
					bQueryBuilder.must(qb);
					break;
				case FROM_CREATOR:
					bQueryBuilder.must(termQuery("creatorId", userId));
					break;
				case BY_OTHER_STUDENTS:
					bQueryBuilder.mustNot(termQuery("creatorId", userId));
					/*
					 * Because lowercased strings are always stored in index. Alternative
					 * is to use match query that would analyze term passed.
					 */
					bQueryBuilder.must(termQuery("type", 
							LearningResourceType.USER_CREATED.toString().toLowerCase()));
					break;
				case UNIVERSITY:
					bQueryBuilder.must(termQuery("type", 
							LearningResourceType.UNIVERSITY_CREATED.toString().toLowerCase()));
					break;
				default:
					break;
			}
			
			/*
			 * this is how query should look like in pseudo code
			 */
//			(creator != {loggedUserId} AND (published = true or published = false and hasDraft = true)) 
//			OR (creator = {loggedUserId} AND ((isDraft = false AND published = false AND hasDraft = false) 
//			                                                           OR (isDraft = true)) 
//			                                                           OR published = true))
			
			BoolQueryBuilder boolFilter = QueryBuilders.boolQuery();
			
			/*
			 * include all published credentials and draft credentials that have draft version
			 * by other users
			 */
			BoolQueryBuilder publishedCredentialsByOthersFilter = QueryBuilders.boolQuery();
			publishedCredentialsByOthersFilter.mustNot(QueryBuilders.termQuery("creatorId", userId));
			BoolQueryBuilder publishedOrHasDraft = QueryBuilders.boolQuery();
			publishedOrHasDraft.should(QueryBuilders.termQuery("published", true));
			BoolQueryBuilder hasDraft = QueryBuilders.boolQuery();
			hasDraft.must(QueryBuilders.termQuery("published", false));
			hasDraft.must(QueryBuilders.termQuery("hasDraft", true));
			publishedOrHasDraft.should(hasDraft);
			publishedCredentialsByOthersFilter.must(publishedOrHasDraft);
			
			boolFilter.should(publishedCredentialsByOthersFilter);
			
			/*
			 * include all draft credentials created first time as draft (never been published),
			 * draft versions of credentials instead of original versions and published credentials
			 */
			BoolQueryBuilder currentUsersCredentials = QueryBuilders.boolQuery();
			currentUsersCredentials.must(QueryBuilders.termQuery("creatorId", userId));
			BoolQueryBuilder correctlySelectedPublishedAndDraftVersions = QueryBuilders.boolQuery();
			BoolQueryBuilder firstTimeDraft = QueryBuilders.boolQuery();
			firstTimeDraft.must(QueryBuilders.termQuery("isDraft", false));
			firstTimeDraft.must(QueryBuilders.termQuery("published", false));
			firstTimeDraft.must(QueryBuilders.termQuery("hasDraft", false));
			correctlySelectedPublishedAndDraftVersions.should(firstTimeDraft);
			correctlySelectedPublishedAndDraftVersions.should(QueryBuilders.termQuery("isDraft", true));
			correctlySelectedPublishedAndDraftVersions.should(QueryBuilders.termQuery("published", true));
			currentUsersCredentials.must(correctlySelectedPublishedAndDraftVersions);
			
			boolFilter.should(currentUsersCredentials);
			
			FilteredQueryBuilder filteredQueryBuilder = QueryBuilders.filteredQuery(bQueryBuilder, 
					boolFilter);
			
			//System.out.println("QUERY: " + filteredQueryBuilder.toString());
			
			String[] includes = {"id", "originalVersionId", "title", "description"};
			SearchRequestBuilder searchRequestBuilder = client.prepareSearch(ESIndexNames.INDEX_NODES)
					.setTypes(ESIndexTypes.CREDENTIAL)
					.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
					.setQuery(filteredQueryBuilder)
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
						Long originalCredId = Long.parseLong(hit.getSource()
								.get("originalVersionId").toString());
						try {
							CredentialData cd = null;
							long credId = 0;
							if(originalCredId != null && originalCredId != 0) {
								credId = originalCredId;
							} else {
								credId = id;
							}
							cd = credentialManager
									.getCredentialDataWithProgressIfExists(credId, userId);
							
							if(cd != null) {
								/*
								 * if credential is created by some other user, this user should not 
								 * be aware that credential is draft
								 */
							    if(cd.getCreator().getId() != userId) {
							    	cd.setPublished(true);
							    }
							    
							    /*
							     * if draft version, set title and description from draft version
							     */
							    if(originalCredId > 0) {
							    	cd.setTitle(hit.getSource().get("title").toString());
							    	cd.setDescription(hit.getSource().get("description").toString());
							    }
							    
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
			CredentialSearchFilter filter, CredentialSortOption sortOption) {
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
				
				bQueryBuilder.must(qb);
			}
			
			//bQueryBuilder.minimumNumberShouldMatch(1);
			
			switch(filter) {
				case ALL:
					break;
				case BOOKMARKS:
					QueryBuilder qb = QueryBuilders.nestedQuery(
					        "bookmarkedBy",               
					        QueryBuilders.boolQuery()          
					             .must(termQuery("bookmarkedBy.id", userId))); 
					bQueryBuilder.must(qb);
					break;
				case FROM_CREATOR:
					bQueryBuilder.must(termQuery("creatorId", userId));
					break;
				case BY_STUDENTS:
					//bQueryBuilder.mustNot(termQuery("creatorId", userId));
					/*
					 * Because lowercased strings are always stored in index. Alternative
					 * is to use match query that would analyze term passed.
					 */
					bQueryBuilder.must(termQuery("type", 
							LearningResourceType.USER_CREATED.toString().toLowerCase()));
					break;
				case UNIVERSITY:
					bQueryBuilder.must(termQuery("type", 
							LearningResourceType.UNIVERSITY_CREATED.toString().toLowerCase()));
					break;
				case YOUR_CREDENTIALS:
					bQueryBuilder.must(termQuery("instructors.id", userId));
				default:
					break;
			}
			
			/*
			 * this is how query should look like in pseudo code
			 */
//			(type == {USER} AND (published = true or published = false and hasDraft = true)) 
//			OR (type = {UNIVERSITY} AND ((isDraft = false AND published = false AND hasDraft = false) 
//			                                                           OR (isDraft = true)) 
//			                                                           OR published = true))
			
			BoolQueryBuilder boolFilter = QueryBuilders.boolQuery();
			
			/*
			 * include all published credentials and draft credentials that have draft version
			 * created by users
			 */
			BoolQueryBuilder publishedCredentialsByStudentsFilter = QueryBuilders.boolQuery();
			publishedCredentialsByStudentsFilter.must(QueryBuilders.termQuery(
					"type", LearningResourceType.USER_CREATED.toString().toLowerCase()));
			BoolQueryBuilder publishedOrHasDraft = QueryBuilders.boolQuery();
			publishedOrHasDraft.should(QueryBuilders.termQuery("published", true));
			BoolQueryBuilder hasDraft = QueryBuilders.boolQuery();
			hasDraft.must(QueryBuilders.termQuery("published", false));
			hasDraft.must(QueryBuilders.termQuery("hasDraft", true));
			publishedOrHasDraft.should(hasDraft);
			publishedCredentialsByStudentsFilter.must(publishedOrHasDraft);
			
			boolFilter.should(publishedCredentialsByStudentsFilter);
			
			/*
			 * include all draft credentials created first time as draft (never been published),
			 * draft versions of credentials instead of original versions and published credentials
			 * created by university
			 */
			BoolQueryBuilder currentUsersCredentials = QueryBuilders.boolQuery();
			currentUsersCredentials.must(QueryBuilders.termQuery(
					"type", LearningResourceType.UNIVERSITY_CREATED.toString().toLowerCase()));
			BoolQueryBuilder correctlySelectedPublishedAndDraftVersions = QueryBuilders.boolQuery();
			BoolQueryBuilder firstTimeDraft = QueryBuilders.boolQuery();
			firstTimeDraft.must(QueryBuilders.termQuery("isDraft", false));
			firstTimeDraft.must(QueryBuilders.termQuery("published", false));
			firstTimeDraft.must(QueryBuilders.termQuery("hasDraft", false));
			correctlySelectedPublishedAndDraftVersions.should(firstTimeDraft);
			correctlySelectedPublishedAndDraftVersions.should(QueryBuilders.termQuery("isDraft", true));
			correctlySelectedPublishedAndDraftVersions.should(QueryBuilders.termQuery("published", true));
			currentUsersCredentials.must(correctlySelectedPublishedAndDraftVersions);
			
			boolFilter.should(currentUsersCredentials);
			
			QueryBuilder filteredQueryBuilder = QueryBuilders.filteredQuery(bQueryBuilder, 
					boolFilter);
			
			//System.out.println("QUERY: " + filteredQueryBuilder.toString());
			
			String[] includes = {"id", "originalVersionId", "title", "description"};
			SearchRequestBuilder searchRequestBuilder = client.prepareSearch(ESIndexNames.INDEX_NODES)
					.setTypes(ESIndexTypes.CREDENTIAL)
					.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
					.setQuery(filteredQueryBuilder)
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
						Long originalCredId = Long.parseLong(hit.getSource()
								.get("originalVersionId").toString());
						try {
							CredentialData cd = null;
							long credId = 0;
							if(originalCredId != null && originalCredId != 0) {
								credId = originalCredId;
							} else {
								credId = id;
							}
							cd = credentialManager
									.getBasicCredentialData(credId, userId);
							
							if(cd != null) {
								/*
								 * if credential is user created manager should not 
								 * be aware that credential is draft
								 */
								if(cd.getType() == LearningResourceType.USER_CREATED) {
								    cd.setPublished(true);
								}
								/*
							     * if credential draft version, set title and description from draft version
							     */
							    if(originalCredId > 0) {
							    	cd.setTitle(hit.getSource().get("title").toString());
							    	cd.setDescription(hit.getSource().get("description").toString());
							    }
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
	public TextSearchResponse1<StudentData> searchUnassignedAndStudentsAssignedToInstructor(
			String searchTerm, long credId, long instructorId, InstructorAssignFilterValue filter,
			int page, int limit) {
		TextSearchResponse1<StudentData> response = new TextSearchResponse1<>();
		try {
			int start = 0;
			start = setStart(page, limit);
			
			Client client = ElasticSearchFactory.getClient();
			esIndexer.addMapping(client, ESIndexNames.INDEX_USERS, ESIndexTypes.USER);
			
			BoolQueryBuilder bQueryBuilder = QueryBuilders.boolQuery();
			
			if(searchTerm != null && !searchTerm.isEmpty()) {
				QueryBuilder qb = QueryBuilders
						.queryStringQuery(searchTerm.toLowerCase() + "*").useDisMax(true)
						.defaultOperator(QueryStringQueryBuilder.Operator.AND)
						.field("name").field("lastname");
				
				bQueryBuilder.must(qb);
			}
				
			//bQueryBuilder.minimumNumberShouldMatch(1);
			
			BoolQueryBuilder credFilter = QueryBuilders.boolQuery();
			credFilter.must(QueryBuilders.termQuery("credentials.id", credId));
			/*
			 * unassigned or assigned to instructor specified by instructorId
			 */
			BoolQueryBuilder unassignedOrWithSpecifiedInstructorFilter = QueryBuilders.boolQuery();
			unassignedOrWithSpecifiedInstructorFilter.should(QueryBuilders.termQuery(
					"credentials.instructorId", 0));
			unassignedOrWithSpecifiedInstructorFilter.should(QueryBuilders.termQuery(
					"credentials.instructorId", instructorId));
			credFilter.must(unassignedOrWithSpecifiedInstructorFilter);
			NestedQueryBuilder nestedCredFilter = QueryBuilders.nestedQuery("credentials",
					credFilter);
			
			QueryBuilder filteredQueryBuilder = QueryBuilders.filteredQuery(bQueryBuilder,
					nestedCredFilter);

			try {
				String[] includes = {"id", "name", "lastname", "avatar", "position"};
				SearchRequestBuilder searchRequestBuilder = client.prepareSearch(ESIndexNames.INDEX_USERS)
						.setTypes(ESIndexTypes.USER)
						.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
						.setQuery(filteredQueryBuilder)
						.addAggregation(AggregationBuilders.nested("nestedAgg").path("credentials")
								.subAggregation(
										AggregationBuilders.filter("filtered")
										.filter(credFilter)
										.subAggregation(
												AggregationBuilders.terms("unassigned")
												.field("credentials.instructorId")
												.include(new long[] {0}))))
						.setFetchSource(includes, null)
						.setFrom(0).setSize(1000)
						.setFrom(start).setSize(limit);
				
				/*
				 * set instructor assign filter as a post filter so it does not influence
				 * aggregation results
				 */
				BoolQueryBuilder filterBuilder = null;
				switch(filter) {
					case All:
						nestedCredFilter.innerHit(new QueryInnerHitBuilder());
						break;
					case Assigned:
						filterBuilder = QueryBuilders.boolQuery();
						filterBuilder.must(QueryBuilders.termQuery("credentials.id", credId));
						filterBuilder.must(QueryBuilders.termQuery("credentials.instructorId",
								instructorId));
						break;
					case Unassigned:
						filterBuilder = QueryBuilders.boolQuery();
						filterBuilder.must(QueryBuilders.termQuery("credentials.id", credId));
						filterBuilder.must(QueryBuilders.termQuery("credentials.instructorId", 0));
						break;
				}
				if(filterBuilder != null) {
					NestedQueryBuilder nestedFilter = QueryBuilders.nestedQuery("credentials",
							filterBuilder).innerHit(new QueryInnerHitBuilder());
					searchRequestBuilder.setPostFilter(nestedFilter);
				}
				searchRequestBuilder.addSort("credentials.instructorId", SortOrder.DESC);
				searchRequestBuilder.addSort("name", SortOrder.ASC);
				searchRequestBuilder.addSort("lastname", SortOrder.ASC);
				//System.out.println(searchRequestBuilder.toString());
				SearchResponse sResponse = searchRequestBuilder.execute().actionGet();
				if(sResponse != null) {
					SearchHits searchHits = sResponse.getHits();
					
					response.setHitsNumber(searchHits.getTotalHits());
					
					if(searchHits != null) {
						for(SearchHit sh : searchHits) {
							Map<String, Object> fields = sh.getSource();
							StudentData student = new StudentData();
							User user = new User();
							user.setId(Long.parseLong(fields.get("id") + ""));
				    		user.setName((String) fields.get("name"));
				    		user.setLastname((String) fields.get("lastname"));
				    		user.setAvatarUrl((String) fields.get("avatar"));
				    		user.setPosition((String) fields.get("position"));
				    		student.setUser(new UserData(user));
				    		
				    		SearchHits innerHits = sh.getInnerHits().get("credentials");
							long totalInnerHits = innerHits.getTotalHits();
							if(totalInnerHits == 1) {
								Map<String, Object> credential = innerHits.getAt(0).getSource();
								student.setCredProgress(Integer.parseInt(
										credential.get("progress") + ""));
								long instId = Long.parseLong(credential.get("instructorId") + "");
								if(instId != 0) {
									InstructorData id = new InstructorData(false);
									UserData ud = new UserData();
									ud.setId(instId);
									id.setUser(ud);
									student.setInstructor(id);
									student.setAssigned(true);
								}
							}
							response.addFoundNode(student);
						}
					}
					
					//get number of unassigned students
					Nested nestedAgg = sResponse.getAggregations().get("nestedAgg");
					Filter filtered = nestedAgg.getAggregations().get("filtered");
					Terms terms = filtered.getAggregations().get("unassigned");
					Iterator<Terms.Bucket> it = terms.getBuckets().iterator();
					long unassignedNo = 0;
					if(it.hasNext()) {
						unassignedNo = it.next().getDocCount();
					}
					long allNo = filtered.getDocCount();
					InstructorAssignFilter[] filters = new InstructorAssignFilter[3];
					filters[0] = new InstructorAssignFilter(InstructorAssignFilterValue.All, 
							allNo);
					filters[1] = new InstructorAssignFilter(InstructorAssignFilterValue.Assigned, 
							allNo - unassignedNo);
					filters[2] = new InstructorAssignFilter(InstructorAssignFilterValue.Unassigned, 
							unassignedNo);
					InstructorAssignFilter selectedFilter = null;
					for(InstructorAssignFilter f : filters) {
						if(f.getFilter() == filter) {
							selectedFilter = f;
							break;
						}
					}
					Map<String, Object> additionalInfo = new HashMap<>();
					additionalInfo.put("filters", filters);
					additionalInfo.put("selectedFilter", selectedFilter);
					response.setAdditionalInfo(additionalInfo);
				}
			} catch (SearchPhaseExecutionException spee) {
				spee.printStackTrace();
				logger.error(spee);
			}
	
		} catch (NoNodeAvailableException e1) {
			logger.error(e1);
		}
		return response;
	}
}
