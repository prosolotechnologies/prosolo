package org.prosolo.search.impl;

import static org.elasticsearch.index.query.QueryBuilders.termQuery;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
import org.elasticsearch.search.aggregations.bucket.terms.Terms.Bucket;
import org.elasticsearch.search.aggregations.metrics.valuecount.ValueCount;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.prosolo.bigdata.common.enums.ESIndexTypes;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.ESIndexNames;
import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.search.UserTextSearch;
import org.prosolo.search.util.competences.CompetenceStudentsSearchFilterValue;
import org.prosolo.search.util.competences.CompetenceStudentsSortOption;
import org.prosolo.search.util.credential.CredentialMembersSearchFilter;
import org.prosolo.search.util.credential.CredentialMembersSearchFilterValue;
import org.prosolo.search.util.credential.CredentialMembersSortOption;
import org.prosolo.search.util.credential.InstructorSortOption;
import org.prosolo.search.util.credential.LearningStatus;
import org.prosolo.search.util.credential.LearningStatusFilter;
import org.prosolo.search.util.roles.RoleFilter;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.indexing.ESIndexer;
import org.prosolo.services.indexing.ElasticSearchFactory;
import org.prosolo.services.interaction.FollowResourceManager;
import org.prosolo.services.nodes.AssessmentManager;
import org.prosolo.services.nodes.CredentialInstructorManager;
import org.prosolo.services.nodes.DefaultManager;
import org.prosolo.services.nodes.RoleManager;
import org.prosolo.services.nodes.UserGroupManager;
import org.prosolo.services.nodes.UserManager;
import org.prosolo.services.nodes.data.StudentData;
import org.prosolo.services.nodes.data.UserData;
import org.prosolo.services.nodes.data.UserSelectionData;
import org.prosolo.services.nodes.data.instructor.InstructorData;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * 
 * @author stefanvuckovic
 *
 */
@Service("org.prosolo.search.UserTextSearch")
public class UserTextSearchImpl extends AbstractManagerImpl implements UserTextSearch {

	private static final long serialVersionUID = -8472868946848154417L;

	private static Logger logger = Logger.getLogger(UserTextSearchImpl.class);
	
	@Inject private DefaultManager defaultManager;
	@Inject private ESIndexer esIndexer;
	@Inject private CredentialInstructorManager credInstructorManager;
	@Inject private RoleManager roleManager;
	@Inject private FollowResourceManager followResourceManager;
	@Inject private AssessmentManager assessmentManager;
	@Inject private UserManager userManager;
	@Inject private UserGroupManager userGroupManager;

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
						.addSort("lastname", SortOrder.ASC)
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
	public TextSearchResponse1<UserData> getUsersWithRoles(
			String term, int page, int limit, boolean paginate, long roleId, List<Role> adminRoles, boolean includeSystemUsers, List<Long> excludeIds) {

		TextSearchResponse1<UserData> response =
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
			bQueryBuilder.filter(qb);

			if (!includeSystemUsers) {
				bQueryBuilder.mustNot(termQuery("system", true));
			}

			if (excludeIds != null) {
				for (Long exUserId : excludeIds) {
					bQueryBuilder.mustNot(termQuery("id", exUserId));
				}
			}

			SearchResponse sResponse = null;

			String[] includes = {"id", "name", "lastname", "avatar", "roles", "position"};
			SearchRequestBuilder srb = client.prepareSearch(ESIndexNames.INDEX_USERS)
					.setTypes(ESIndexTypes.USER)
					.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
					.setQuery(bQueryBuilder)
					.setFrom(start).setSize(size)
					.addSort("lastname", SortOrder.ASC)
					.addSort("name", SortOrder.ASC)
					.addAggregation(AggregationBuilders.terms("roles")
							.field("roles.id"))
					.addAggregation(AggregationBuilders.count("docCount")
							.field("id"))
					.setFetchSource(includes, null);

			if(adminRoles != null && !adminRoles.isEmpty()){
				BoolQueryBuilder bqb1 = QueryBuilders.boolQuery();
				for(Role r : adminRoles){
					bqb1.should(termQuery("roles.id", r.getId()));
				}
				bQueryBuilder.filter(bqb1);
			}

			//set as a post filter so it does not influence aggregation results
			if(roleId > 0) {
				BoolQueryBuilder bqb = QueryBuilders.boolQuery().filter(termQuery("roles.id", roleId));
				srb.setPostFilter(bqb);
			}

			//System.out.println(srb.toString());
			sResponse = srb.execute().actionGet();

			if (sResponse != null) {
				response.setHitsNumber(sResponse.getHits().getTotalHits());
				List<org.prosolo.common.domainmodel.organization.Role> roles;

				if (adminRoles == null || adminRoles.isEmpty()){
					roles = roleManager.getAllRoles();
				} else {
					roles = adminRoles;
				}

				for(SearchHit sh : sResponse.getHits()) {
					Map<String, Object> fields = sh.getSource();
					User user = new User();
					user.setId(Long.parseLong(fields.get("id") + ""));
					user.setName((String) fields.get("name"));
					user.setLastname((String) fields.get("lastname"));
					user.setAvatarUrl((String) fields.get("avatar"));
					user.setPosition((String) fields.get("position"));
					user.setEmail(userManager.getUserEmail(user.getId()));
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
					UserData userData = new UserData(user, userRoles);

					response.addFoundNode(userData);
				}

				//get facets
				ValueCount docCount = sResponse.getAggregations().get("docCount");
				Terms terms = sResponse.getAggregations().get("roles");
				List<Terms.Bucket> buckets = terms.getBuckets();

				List<RoleFilter> roleFilters = new ArrayList<>();
				RoleFilter defaultFilter = new RoleFilter(0, "All", docCount.getValue());
				roleFilters.add(defaultFilter);
				RoleFilter selectedFilter = defaultFilter;
				for(org.prosolo.common.domainmodel.organization.Role role : roles) {
					Terms.Bucket bucket = getBucketForRoleId(role.getId(), buckets);
					int number = 0;
					if(bucket != null) {
						number = (int) bucket.getDocCount();
					}
					RoleFilter rf = new RoleFilter(role.getId(), role.getTitle(), number);
					roleFilters.add(rf);
					if(role.getId() == roleId) {
						selectedFilter = rf;
					}
				}

				Map<String, Object> additionalInfo = new HashMap<>();
				additionalInfo.put("filters", roleFilters);
				additionalInfo.put("selectedFilter", selectedFilter);

				response.setAdditionalInfo(additionalInfo);
			}
		} catch (Exception e1) {
			e1.printStackTrace();
			logger.error(e1);
		}
		return response;
	}
	
	private Bucket getBucketForRoleId(long id, List<Bucket> buckets) {
		if(buckets != null) {
			for(Bucket b : buckets) {
				if(Long.parseLong(b.getKey().toString()) == id) {
					return b;
				}
			}
		}
		return null;
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
	public TextSearchFilteredResponse<StudentData, CredentialMembersSearchFilterValue> searchCredentialMembers (
			String searchTerm, CredentialMembersSearchFilterValue filter, int page, int limit, long credId, 
			long instructorId, CredentialMembersSortOption sortOption) {
		TextSearchFilteredResponse<StudentData, CredentialMembersSearchFilterValue> response = 
				new TextSearchFilteredResponse<>();
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
			
			BoolQueryBuilder nestedFB = QueryBuilders.boolQuery();
			nestedFB.must(QueryBuilders.termQuery("credentials.id", credId));
			if(instructorId != -1) {
				nestedFB.must(QueryBuilders.termQuery("credentials.instructorId", instructorId));
			}
			NestedQueryBuilder nestedFilter1 = QueryBuilders.nestedQuery("credentials",
					nestedFB);
//					.innerHit(new QueryInnerHitBuilder());
//			FilteredQueryBuilder filteredQueryBuilder = QueryBuilders.filteredQuery(bQueryBuilder, 
//					nestedFilter1);
			bQueryBuilder.filter(nestedFilter1);
			//bQueryBuilder.must(termQuery("credentials.id", credId));
			
			try {
				String[] includes = {"id", "name", "lastname", "avatar", "position"};
				SearchRequestBuilder searchRequestBuilder = client.prepareSearch(ESIndexNames.INDEX_USERS)
						.setTypes(ESIndexTypes.USER)
						.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
						.setQuery(bQueryBuilder)
						.addAggregation(AggregationBuilders.nested("nestedAgg").path("credentials")
								.subAggregation(AggregationBuilders.filter("filtered")
										.filter(QueryBuilders.termQuery("credentials.id", credId))
								.subAggregation(
										AggregationBuilders.terms("unassigned")
										.field("credentials.instructorId")
										.include(new long[] {0}))
								.subAggregation(AggregationBuilders.filter("completed")
										.filter(QueryBuilders.termQuery("credentials.progress", 100)))))
								
						.setFetchSource(includes, null);
				
				/*
				 * set instructor assign filter as a post filter so it does not influence
				 * aggregation results
				 */
				if(filter == CredentialMembersSearchFilterValue.Unassigned) {
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
				} else if(filter == CredentialMembersSearchFilterValue.Completed) {
					BoolQueryBuilder completedF = QueryBuilders.boolQuery();
					completedF.must(QueryBuilders.termQuery("credentials.progress", 100));
					/*
					 * need to add this condition again or post filter will be applied on other 
					 * credentials for users matched by query
					 */
					completedF.must(QueryBuilders.termQuery("credentials.id", credId));
					NestedQueryBuilder nestedFilter = QueryBuilders.nestedQuery("credentials",
							completedF).innerHit(new QueryInnerHitBuilder());
					searchRequestBuilder.setPostFilter(nestedFilter);
			    } else {
					nestedFilter1.innerHit(new QueryInnerHitBuilder());
				}
				
				searchRequestBuilder.setFrom(start).setSize(limit);	
				
				//add sorting
				SortOrder sortOrder = sortOption.getSortOrder() == 
						org.prosolo.services.util.SortingOption.ASC ? 
						SortOrder.ASC : SortOrder.DESC;
				for(String field : sortOption.getSortFields()) {
					String nestedDoc = null;
					int dotIndex = field.indexOf(".");
					if(dotIndex != -1) {
						nestedDoc = field.substring(0, dotIndex);
					}
					if(nestedDoc != null) {
						BoolQueryBuilder credFilter = QueryBuilders.boolQuery();
						credFilter.must(QueryBuilders.termQuery(nestedDoc + ".id", credId));
						//searchRequestBuilder.addSort(field, sortOrder).setQuery(credFilter);
						FieldSortBuilder sortB = SortBuilders.fieldSort(field).order(sortOrder).setNestedPath(nestedDoc).setNestedFilter(credFilter);
						searchRequestBuilder.addSort(sortB);
					} else {
						searchRequestBuilder.addSort(field, sortOrder);
					}
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
									student.setProgress(Integer.parseInt(
											credential.get("progress").toString()));
									Optional<Long> credAssessmentId = assessmentManager
											.getDefaultCredentialAssessmentId(credId, user.getId());
									if(credAssessmentId.isPresent()) {
										student.setAssessmentId(credAssessmentId.get());
									}
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
						Filter completed = filtered.getAggregations().get("completed");
						//Terms terms = nestedAgg.getAggregations().get("unassigned");
						Iterator<Terms.Bucket> it = terms.getBuckets().iterator();
						long unassignedNo = 0;
						if(it.hasNext()) {
							unassignedNo = it.next().getDocCount();
						}
						
						long allStudentsNumber = filtered.getDocCount();
						
						response.putFilter(CredentialMembersSearchFilterValue.All, allStudentsNumber);
						response.putFilter(CredentialMembersSearchFilterValue.Unassigned, unassignedNo);
						response.putFilter(CredentialMembersSearchFilterValue.Assigned, allStudentsNumber - unassignedNo);
						response.putFilter(CredentialMembersSearchFilterValue.Completed, completed.getDocCount());

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
				
				searchRequestBuilder.addSort("lastname", SortOrder.ASC);
				searchRequestBuilder.addSort("name", SortOrder.ASC);
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
	public TextSearchResponse1<StudentData> searchUnassignedAndStudentsAssignedToInstructor(
			String searchTerm, long credId, long instructorId, CredentialMembersSearchFilterValue filter,
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
			
			//we don't want to return user that is actually instructor because we can't assing student to himself
			bQueryBuilder.mustNot(termQuery("id", instructorId));
				
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
		
			BoolQueryBuilder qb = QueryBuilders.boolQuery();
			qb.must(bQueryBuilder);
			qb.filter(nestedCredFilter);

			try {
				String[] includes = {"id", "name", "lastname", "avatar", "position"};
				SearchRequestBuilder searchRequestBuilder = client.prepareSearch(ESIndexNames.INDEX_USERS)
						.setTypes(ESIndexTypes.USER)
						.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
						.setQuery(qb)
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
					default:
						break;
				}
				if(filterBuilder != null) {
					NestedQueryBuilder nestedFilter = QueryBuilders.nestedQuery("credentials",
							filterBuilder).innerHit(new QueryInnerHitBuilder());
					searchRequestBuilder.setPostFilter(nestedFilter);
				}
				searchRequestBuilder.addSort("credentials.instructorId", SortOrder.DESC);
				searchRequestBuilder.addSort("lastname", SortOrder.ASC);
				searchRequestBuilder.addSort("name", SortOrder.ASC);
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
								student.setProgress(Integer.parseInt(
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
					CredentialMembersSearchFilter[] filters = new CredentialMembersSearchFilter[3];
					filters[0] = new CredentialMembersSearchFilter(CredentialMembersSearchFilterValue.All, 
							allNo);
					filters[1] = new CredentialMembersSearchFilter(CredentialMembersSearchFilterValue.Assigned, 
							allNo - unassignedNo);
					filters[2] = new CredentialMembersSearchFilter(CredentialMembersSearchFilterValue.Unassigned, 
							unassignedNo);
					CredentialMembersSearchFilter selectedFilter = null;
					for(CredentialMembersSearchFilter f : filters) {
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
	
	@Override
	public TextSearchResponse1<StudentData> searchCredentialMembersWithLearningStatusFilter (
			String searchTerm, LearningStatus filter, int page, int limit, long credId, 
			long userId, CredentialMembersSortOption sortOption) {
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
			
			BoolQueryBuilder nestedFB = QueryBuilders.boolQuery();
			nestedFB.must(QueryBuilders.termQuery("credentials.id", credId));
			NestedQueryBuilder nestedFilter1 = QueryBuilders.nestedQuery("credentials",
					nestedFB);
//					.innerHit(new QueryInnerHitBuilder());
			//instead of deprecated filteredquery
			BoolQueryBuilder bqb = QueryBuilders.boolQuery();
			bqb.must(bQueryBuilder);
			bqb.filter(nestedFilter1);
//			FilteredQueryBuilder filteredQueryBuilder = QueryBuilders.filteredQuery(bQueryBuilder, 
//					nestedFilter1);
			//bQueryBuilder.must(termQuery("credentials.id", credId));
			
			try {
				String[] includes = {"id", "name", "lastname", "avatar", "position"};
				SearchRequestBuilder searchRequestBuilder = client.prepareSearch(ESIndexNames.INDEX_USERS)
						.setTypes(ESIndexTypes.USER)
						.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
						.setQuery(bqb)
						.addAggregation(AggregationBuilders.nested("nestedAgg").path("credentials")
								.subAggregation(AggregationBuilders.filter("filtered")
										.filter(QueryBuilders.termQuery("credentials.id", credId))
								.subAggregation(
										AggregationBuilders.terms("inactive")
										.field("credentials.progress")
										.include(new long[] {100}))))
						.setFetchSource(includes, null);
				
				/*
				 * set learning status filter as a post filter so it does not influence
				 * aggregation results
				 */
				if(filter == LearningStatus.Active) {
					BoolQueryBuilder assignFilter = QueryBuilders.boolQuery();
					assignFilter.mustNot(QueryBuilders.termQuery("credentials.progress", 100));
					/*
					 * need to add this condition again or post filter will be applied on other 
					 * credentials for users matched by query
					 */
					assignFilter.filter(QueryBuilders.termQuery("credentials.id", credId));
					NestedQueryBuilder nestedFilter = QueryBuilders.nestedQuery("credentials",
							assignFilter).innerHit(new QueryInnerHitBuilder());
					searchRequestBuilder.setPostFilter(nestedFilter);
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
							boolean followed = followResourceManager.isUserFollowingUser(
									userId, user.getId());
							userData.setFollowedByCurrentUser(followed);
							student.setUser(userData);
							
							SearchHits innerHits = sh.getInnerHits().get("credentials");
							long totalInnerHits = innerHits.getTotalHits();
							if(totalInnerHits == 1) {
								Map<String, Object> credential = innerHits.getAt(0).getSource();
								
								if(credential != null) {
									student.setProgress(Integer.parseInt(
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
						Terms terms = filtered.getAggregations().get("inactive");
						//Terms terms = nestedAgg.getAggregations().get("unassigned");
						Iterator<Terms.Bucket> it = terms.getBuckets().iterator();
						long inactive = 0;
						if(it.hasNext()) {
							inactive = it.next().getDocCount();
						}
						
						LearningStatusFilter[] filters = new LearningStatusFilter[2];
						filters[0] = new LearningStatusFilter(LearningStatus.All,
								filtered.getDocCount());
								//nestedAgg.getDocCount());
						filters[1] = new LearningStatusFilter(LearningStatus.Active, 
								filtered.getDocCount() - inactive);
						Map<String, Object> additionalInfo = new HashMap<>();
						additionalInfo.put("filters", filters);
						additionalInfo.put("selectedFilter", filters[0].getStatus() == filter
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
	
	@Override
	public TextSearchResponse1<StudentData> searchUnenrolledUsersWithUserRole (
			String searchTerm, int page, int limit, long credId, long userRoleId) {
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
			bQueryBuilder.mustNot(termQuery("system", true));
			
			//bQueryBuilder.filter(termQuery("roles.id", userRoleId));
			
			BoolQueryBuilder nestedFB = QueryBuilders.boolQuery();
			nestedFB.must(QueryBuilders.termQuery("credentials.id", credId));
			NestedQueryBuilder nestedFilter1 = QueryBuilders.nestedQuery("credentials",
					nestedFB);

			bQueryBuilder.mustNot(nestedFilter1);
			
			//bQueryBuilder.must(termQuery("credentials.id", credId));
			
			try {
				String[] includes = {"id", "name", "lastname", "avatar", "position"};
				SearchRequestBuilder searchRequestBuilder = client.prepareSearch(ESIndexNames.INDEX_USERS)
						.setTypes(ESIndexTypes.USER)
						.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
						.setQuery(bQueryBuilder)
						.setFetchSource(includes, null);
				
				searchRequestBuilder.setFrom(start).setSize(limit);	
				
				//add sorting
				searchRequestBuilder.addSort("lastname", SortOrder.ASC);
				searchRequestBuilder.addSort("name", SortOrder.ASC);
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
							
							response.addFoundNode(student);			
						}
						
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
	
	@Override
	public TextSearchResponse1<UserData> searchPeopleUserFollows(
			String term, int page, int limit, long userId) {
		TextSearchResponse1<UserData> response = new TextSearchResponse1<>();
		
		try {
			int size = limit;
			int start = setStart(page, limit);
			
			Client client = ElasticSearchFactory.getClient();
			esIndexer.addMapping(client, ESIndexNames.INDEX_USERS, ESIndexTypes.USER);
			
			QueryBuilder qb = QueryBuilders
					.queryStringQuery(term.toLowerCase() + "*").useDisMax(true)
					.defaultOperator(QueryStringQueryBuilder.Operator.AND)
					.field("name").field("lastname");
			
			BoolQueryBuilder bQueryBuilder = QueryBuilders.boolQuery();
			bQueryBuilder.filter(qb);
			bQueryBuilder.filter(termQuery("followers.id", userId));
			
			SearchResponse sResponse = null;
			
			String[] includes = {"id", "name", "lastname", "avatar"};
			SearchRequestBuilder srb = client.prepareSearch(ESIndexNames.INDEX_USERS)
					.setTypes(ESIndexTypes.USER)
					.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
					.setQuery(bQueryBuilder)
					.setFrom(start).setSize(size)
					.addSort("lastname", SortOrder.ASC)
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
	public TextSearchResponse1<UserSelectionData> searchUsersInGroups(
			String searchTerm, int page, int limit, long groupId, boolean includeSystemUsers) {
		TextSearchResponse1<UserSelectionData> response = new TextSearchResponse1<>();
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
			
			if (!includeSystemUsers) {
				bQueryBuilder.mustNot(termQuery("system", true));
			}
			
			String[] includes = {"id", "name", "lastname", "avatar", "position"};
			SearchRequestBuilder searchRequestBuilder = client.prepareSearch(ESIndexNames.INDEX_USERS)
					.setTypes(ESIndexTypes.USER)
					.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
					.setQuery(bQueryBuilder)
					.setFetchSource(includes, null);
			
			searchRequestBuilder.setFrom(start).setSize(limit);	
			
			//add sorting
			searchRequestBuilder.addSort("lastname", SortOrder.ASC);
			searchRequestBuilder.addSort("name", SortOrder.ASC);
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
						UserData userData = new UserData(user);
						
						boolean isUserInGroup = userGroupManager.isUserInGroup(groupId, 
								userData.getId());
						UserSelectionData data = new UserSelectionData(userData, isUserInGroup);
						
						response.addFoundNode(data);			
					}
				}
			}
	
		} catch (Exception e1) {
			logger.error(e1);
		}
		return response;
	}
	
	@Override
	public TextSearchResponse1<UserData> searchPeersWithoutAssessmentRequest(
			String searchTerm, long limit, long credId, List<Long> peersToExcludeFromSearch) {
		TextSearchResponse1<UserData> response = new TextSearchResponse1<>();
		try {
			Client client = ElasticSearchFactory.getClient();
			esIndexer.addMapping(client, ESIndexNames.INDEX_USERS, ESIndexTypes.USER);
			
			BoolQueryBuilder bQueryBuilder = QueryBuilders.boolQuery();
			if (searchTerm != null && !searchTerm.isEmpty()) {
				QueryBuilder qb = QueryBuilders
						.queryStringQuery(searchTerm.toLowerCase() + "*").useDisMax(true)
						.defaultOperator(QueryStringQueryBuilder.Operator.AND)
						.field("name").field("lastname");
				
				bQueryBuilder.must(qb);
			}
			
			BoolQueryBuilder bqb = QueryBuilders.boolQuery()
					.must(bQueryBuilder)
					.filter(QueryBuilders.nestedQuery("credentials",
						QueryBuilders.boolQuery()
						.must(QueryBuilders.termQuery("credentials.id", credId))));
			
			if (peersToExcludeFromSearch != null) {
				for (Long exUserId : peersToExcludeFromSearch) {
					bqb.mustNot(termQuery("id", exUserId));
				}
			}
			
			try {
				String[] includes = {"id", "name", "lastname", "avatar"};
				SearchRequestBuilder searchRequestBuilder = client.prepareSearch(ESIndexNames.INDEX_USERS)
						.setTypes(ESIndexTypes.USER)
						.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
						.setQuery(bqb)
						.addSort("lastname", SortOrder.ASC)
						.addSort("name", SortOrder.ASC)
						.setFetchSource(includes, null)
						.setSize(3);	
				
				SearchResponse sResponse = searchRequestBuilder.execute().actionGet();
				
				if (sResponse != null) {
					SearchHits searchHits = sResponse.getHits();
					response.setHitsNumber(searchHits.getTotalHits());
					
					if (searchHits != null) {
						for (SearchHit sh : searchHits) {
							Map<String, Object> fields = sh.getSource();
							User user = new User();
							user.setId(Long.parseLong(fields.get("id") + ""));
							user.setName((String) fields.get("name"));
							user.setLastname((String) fields.get("lastname"));
							user.setAvatarUrl((String) fields.get("avatar"));
							user.setPosition((String) fields.get("position"));
							UserData userData = new UserData(user);
							
							response.addFoundNode(userData);
						}
						
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
	
	@Override
	public TextSearchFilteredResponse<StudentData, CompetenceStudentsSearchFilterValue> searchCompetenceStudents (
			String searchTerm, long compId, CompetenceStudentsSearchFilterValue filter, 
			CompetenceStudentsSortOption sortOption, int page, int limit) {
		TextSearchFilteredResponse<StudentData, CompetenceStudentsSearchFilterValue> response = new TextSearchFilteredResponse<>();
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
			
			BoolQueryBuilder nestedFB = QueryBuilders.boolQuery();
			nestedFB.must(QueryBuilders.termQuery("competences.id", compId));
			NestedQueryBuilder nestedFilter1 = QueryBuilders.nestedQuery("competences",
					nestedFB);

			bQueryBuilder.filter(nestedFilter1);
			
			BoolQueryBuilder studentsLearningCompAggrFilter = QueryBuilders.boolQuery();
			studentsLearningCompAggrFilter.filter(QueryBuilders.termQuery("competences.id", compId));
			BoolQueryBuilder studentsCompletedCompAggrFilter = QueryBuilders.boolQuery();
			studentsCompletedCompAggrFilter.filter(QueryBuilders.termQuery("competences.progress", 100));
			try {
				String[] includes = {"id", "name", "lastname", "avatar", "position"};
				SearchRequestBuilder searchRequestBuilder = client.prepareSearch(ESIndexNames.INDEX_USERS)
						.setTypes(ESIndexTypes.USER)
						.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
						.setQuery(bQueryBuilder)
						.addAggregation(AggregationBuilders.nested("nestedAgg").path("competences")
								.subAggregation(AggregationBuilders.filter("filtered")
										.filter(studentsLearningCompAggrFilter)
										.subAggregation(AggregationBuilders.filter("completed")
												.filter(studentsCompletedCompAggrFilter))))
						.setFetchSource(includes, null);
				
				/*
				 * set search filter as a post filter so it does not influence
				 * aggregation results
				 */
				if(filter == CompetenceStudentsSearchFilterValue.COMPLETED) {
					BoolQueryBuilder completedFilter = QueryBuilders.boolQuery();
					completedFilter.must(QueryBuilders.termQuery("competences.progress", 100));
					/*
					 * need to add this condition again or post filter will be applied on other 
					 * competences for users matched by query
					 */
					completedFilter.must(QueryBuilders.termQuery("competences.id", compId));
					NestedQueryBuilder nestedFilter = QueryBuilders.nestedQuery("competences",
							completedFilter).innerHit(new QueryInnerHitBuilder());
					searchRequestBuilder.setPostFilter(nestedFilter);
				} else if(filter == CompetenceStudentsSearchFilterValue.UNCOMPLETED) {
					BoolQueryBuilder uncompletedFilter = QueryBuilders.boolQuery();
					uncompletedFilter.mustNot(QueryBuilders.termQuery("competences.progress", 100));
					/*
					 * need to add this condition again or post filter will be applied on other 
					 * competences for users matched by query
					 */
					uncompletedFilter.must(QueryBuilders.termQuery("competences.id", compId));
					NestedQueryBuilder nestedFilter = QueryBuilders.nestedQuery("competences",
							uncompletedFilter).innerHit(new QueryInnerHitBuilder());
					searchRequestBuilder.setPostFilter(nestedFilter);
			    } else {
					nestedFilter1.innerHit(new QueryInnerHitBuilder());
				}
				
				searchRequestBuilder.setFrom(start).setSize(limit);	
				
				//add sorting
				SortOrder sortOrder = sortOption.getSortOrder() == 
						org.prosolo.services.util.SortingOption.ASC ? 
						SortOrder.ASC : SortOrder.DESC;
				for(String field : sortOption.getSortFields()) {
					String nestedDoc = null;
					int dotIndex = field.indexOf(".");
					if(dotIndex != -1) {
						nestedDoc = field.substring(0, dotIndex);
					}
					if(nestedDoc != null) {
						BoolQueryBuilder compFilter = QueryBuilders.boolQuery();
						compFilter.must(QueryBuilders.termQuery(nestedDoc + ".id", compId));
						//searchRequestBuilder.addSort(field, sortOrder).setQuery(credFilter);
						FieldSortBuilder sortB = SortBuilders.fieldSort(field).order(sortOrder).setNestedPath(
								nestedDoc).setNestedFilter(compFilter);
						searchRequestBuilder.addSort(sortB);
					} else {
						searchRequestBuilder.addSort(field, sortOrder);
					}
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
							
							SearchHits innerHits = sh.getInnerHits().get("competences");
							long totalInnerHits = innerHits.getTotalHits();
							if(totalInnerHits == 1) {
								Map<String, Object> competence = innerHits.getAt(0).getSource();
								
								if(competence != null) {
									student.setProgress(Integer.parseInt(
											competence.get("progress").toString()));
									String dateEnrolledString = (String) competence.get("dateEnrolled");
									DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
									if(dateEnrolledString != null && !dateEnrolledString.isEmpty()) {
										try {
											student.setDateEnrolled(df.parse(dateEnrolledString));
										} catch(Exception e) {
											logger.error(e);
										}
									}
									String dateCompletedString = (String) competence.get("dateCompleted");
									if(dateCompletedString != null && !dateCompletedString.isEmpty()) {
										try {
											student.setDateCompleted(df.parse(dateCompletedString));
										} catch(Exception e) {
											logger.error(e);
										}
									}
									response.addFoundNode(student);
								}
							}				
						}
						
						Nested nestedAgg = sResponse.getAggregations().get("nestedAgg");
						//filter with number of students learning competence
						Filter filtered  = nestedAgg.getAggregations().get("filtered");
						//filter with number of students completed learning competence
						Filter completed = filtered.getAggregations().get("completed");
						
						long allStudentsNumber = filtered.getDocCount();
						long completedNumber = completed.getDocCount();
						response.putFilter(CompetenceStudentsSearchFilterValue.ALL, allStudentsNumber);
						response.putFilter(CompetenceStudentsSearchFilterValue.COMPLETED, completedNumber);
						response.putFilter(CompetenceStudentsSearchFilterValue.UNCOMPLETED, 
								allStudentsNumber - completedNumber);
						
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

	@Override
	public TextSearchResponse1<UserData> searchNewOwner(
			String searchTerm, int limit, Long excludedId,List<UserData> adminsToExcludeFromSearch,List<Role> adminRoles) {
		TextSearchResponse1<UserData> response = new TextSearchResponse1<>();
		try {
			Client client = ElasticSearchFactory.getClient();
			esIndexer.addMapping(client, ESIndexNames.INDEX_USERS, ESIndexTypes.USER);

			BoolQueryBuilder bQueryBuilder = QueryBuilders.boolQuery();
			if (searchTerm != null && !searchTerm.isEmpty()) {
				QueryBuilder qb = QueryBuilders
						.queryStringQuery(searchTerm.toLowerCase() + "*").useDisMax(true)
						.defaultOperator(QueryStringQueryBuilder.Operator.AND)
						.field("name").field("lastname");

				bQueryBuilder.must(qb);
			}

			BoolQueryBuilder bqb = QueryBuilders.boolQuery()
					.must(bQueryBuilder);

			if(adminsToExcludeFromSearch == null) {
				bqb.mustNot(termQuery("id", excludedId));
			}else{
				for(UserData admin : adminsToExcludeFromSearch){
					bQueryBuilder.mustNot(termQuery("id", admin.getId()));
				}
			}
			if(adminRoles != null && !adminRoles.isEmpty()){
				BoolQueryBuilder bqb1 = QueryBuilders.boolQuery();
				for(Role r : adminRoles){
					bqb1.should(termQuery("roles.id", r.getId()));
				}
				bQueryBuilder.filter(bqb1);
			}

			try {
				String[] includes = {"id", "name", "lastname", "avatar"};
				SearchRequestBuilder searchRequestBuilder = client.prepareSearch(ESIndexNames.INDEX_USERS)
						.setTypes(ESIndexTypes.USER)
						.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
						.setQuery(bqb)
						.addSort("lastname", SortOrder.ASC)
						.addSort("name", SortOrder.ASC)
						.setFetchSource(includes, null)
						.setSize(limit);

				SearchResponse sResponse = searchRequestBuilder.execute().actionGet();

				if (sResponse != null) {
					SearchHits searchHits = sResponse.getHits();
					response.setHitsNumber(searchHits.getTotalHits());

					if (searchHits != null) {
						for (SearchHit sh : searchHits) {
							Map<String, Object> fields = sh.getSource();
							User user = new User();
							user.setId(Long.parseLong(fields.get("id") + ""));
							user.setName((String) fields.get("name"));
							user.setLastname((String) fields.get("lastname"));
							user.setAvatarUrl((String) fields.get("avatar"));
							user.setPosition((String) fields.get("position"));
							UserData userData = new UserData(user);

							response.addFoundNode(userData);
						}

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

}
