package org.prosolo.search.impl;

import static org.elasticsearch.index.query.QueryBuilders.termQuery;

import java.util.Collection;
import java.util.HashMap;
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
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.index.query.support.QueryInnerHitBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortOrder;
import org.prosolo.bigdata.common.enums.ESIndexTypes;
import org.prosolo.common.domainmodel.activities.Activity;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.competences.Competence;
import org.prosolo.common.domainmodel.course.Course;
import org.prosolo.common.domainmodel.course.CreatorType;
import org.prosolo.common.domainmodel.user.LearningGoal;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.reminders.Reminder;
import org.prosolo.common.domainmodel.user.reminders.ReminderStatus;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.search.TextSearch;
import org.prosolo.search.util.CourseMembersSortOption;
import org.prosolo.search.util.CourseMembersSortOptionTranslator;
import org.prosolo.search.util.ESSortOption;
import org.prosolo.search.util.ESSortOrderTranslator;
import org.prosolo.search.util.InstructorAssignedFilter;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.indexing.ESIndexNames;
import org.prosolo.services.indexing.ESIndexer;
import org.prosolo.services.indexing.ElasticSearchFactory;
import org.prosolo.services.lti.exceptions.DbConnectionException;
import org.prosolo.services.nodes.CourseManager;
import org.prosolo.services.nodes.DefaultManager;
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
	@Inject private CourseManager courseManager;

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
					.queryString(searchString.toLowerCase() + "*").useDisMax(true)
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
					.queryString(searchString.toLowerCase() + "*").useDisMax(true)
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
		
		TextSearchResponse response = new TextSearchResponse();
		
		try {
			int start = setStart(page, limit);
			limit = setLimit(limit, loadOneMore);
			
			Client client = ElasticSearchFactory.getClient();
			esIndexer.addMapping(client, ESIndexNames.INDEX_NODES, ESIndexTypes.COMPETENCE);
			
			QueryBuilder qb = QueryBuilders
					.queryString(searchString.toLowerCase() + "*").useDisMax(true)
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
							.queryString(tag.getTitle()).useDisMax(true)
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
					.queryString(searchString.toLowerCase() + "*").useDisMax(true)
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
						Activity activity = defaultManager.loadResource(Activity.class, id);
						
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
			Collection<Course> excludeCourses, boolean published, List<Tag> filterTags, 
			SortingOption sortTitleAsc, SortingOption sortDateAsc) {
		
		TextSearchResponse response = new TextSearchResponse();
		
		try {
			int start = setStart(page, limit);
			limit = setLimit(limit, loadOneMore);
			
			Client client = ElasticSearchFactory.getClient();
			esIndexer.addMapping(client, ESIndexNames.INDEX_NODES, ESIndexTypes.COURSE );
			
			QueryBuilder qb = QueryBuilders
					.queryString(searchQuery.toLowerCase() + "*").useDisMax(true)
					.defaultOperator(QueryStringQueryBuilder.Operator.AND)
					.field("description")
					.field("tags.title")
					.field("title");
			
			BoolQueryBuilder bQueryBuilder = QueryBuilders.boolQuery();
			bQueryBuilder.should(qb);
			
			if (filterTags != null) {
				for (Tag tag : filterTags) {
					QueryBuilder tagQB = QueryBuilders
							.queryString(tag.getTitle()).useDisMax(true)
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
					.queryString(searchQuery.toLowerCase() + "*").useDisMax(true)
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
	public Map<String, Object> searchCourseMembers (
			String searchTerm, InstructorAssignedFilter filter, int page, int limit, long courseId, 
			long instructorId, CourseMembersSortOption sortOption) {
		
		Map<String, Object> resultMap = new HashMap<>();
		try {
			int start = 0;
			start = setStart(page, limit);
		
			
			Client client = ElasticSearchFactory.getClient();
			esIndexer.addMapping(client, ESIndexNames.INDEX_USERS, ESIndexTypes.USER);
			
			QueryBuilder qb = QueryBuilders
					.queryStringQuery(searchTerm.toLowerCase() + "*").useDisMax(true)
					.defaultOperator(QueryStringQueryBuilder.Operator.AND)
					.field("name").field("lastname");
			
			BoolQueryBuilder bQueryBuilder = QueryBuilders.boolQuery();
			//bQueryBuilder.minimumNumberShouldMatch(1);
			
			BoolQueryBuilder nestedBQBuilder = QueryBuilders.boolQuery();
			nestedBQBuilder.must(QueryBuilders.matchQuery("courses.id", courseId));
			if(instructorId != -1) {
				nestedBQBuilder.must(QueryBuilders.matchQuery("courses.instructorId", instructorId));
			}
			if(filter != InstructorAssignedFilter.All) {
				QueryBuilder qBuilder = termQuery("courses.instructorId", 0);
				if(filter == InstructorAssignedFilter.Assigned) {
					nestedBQBuilder.mustNot(qBuilder);
				} else {
					nestedBQBuilder.must(qBuilder);
				}
			}
			QueryBuilder nestedQB = QueryBuilders.nestedQuery(
			        "courses", nestedBQBuilder).innerHit(new QueryInnerHitBuilder());

			bQueryBuilder.must(qb);
			bQueryBuilder.must(nestedQB);
			//bQueryBuilder.must(termQuery("courses.id", courseId));
			
			
			try {
				String[] includes = {"id", "name", "lastname", "avatar", "position"};
				SearchRequestBuilder searchRequestBuilder = client.prepareSearch(ESIndexNames.INDEX_USERS)
						.setTypes(ESIndexTypes.USER)
						.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
						.setQuery(bQueryBuilder)
						.setFetchSource(includes, null);
				
				
				searchRequestBuilder.setFrom(start).setSize(limit);
				
				
				//add sorting
				ESSortOption esSortOption = CourseMembersSortOptionTranslator.getSortOption(sortOption);
				SortOrder sortOrder = esSortOption.getSortOrder();
				List<String> sortFields = esSortOption.getFields();
				for(String field : sortFields) {
					searchRequestBuilder.addSort(field, sortOrder);
				}
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
							User user = new User();
							user.setId(Long.parseLong(fields.get("id") + ""));
							user.setName((String) fields.get("name"));
							user.setLastname((String) fields.get("lastname"));
							user.setAvatarUrl((String) fields.get("avatar"));
							user.setPosition((String) fields.get("position"));
							
							resMap.put("user", user);
							
							SearchHits innerHits = sh.getInnerHits().get("courses");
							long totalInnerHits = innerHits.getTotalHits();
							if(totalInnerHits == 1) {
								Map<String, Object> course = innerHits.getAt(0).getSource();
								
								if(course != null) {
									long instrId = Long.parseLong(course.get("instructorId").toString());
									Map<String, Object> instructor = null;
									if(instrId != 0) {
										try {
											instructor = courseManager.getCourseInstructor(instrId, courseId);
										} catch(Exception e) {
											e.printStackTrace();
											logger.error(e);
										}
									}
									resMap.put("instructor", instructor);
									resMap.put("courseProgress", course.get("progress"));
									Map<String, Object> profile = (Map<String, Object>) course.get("profile");
								    if(profile != null && !profile.isEmpty()) {
								    	resMap.put("profileType", profile.get("profileType"));
								    	resMap.put("profileTitle", profile.get("profileTitle"));
								    }
								}
							}
							
												
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
	
	/*
	 * if pagination is not wanted and all results should be returned
	 * -1 should be passed as a page parameter 
	 */
	@Override
	public Map<String, Object> searchInstructors (
			String searchTerm, int page, int limit, long courseId, 
			SortingOption sortingOption, List<Long> excludedIds) {
		
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
			
			bQueryBuilder.must(termQuery("coursesWithInstructorRole.id", courseId));
			bQueryBuilder.must(qb);
			
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
				int size = Integer.MAX_VALUE;
				if(page != -1) {
					start = setStart(page, limit);
					size = limit;
				}
				searchRequestBuilder.setFrom(start).setSize(size);
				//add sorting
				SortOrder sortOrder = ESSortOrderTranslator.getSortOrder(sortingOption);
				
				searchRequestBuilder.addSort("name" , sortOrder);
				searchRequestBuilder.addSort("lastname" , sortOrder);
			
				//System.out.println(searchRequestBuilder.toString());
				SearchResponse sResponse = searchRequestBuilder.execute().actionGet();
				if(sResponse != null) {
					SearchHits searchHits = sResponse.getHits();
					
					resultMap.put("resultNumber", searchHits.getTotalHits());
					
					List<Map<String, Object>> data = new LinkedList<>();
					
					if(searchHits != null) {
						for(SearchHit sh : searchHits) {
							Map<String, Object> fields = sh.getSource();
							long id = Long.parseLong(fields.get("id") + "");
							Map<String, Object> instructorData = courseManager.getCourseInstructor(id, courseId);
							if(instructorData != null) {
								data.add(instructorData);
							}
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
		} catch(DbConnectionException dbce) {
			logger.error(dbce);
		}
		return resultMap;
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
						.setFrom(0).setSize(Integer.MAX_VALUE);
				
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
	public Map<String, Object> searchUsersWithInstructorRole (String searchTerm, long courseId, long roleId) {
		
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
				
			
			bQueryBuilder.must(qb);
			bQueryBuilder.mustNot(QueryBuilders.matchQuery("coursesWithInstructorRole.id", courseId));
			bQueryBuilder.must(QueryBuilders.matchQuery("roles.id", roleId));

			try {
				String[] includes = {"id", "name", "lastname", "avatar", "position"};
				SearchRequestBuilder searchRequestBuilder = client.prepareSearch(ESIndexNames.INDEX_USERS)
						.setTypes(ESIndexTypes.USER)
						.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
						.setQuery(bQueryBuilder)
						.setFetchSource(includes, null)
						.setFrom(0).setSize(Integer.MAX_VALUE);
				
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
				    		resMap.put("position", (String) fields.get("position"));
					
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
		
}
