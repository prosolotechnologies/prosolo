package org.prosolo.search.impl;

import org.apache.log4j.Logger;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchPhaseExecutionException;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.filter.Filter;
import org.elasticsearch.search.aggregations.bucket.nested.Nested;
import org.elasticsearch.search.aggregations.bucket.terms.IncludeExclude;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms.Bucket;
import org.elasticsearch.search.aggregations.metrics.valuecount.ValueCount;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.NestedSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.prosolo.bigdata.common.enums.ESIndexTypes;
import org.prosolo.common.ESIndexNames;
import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.elasticsearch.ElasticSearchConnector;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.common.util.ElasticsearchUtil;
import org.prosolo.search.UserTextSearch;
import org.prosolo.search.util.competences.CompetenceStudentsSearchFilterValue;
import org.prosolo.search.util.competences.CompetenceStudentsSortOption;
import org.prosolo.search.util.credential.*;
import org.prosolo.search.util.roles.RoleFilter;
import org.prosolo.search.util.users.UserScopeFilter;
import org.prosolo.search.util.users.UserSearchConfig;
import org.prosolo.services.assessment.AssessmentManager;
import org.prosolo.services.common.data.SortingOption;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.interaction.FollowResourceManager;
import org.prosolo.services.nodes.CredentialInstructorManager;
import org.prosolo.services.nodes.DefaultManager;
import org.prosolo.services.nodes.RoleManager;
import org.prosolo.services.nodes.data.instructor.InstructorData;
import org.prosolo.services.user.UserManager;
import org.prosolo.services.user.data.StudentAssessmentInfo;
import org.prosolo.services.user.data.StudentData;
import org.prosolo.services.user.data.UserData;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.IOException;
import java.util.*;

import static org.elasticsearch.index.query.QueryBuilders.termQuery;


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
	@Inject private CredentialInstructorManager credInstructorManager;
	@Inject private RoleManager roleManager;
	@Inject private FollowResourceManager followResourceManager;
	@Inject private AssessmentManager assessmentManager;
	@Inject private UserManager userManager;

	@Override
	public PaginatedResult<UserData> searchUsers(
			long orgId, String searchString, int page, int limit, boolean loadOneMore,
			Collection<Long> includeUserIds, Collection<Long> excludeUserIds) {
		
		PaginatedResult<UserData> response = new PaginatedResult<>();
		
		try {
			int start = setStart(page, limit);
			limit = setLimit(limit, loadOneMore);

			QueryBuilder qb = QueryBuilders
					.queryStringQuery(ElasticsearchUtil.escapeSpecialChars(searchString.toLowerCase()) + "*")
					.defaultOperator(Operator.AND)
					.field("name").field("lastname");
			
			BoolQueryBuilder bQueryBuilder = QueryBuilders.boolQuery();
			bQueryBuilder.filter(qb);

			if (orgId > 0) {
				bQueryBuilder.mustNot(termQuery("system", true));
			}

			if (includeUserIds != null) {
				BoolQueryBuilder includeUsersQueryBuilder = QueryBuilders.boolQuery();
				for (Long userId : includeUserIds) {
					includeUsersQueryBuilder.should(termQuery("id", userId));
				}
				bQueryBuilder.filter(includeUsersQueryBuilder);
			}

			if (excludeUserIds != null) {
				for (Long exUserId : excludeUserIds) {
					bQueryBuilder.mustNot(termQuery("id", exUserId));
				}
			}
			SearchResponse sResponse = null;
			
			try {
				SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
				searchSourceBuilder
						.query(bQueryBuilder)
						.from(start)
						.size(limit)
						.sort(new FieldSortBuilder("lastname.sort").order(SortOrder.ASC))
						.sort(new FieldSortBuilder("name.sort").order(SortOrder.ASC));

				//System.out.println(searchRequestBuilder.toString());
				String indexName = orgId > 0 ? ElasticsearchUtil.getOrganizationIndexName(ESIndexNames.INDEX_USERS, orgId) : ESIndexNames.INDEX_USERS;
				String indexType = orgId > 0 ? ESIndexTypes.ORGANIZATION_USER : ESIndexTypes.USER;
				sResponse = ElasticSearchConnector.getClient().search(searchSourceBuilder, indexName, indexType);
			} catch (IOException|SearchPhaseExecutionException e) {
				logger.error("Error", e);
			}
	
			if (sResponse != null) {
				response.setHitsNumber(sResponse.getHits().getTotalHits());
			
				for (SearchHit hit : sResponse.getHits()) {
					Long id = ((Integer) hit.getSourceAsMap().get("id")).longValue();
					try {
						User user = defaultManager.loadResource(User.class, id);
						
						if (user != null) {
							response.addFoundNode(new UserData(user));
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
	public PaginatedResult<UserData> getUsersWithRoles(
			String term, int page, int limit, boolean paginate, long roleId, List<Role> roles,
			boolean includeSystemUsers, List<Long> excludeIds, long organizationId) {

		PaginatedResult<UserData> response =
				new PaginatedResult<>();

		try {
			int start = 0;
			int size = 1000;
			if (paginate) {
				start = setStart(page, limit);
				size = limit;
			}

			QueryBuilder qb = QueryBuilders
					.queryStringQuery(ElasticsearchUtil.escapeSpecialChars(term.toLowerCase()) + "*")
					.defaultOperator(Operator.AND)
					.field("name").field("lastname");

			BoolQueryBuilder bQueryBuilder = QueryBuilders.boolQuery();
			bQueryBuilder.should(qb);
			bQueryBuilder.filter(qb);

			if (organizationId > 0 && !includeSystemUsers) {
				bQueryBuilder.mustNot(termQuery("system", true));
			}

			if (excludeIds != null) {
				for (Long exUserId : excludeIds) {
					bQueryBuilder.mustNot(termQuery("id", exUserId));
				}
			}

			String[] includes = {"id", "name", "lastname", "avatar", "roles", "position"};

			SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
			searchSourceBuilder
					.query(bQueryBuilder)
					.from(start)
					.size(size)
					.sort(new FieldSortBuilder("lastname.sort").order(SortOrder.ASC))
					.sort(new FieldSortBuilder("name.sort").order(SortOrder.ASC))
					.aggregation(AggregationBuilders.count("docCount").field("id"))
					.fetchSource(includes, null);

			if (organizationId > 0) {
				searchSourceBuilder.aggregation(AggregationBuilders.nested("nestedAgg", "roles")
						.subAggregation(
								AggregationBuilders.terms("roles").field("roles.id")));
			} else {
				searchSourceBuilder.aggregation(AggregationBuilders.terms("roles").field("roles.id"));
			}

			if (roles != null && !roles.isEmpty()) {
				BoolQueryBuilder bqb1 = QueryBuilders.boolQuery();
				for(Role r : roles) {
					bqb1.should(termQuery("roles.id", r.getId()));
				}
				if (organizationId > 0) {
					NestedQueryBuilder nestedFilter = QueryBuilders.nestedQuery("roles", bqb1, ScoreMode.None);
					bQueryBuilder.filter(nestedFilter);
				} else {
					bQueryBuilder.filter(bqb1);
				}
			}

			//set as a post filter so it does not influence aggregation results
			if (roleId > 0) {
				BoolQueryBuilder bqb = QueryBuilders.boolQuery().filter(termQuery("roles.id", roleId));
				if (organizationId > 0) {
					NestedQueryBuilder nestedPostFilter = QueryBuilders.nestedQuery("roles", bqb, ScoreMode.None);
					searchSourceBuilder.postFilter(nestedPostFilter);
				} else {
					searchSourceBuilder.postFilter(bqb);
				}

			}

			//System.out.println(srb.toString());
			String indexName = organizationId > 0 ? ElasticsearchUtil.getOrganizationIndexName(ESIndexNames.INDEX_USERS, organizationId) : ESIndexNames.INDEX_USERS;
			String indexType = organizationId > 0 ? ESIndexTypes.ORGANIZATION_USER : ESIndexTypes.USER;
			SearchResponse sResponse = ElasticSearchConnector.getClient().search(searchSourceBuilder, indexName, indexType);

			if (sResponse != null) {
				response.setHitsNumber(sResponse.getHits().getTotalHits());
				List<Role> listRoles;

				if (roles == null || roles.isEmpty()){
					listRoles = roleManager.getAllRoles();
				} else {
					listRoles = roles;
				}

				for (SearchHit sh : sResponse.getHits()) {
					Map<String, Object> fields = sh.getSourceAsMap();
					User user = new User();
					user.setId(Long.parseLong(fields.get("id") + ""));
					user.setName((String) fields.get("name"));
					user.setLastname((String) fields.get("lastname"));
					user.setAvatarUrl((String) fields.get("avatar"));
					user.setPosition((String) fields.get("position"));
					user.setEmail(userManager.getUserEmail(user.getId()));
					@SuppressWarnings("unchecked")
					List<Map<String, Object>> rolesList = (List<Map<String, Object>>) fields.get("roles");
					List<Role> userRoles = new ArrayList<>();
					if(rolesList != null) {
						for(Map<String, Object> map : rolesList) {
							Role r = getRoleDataForId(listRoles, Long.parseLong(map.get("id") + ""));
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
				Terms terms;
				if (organizationId > 0) {
					Nested nestedAgg = sResponse.getAggregations().get("nestedAgg");
					terms = nestedAgg.getAggregations().get("roles");
				} else {
					terms = sResponse.getAggregations().get("roles");
				}
				List<? extends Terms.Bucket> buckets = terms.getBuckets();

				List<RoleFilter> roleFilters = new ArrayList<>();
				RoleFilter defaultFilter = new RoleFilter(0, "All", docCount.getValue());
				roleFilters.add(defaultFilter);
				RoleFilter selectedFilter = defaultFilter;
				for(Role role : listRoles) {
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
	
	private Bucket getBucketForRoleId(long id, List<? extends Bucket> buckets) {
		if(buckets != null) {
			for(Bucket b : buckets) {
				if(Long.parseLong(b.getKey().toString()) == id) {
					return b;
				}
			}
		}
		return null;
	}

	private Role getRoleDataForId(List<Role> roles,
			long roleId) {
		if(roles != null) {
			for(Role r : roles) {
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
	public TextSearchFilteredResponse<StudentData, CredentialMembersSearchFilter.SearchFilter> searchCredentialMembers (
			long organizationId, String searchTerm, CredentialMembersSearchFilter.SearchFilter filter, CredentialStudentsInstructorFilter instructorFilter,
			int page, int limit, long credId, CredentialMembersSortOption sortOption) {
		TextSearchFilteredResponse<StudentData, CredentialMembersSearchFilter.SearchFilter> response =
				new TextSearchFilteredResponse<>();
		try {
			int start = 0;
			start = setStart(page, limit);

			BoolQueryBuilder bQueryBuilder = QueryBuilders.boolQuery();
			if (searchTerm != null && !searchTerm.isEmpty()) {
				QueryBuilder qb = QueryBuilders
						.queryStringQuery(ElasticsearchUtil.escapeSpecialChars(searchTerm.toLowerCase()) + "*")
						.defaultOperator(Operator.AND)
						.field("name").field("lastname");
				
				bQueryBuilder.must(qb);
			}
			
			BoolQueryBuilder nestedFB = QueryBuilders.boolQuery();
			nestedFB.must(QueryBuilders.termQuery("credentials.id", credId));

			if (instructorFilter != null && instructorFilter.getFilter() != CredentialStudentsInstructorFilter.SearchFilter.ALL) {
				nestedFB.filter(QueryBuilders.termQuery("credentials.instructorId", instructorFilter.getInstructorId()));
			}
			NestedQueryBuilder nestedFilter1 = QueryBuilders.nestedQuery("credentials", nestedFB, ScoreMode.None);

			bQueryBuilder.filter(nestedFilter1);
			
			try {
				String[] includes = {"id", "name", "lastname", "avatar", "position"};

				SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
				searchSourceBuilder
						.query(bQueryBuilder)
						.from(start)
						.size(limit)
						.aggregation(AggregationBuilders.nested("nestedAgg", "credentials")
								.subAggregation(AggregationBuilders.filter("filtered", QueryBuilders.termQuery("credentials.id", credId))
										.subAggregation(AggregationBuilders.filter("completed", QueryBuilders.termQuery("credentials.progress", 100)))
										.subAggregation(AggregationBuilders.filter("assessorNotified", QueryBuilders.termQuery("credentials.assessorNotified", true)))
										.subAggregation(AggregationBuilders.filter("assessed", QueryBuilders.termQuery("credentials.assessed", true)))))
						.fetchSource(includes, null);
				/*
				 * set filter as a post filter so it does not influence aggregation results
				 */
				if (filter == CredentialMembersSearchFilter.SearchFilter.All) {
					nestedFilter1.innerHit(new InnerHitBuilder());
				} else {
					BoolQueryBuilder postFilter = QueryBuilders.boolQuery();
					switch (filter) {
						case AssessorNotified:
							postFilter.filter(QueryBuilders.termQuery("credentials.assessorNotified", true));
							break;
						case Nongraded:
							postFilter.filter(QueryBuilders.termQuery("credentials.assessed", false));
							break;
						case Graded:
							postFilter.filter(QueryBuilders.termQuery("credentials.assessed", true));
							break;
						case Completed:
							postFilter.filter(QueryBuilders.termQuery("credentials.progress", 100));
							break;
					}

					/*
					 * need to add this condition again or post filter will be applied on other
					 * credentials for users matched by query
					 */
					postFilter.filter(QueryBuilders.termQuery("credentials.id", credId));
					NestedQueryBuilder nestedFilter = QueryBuilders.nestedQuery("credentials", postFilter, ScoreMode.None).innerHit(new InnerHitBuilder());
					searchSourceBuilder.postFilter(nestedFilter);
				}
				
				//add sorting
				addCredentialSortOption(sortOption, credId, searchSourceBuilder);
				//System.out.println(searchRequestBuilder.toString());
				String indexName = ElasticsearchUtil.getOrganizationIndexName(ESIndexNames.INDEX_USERS, organizationId);
				SearchResponse sResponse = ElasticSearchConnector.getClient().search(searchSourceBuilder, indexName, ESIndexTypes.ORGANIZATION_USER);
				
				if (sResponse != null) {
					SearchHits searchHits = sResponse.getHits();
					response.setHitsNumber(searchHits.getTotalHits());
					
					if (searchHits != null) {
						for(SearchHit sh : searchHits) {
							StudentData student = new StudentData();
							Map<String, Object> fields = sh.getSourceAsMap();
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
								Map<String, Object> credential = innerHits.getAt(0).getSourceAsMap();
								
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
											.getActiveInstructorCredentialAssessmentId(credId, user.getId());
									if (credAssessmentId.isPresent()) {
										student.setStudentAssessmentInfo(
												new StudentAssessmentInfo(
														credAssessmentId.get(),
														Boolean.parseBoolean(credential.get("assessorNotified").toString())));
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
						Filter completed = filtered.getAggregations().get("completed");
						Filter assessorNotified = filtered.getAggregations().get("assessorNotified");
						Filter assessed = filtered.getAggregations().get("assessed");
						//Terms terms = nestedAgg.getAggregations().get("unassigned");
//
// 						Iterator<? extends Terms.Bucket> it = terms.getBuckets().iterator();
//						long unassignedNo = 0;
//						if(it.hasNext()) {
//							unassignedNo = it.next().getDocCount();
//						}

						long allStudentsNumber = filtered.getDocCount();
						
						response.putFilter(CredentialMembersSearchFilter.SearchFilter.All, allStudentsNumber);
						response.putFilter(CredentialMembersSearchFilter.SearchFilter.AssessorNotified, assessorNotified.getDocCount());
						response.putFilter(CredentialMembersSearchFilter.SearchFilter.Nongraded, allStudentsNumber - assessed.getDocCount());
						response.putFilter(CredentialMembersSearchFilter.SearchFilter.Graded, assessed.getDocCount());
						response.putFilter(CredentialMembersSearchFilter.SearchFilter.Completed, completed.getDocCount());

						return response;
					}
				}
			} catch (SearchPhaseExecutionException spee) {
				logger.error("Error", spee);
			}
		} catch (Exception e1) {
			logger.error("Error", e1);
		}
		return response;
	}

	private void addCredentialSortOption(CredentialMembersSortOption sortOption, long credId, SearchSourceBuilder searchSourceBuilder) {
		//add sorting
		SortOrder sortOrder = sortOption.getSortOrder() ==
				SortingOption.ASC ?
				SortOrder.ASC : SortOrder.DESC;
		for (String field : sortOption.getSortFields()) {
			if (sortOption.isNestedSort()) {
				String nestedDoc = field.substring(0, field.indexOf("."));
				BoolQueryBuilder credFilter = QueryBuilders.boolQuery();
				credFilter.must(QueryBuilders.termQuery(nestedDoc + ".id", credId));
				//searchRequestBuilder.addSort(field, sortOrder).setQuery(credFilter);
				FieldSortBuilder sortB = SortBuilders.fieldSort(field).order(sortOrder)
						.setNestedSort(new NestedSortBuilder(nestedDoc).setFilter(credFilter));
				//setting nested path and nested filter is deprecated
				//.setNestedPath(nestedDoc).setNestedFilter(credFilter);
				searchSourceBuilder.sort(sortB);
			} else {
				searchSourceBuilder.sort(field, sortOrder);
			}
		}
	}
	
	/*
	 * if pagination is not wanted and all results should be returned
	 * -1 should be passed as a page parameter 
	 */
	@Override
	public PaginatedResult<InstructorData> searchInstructors (
			long organizationId, String searchTerm, int page, int limit, long credId,
			InstructorSortOption sortOption, List<Long> excludedIds) {
		PaginatedResult<InstructorData> response = new PaginatedResult<>();
		try {
			BoolQueryBuilder bQueryBuilder = QueryBuilders.boolQuery();
			
			if (searchTerm != null && !searchTerm.isEmpty()) {
				QueryBuilder qb = QueryBuilders
						.queryStringQuery(ElasticsearchUtil.escapeSpecialChars(searchTerm.toLowerCase()) + "*")
						.defaultOperator(Operator.AND)
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
				SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
				searchSourceBuilder
						.query(bQueryBuilder)
						.fetchSource(includes, null);
				
				int start = 0;
				int size = 1000;
				if (page != -1) {
					start = setStart(page, limit);
					size = limit;
				}
				searchSourceBuilder.from(start).size(size);

				//add sorting
				for (String field : sortOption.getSortFields()) {
					SortOrder sortOrder = sortOption.getSortOrder() == 
							SortingOption.ASC ?
							SortOrder.ASC : SortOrder.DESC;
					searchSourceBuilder.sort(field, sortOrder);
				}

				//System.out.println(searchRequestBuilder.toString());
				String indexName = ElasticsearchUtil.getOrganizationIndexName(ESIndexNames.INDEX_USERS, organizationId);
				SearchResponse sResponse = ElasticSearchConnector.getClient().search(searchSourceBuilder, indexName, ESIndexTypes.ORGANIZATION_USER);
				if (sResponse != null) {
					SearchHits searchHits = sResponse.getHits();
					
					response.setHitsNumber(searchHits.getTotalHits());
					
					if (searchHits != null) {
						for(SearchHit sh : searchHits) {
							Map<String, Object> fields = sh.getSourceAsMap();
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
	
		} catch (Exception e1) {
			logger.error(e1);
		}
		return response;
	}
	
	@Override
	public PaginatedResult<UserData> searchUsersWithInstructorRole (long orgId, String searchTerm,
																	long credId, long roleId, List<Long> unitIds) {
		PaginatedResult<UserData> response = new PaginatedResult<>();
		try {
			if (unitIds == null || unitIds.isEmpty()) {
				return response;
			}
			
			BoolQueryBuilder bQueryBuilder = QueryBuilders.boolQuery();
			
			if(searchTerm != null && !searchTerm.isEmpty()) {
				QueryBuilder qb = QueryBuilders
						.queryStringQuery(ElasticsearchUtil.escapeSpecialChars(searchTerm.toLowerCase()) + "*")
						.defaultOperator(Operator.AND)
						.field("name").field("lastname");
				
				bQueryBuilder.filter(qb);
			}

			BoolQueryBuilder unitRoleFilter = QueryBuilders.boolQuery();
			unitRoleFilter.filter(termQuery("roles.id", roleId));
			BoolQueryBuilder unitFilter = QueryBuilders.boolQuery();
			for (long unitId : unitIds) {
				unitFilter.should(termQuery("roles.units.id", unitId));
			}
			unitRoleFilter.filter(unitFilter);

			NestedQueryBuilder nestedFilter = QueryBuilders.nestedQuery("roles", unitRoleFilter, ScoreMode.None);
			bQueryBuilder.filter(nestedFilter);
			
			//bQueryBuilder.minimumNumberShouldMatch(1);
			
			bQueryBuilder.mustNot(termQuery("credentialsWithInstructorRole.id", credId));

			try {
				String[] includes = {"id", "name", "lastname", "avatar", "position"};
				SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
				searchSourceBuilder
						.query(bQueryBuilder)
						.fetchSource(includes, null)
						.from(0).size(1000);
				
				searchSourceBuilder.sort("lastname.sort", SortOrder.ASC);
				searchSourceBuilder.sort("name.sort", SortOrder.ASC);
				//System.out.println(searchRequestBuilder.toString());
				String indexName = ElasticsearchUtil.getOrganizationIndexName(ESIndexNames.INDEX_USERS, orgId);
				SearchResponse sResponse = ElasticSearchConnector.getClient().search(searchSourceBuilder, indexName, ESIndexTypes.ORGANIZATION_USER);
				if (sResponse != null) {
					SearchHits searchHits = sResponse.getHits();
					
					response.setHitsNumber(searchHits.getTotalHits());
					
					if (searchHits != null) {
						for (SearchHit sh : searchHits) {
							Map<String, Object> fields = sh.getSourceAsMap();
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
	
		} catch (Exception e1) {
			logger.error(e1);
		}
		return response;
	}
	
	@Override
	public PaginatedResult<StudentData> searchUnassignedAndStudentsAssignedToInstructor(
			long orgId, String searchTerm, long credId, long instructorId, StudentAssignSearchFilter.SearchFilter filter,
			int page, int limit) {
		PaginatedResult<StudentData> response = new PaginatedResult<>();
		try {
			int start = 0;
			start = setStart(page, limit);
			
			BoolQueryBuilder bQueryBuilder = QueryBuilders.boolQuery();
			
			if (searchTerm != null && !searchTerm.isEmpty()) {
				QueryBuilder qb = QueryBuilders
						.queryStringQuery(ElasticsearchUtil.escapeSpecialChars(searchTerm.toLowerCase()) + "*")
						.defaultOperator(Operator.AND)
						.field("name.sort").field("lastname.sort");
				
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
					credFilter, ScoreMode.None);
		
			BoolQueryBuilder qb = QueryBuilders.boolQuery();
			qb.must(bQueryBuilder);
			qb.filter(nestedCredFilter);

			try {
				String[] includes = {"id", "name", "lastname", "avatar", "position"};
				SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
				searchSourceBuilder
						.query(qb)
						.aggregation(AggregationBuilders.nested("nestedAgg", "credentials")
								.subAggregation(
										AggregationBuilders.filter("filtered", credFilter)
												.subAggregation(
														AggregationBuilders.terms("unassigned")
																.field("credentials.instructorId")
																.includeExclude(new IncludeExclude(new long[] {0}, null)))))
						.fetchSource(includes, null)
						.from(start).size(limit);
				
				/*
				 * set instructor assign filter as a post filter so it does not influence
				 * aggregation results
				 */
				BoolQueryBuilder filterBuilder = null;
				switch (filter) {
					case All:
						nestedCredFilter.innerHit(new InnerHitBuilder());
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
				if (filterBuilder != null) {
					NestedQueryBuilder nestedFilter = QueryBuilders.nestedQuery("credentials",
							filterBuilder, ScoreMode.None).innerHit(new InnerHitBuilder());
					searchSourceBuilder.postFilter(nestedFilter);
				}
				searchSourceBuilder.sort("credentials.instructorId", SortOrder.DESC);
				searchSourceBuilder.sort("lastname.sort", SortOrder.ASC);
				searchSourceBuilder.sort("name.sort", SortOrder.ASC);
				//System.out.println(searchRequestBuilder.toString());
				String indexName = ElasticsearchUtil.getOrganizationIndexName(ESIndexNames.INDEX_USERS, orgId);
				SearchResponse sResponse = ElasticSearchConnector.getClient().search(searchSourceBuilder, indexName, ESIndexTypes.ORGANIZATION_USER);
				if (sResponse != null) {
					SearchHits searchHits = sResponse.getHits();
					
					response.setHitsNumber(searchHits.getTotalHits());
					
					if (searchHits != null) {
						for(SearchHit sh : searchHits) {
							Map<String, Object> fields = sh.getSourceAsMap();
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
								Map<String, Object> credential = innerHits.getAt(0).getSourceAsMap();
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
					Iterator<? extends Terms.Bucket> it = terms.getBuckets().iterator();
					long unassignedNo = 0;
					if(it.hasNext()) {
						unassignedNo = it.next().getDocCount();
					}
					long allNo = filtered.getDocCount();
					StudentAssignSearchFilter[] filters = new StudentAssignSearchFilter[3];
					filters[0] = new StudentAssignSearchFilter(StudentAssignSearchFilter.SearchFilter.All,
							allNo);
					filters[1] = new StudentAssignSearchFilter(StudentAssignSearchFilter.SearchFilter.Assigned,
							allNo - unassignedNo);
					filters[2] = new StudentAssignSearchFilter(StudentAssignSearchFilter.SearchFilter.Unassigned,
							unassignedNo);
					StudentAssignSearchFilter selectedFilter = null;
					for(StudentAssignSearchFilter f : filters) {
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
	
		} catch (Exception e1) {
			logger.error(e1);
		}
		return response;
	}
	
	@Override
	public PaginatedResult<StudentData> searchCredentialMembersWithLearningStatusFilter (
			long orgId, String searchTerm, LearningStatus filter, int page, int limit, long credId,
			long userId, CredentialMembersSortOption sortOption) {
		PaginatedResult<StudentData> response = new PaginatedResult<>();
		try {
			int start = 0;
			start = setStart(page, limit);
			
			BoolQueryBuilder bQueryBuilder = QueryBuilders.boolQuery();
			if (searchTerm != null && !searchTerm.isEmpty()) {
				QueryBuilder qb = QueryBuilders
						.queryStringQuery(ElasticsearchUtil.escapeSpecialChars(searchTerm.toLowerCase()) + "*")
						.defaultOperator(Operator.AND)
						.field("name").field("lastname");
				
				bQueryBuilder.must(qb);
			}
			
			BoolQueryBuilder nestedFB = QueryBuilders.boolQuery();
			nestedFB.must(QueryBuilders.termQuery("credentials.id", credId));
			NestedQueryBuilder nestedFilter1 = QueryBuilders.nestedQuery("credentials",
					nestedFB, ScoreMode.None);
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

				SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
				searchSourceBuilder
						.query(bqb)
						.aggregation(AggregationBuilders.nested("nestedAgg", "credentials")
								.subAggregation(AggregationBuilders.filter("filtered", QueryBuilders.termQuery("credentials.id", credId))
										.subAggregation(
												AggregationBuilders.terms("inactive")
														.field("credentials.progress")
														.includeExclude(new IncludeExclude(new long[] {100}, null)))))
						.fetchSource(includes, null);
				
				/*
				 * set learning status filter as a post filter so it does not influence
				 * aggregation results
				 */
				if (filter == LearningStatus.Active) {
					BoolQueryBuilder assignFilter = QueryBuilders.boolQuery();
					assignFilter.mustNot(QueryBuilders.termQuery("credentials.progress", 100));
					/*
					 * need to add this condition again or post filter will be applied on other 
					 * credentials for users matched by query
					 */
					assignFilter.filter(QueryBuilders.termQuery("credentials.id", credId));
					NestedQueryBuilder nestedFilter = QueryBuilders.nestedQuery("credentials",
							assignFilter, ScoreMode.None).innerHit(new InnerHitBuilder());
					searchSourceBuilder.postFilter(nestedFilter);
				} else {
					nestedFilter1.innerHit(new InnerHitBuilder());
				}
				
				searchSourceBuilder.from(start).size(limit);
				
				//add sorting
				addCredentialSortOption(sortOption, credId, searchSourceBuilder);
				//System.out.println(searchRequestBuilder.toString());
				String indexName = ElasticsearchUtil.getOrganizationIndexName(ESIndexNames.INDEX_USERS, orgId);
				SearchResponse sResponse = ElasticSearchConnector.getClient().search(searchSourceBuilder, indexName, ESIndexTypes.ORGANIZATION_USER);
				
				if (sResponse != null) {
					SearchHits searchHits = sResponse.getHits();
					response.setHitsNumber(searchHits.getTotalHits());
					
					if (searchHits != null) {
						for (SearchHit sh : searchHits) {
							StudentData student = new StudentData();
							Map<String, Object> fields = sh.getSourceAsMap();
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
								Map<String, Object> credential = innerHits.getAt(0).getSourceAsMap();
								
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
						Iterator<? extends Terms.Bucket> it = terms.getBuckets().iterator();
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
	
		} catch (Exception e1) {
			logger.error(e1);
		}
		return null;
	}
	
	@Override
	public PaginatedResult<StudentData> searchUnenrolledUsersWithUserRole (
			 long orgId, String searchTerm, int page, int limit, long credId, long userRoleId,
			 List<Long> unitIds) {
		PaginatedResult<StudentData> response = new PaginatedResult<>();
		try {
			if (unitIds == null || unitIds.isEmpty()) {
				return response;
			}

			int start = 0;
			start = setStart(page, limit);

			BoolQueryBuilder bQueryBuilder = QueryBuilders.boolQuery();
			
			if(searchTerm != null && !searchTerm.isEmpty()) {
				QueryBuilder qb = QueryBuilders
						.queryStringQuery(ElasticsearchUtil.escapeSpecialChars(searchTerm.toLowerCase()) + "*")
						.defaultOperator(Operator.AND)
						.field("name").field("lastname");
				
				bQueryBuilder.must(qb);
			}
			bQueryBuilder.mustNot(termQuery("system", true));
			
			//bQueryBuilder.filter(termQuery("roles.id", userRoleId));

			BoolQueryBuilder unitRoleFilter = QueryBuilders.boolQuery();
			unitRoleFilter.filter(termQuery("roles.id", userRoleId));
			BoolQueryBuilder unitFilter = QueryBuilders.boolQuery();
			for (long unitId : unitIds) {
				unitFilter.should(termQuery("roles.units.id", unitId));
			}
			unitRoleFilter.filter(unitFilter);

			NestedQueryBuilder nestedFilter = QueryBuilders.nestedQuery("roles", unitRoleFilter, ScoreMode.None);
			bQueryBuilder.filter(nestedFilter);
			
			BoolQueryBuilder nestedFB = QueryBuilders.boolQuery();
			nestedFB.must(QueryBuilders.termQuery("credentials.id", credId));
			NestedQueryBuilder nestedFilter1 = QueryBuilders.nestedQuery("credentials", nestedFB, ScoreMode.None);

			bQueryBuilder.mustNot(nestedFilter1);
			
			//bQueryBuilder.must(termQuery("credentials.id", credId));
			
			try {
				String[] includes = {"id", "name", "lastname", "avatar", "position"};
				SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
				searchSourceBuilder
						.query(bQueryBuilder)
						.fetchSource(includes, null)
						.from(start)
						.size(limit);
				
				//add sorting
				searchSourceBuilder.sort("lastname.sort", SortOrder.ASC);
				searchSourceBuilder.sort("name.sort", SortOrder.ASC);
				//System.out.println(searchRequestBuilder.toString());
				String indexName = ElasticsearchUtil.getOrganizationIndexName(ESIndexNames.INDEX_USERS, orgId);
				SearchResponse sResponse = ElasticSearchConnector.getClient().search(searchSourceBuilder, indexName, ESIndexTypes.ORGANIZATION_USER);
				
				if (sResponse != null) {
					SearchHits searchHits = sResponse.getHits();
					response.setHitsNumber(searchHits.getTotalHits());
					
					if (searchHits != null) {
						for (SearchHit sh : searchHits) {
							StudentData student = new StudentData();
							Map<String, Object> fields = sh.getSourceAsMap();
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
	
		} catch (Exception e1) {
			logger.error(e1);
		}
		return null;
	}
	
	@Override
	public PaginatedResult<UserData> searchUsersWithFollowInfo(
			long orgId, String term, int page, int limit, long userId, UserSearchConfig searchConfig) {
		PaginatedResult<UserData> response = new PaginatedResult<>();
		
		try {
			int size = limit;
			int start = setStart(page, limit);

			QueryBuilder qb = QueryBuilders
					.queryStringQuery(ElasticsearchUtil.escapeSpecialChars(term.toLowerCase()) + "*")
					.defaultOperator(Operator.AND)
					.field("name").field("lastname");
			
			BoolQueryBuilder bQueryBuilder = QueryBuilders.boolQuery();
			bQueryBuilder.filter(qb);
			switch (searchConfig.getScope()) {
				case FOLLOWING:
					/*
					if given scope is 'users which given user is following' we search users
					that have our user in followers collection
					 */
					bQueryBuilder.filter(termQuery("followers.id", userId));
					break;
				case FOLLOWERS:
					/*
					if given scope is 'users that follow given user' we search users
					that have our user in following collection
					 */
					bQueryBuilder.filter(termQuery("following.id", userId));
					break;
				case ORGANIZATION:
					//don't return user for whom we are issuing query
					bQueryBuilder.mustNot(termQuery("id", userId));
					break;
			}

			BoolQueryBuilder roleFilter = QueryBuilders.boolQuery();
			roleFilter.filter(termQuery("roles.id", searchConfig.getRoleId()));
			NestedQueryBuilder nestedFilter = QueryBuilders.nestedQuery("roles", roleFilter, ScoreMode.None);
			bQueryBuilder.filter(nestedFilter);
			if (searchConfig.getUserScopeFilter() == UserScopeFilter.USERS_UNITS) {
				//if empty unit ids collection empty result set is returned
				if (searchConfig.getUnitIds().isEmpty()) {
					return new PaginatedResult<>();
				}

				BoolQueryBuilder unitFilter = QueryBuilders.boolQuery();
				for (long unitId : searchConfig.getUnitIds()) {
					unitFilter.should(termQuery("roles.units.id", unitId));
				}
				roleFilter.filter(unitFilter);
			}

			SearchResponse sResponse = null;
			
			String[] includes = {"id", "name", "lastname", "avatar", "position"};
			SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
			searchSourceBuilder
					.query(bQueryBuilder)
					.fetchSource(includes, null)
					.from(start)
					.size(size)
					.sort("lastname.sort", SortOrder.ASC)
					.sort("name.sort", SortOrder.ASC);

			//System.out.println(srb.toString());
			String indexName = ElasticsearchUtil.getOrganizationIndexName(ESIndexNames.INDEX_USERS, orgId);
			sResponse = ElasticSearchConnector.getClient().search(searchSourceBuilder, indexName, ESIndexTypes.ORGANIZATION_USER);
			
			if (sResponse != null) {
				response.setHitsNumber(sResponse.getHits().getTotalHits());
				
				for (SearchHit sh : sResponse.getHits()) {
//					Map<String, Object> fields = sh.getSourceAsMap();
//					User user = new User();
//					user.setId(Long.parseLong(fields.get("id") + ""));
//					user.setName((String) fields.get("name"));
//					user.setLastname((String) fields.get("lastname"));
//					user.setAvatarUrl((String) fields.get("avatar"));
//					UserData userData = new UserData(user);
					UserData userData = getUserDataFromSearchHit(sh);
					/*
					if search scope is following users, there is no need to issue a query to check
					whether given user is following user from result set
					 */
					userData.setFollowedByCurrentUser(
							searchConfig.getScope() == UserSearchConfig.UserScope.FOLLOWING
									|| followResourceManager.isUserFollowingUser(userId, userData.getId()));
					
					response.addFoundNode(userData);
				}
			}
		} catch (Exception e1) {
			logger.error("Error", e1);
		}
		return response;
	}
	
	@Override
	public PaginatedResult<UserData> searchUsersInGroups(
			long orgId, String searchTerm, int page, int limit, long groupId,
			boolean includeSystemUsers) {
		PaginatedResult<UserData> response = new PaginatedResult<>();
		try {
			int start = 0;
			start = setStart(page, limit);
			
			BoolQueryBuilder bQueryBuilder = QueryBuilders.boolQuery();
			if (searchTerm != null && !searchTerm.isEmpty()) {
				QueryBuilder qb = QueryBuilders
						.queryStringQuery(ElasticsearchUtil.escapeSpecialChars(searchTerm.toLowerCase()) + "*")
						.defaultOperator(Operator.AND)
						.field("name").field("lastname");
				
				bQueryBuilder.filter(qb);
			}
			
			if (!includeSystemUsers) {
				bQueryBuilder.mustNot(termQuery("system", true));
			}

			bQueryBuilder.filter(termQuery("groups.id", groupId));

			String[] includes = {"id", "name", "lastname", "avatar", "position"};
			SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
			searchSourceBuilder
					.query(bQueryBuilder)
					.fetchSource(includes, null)
					.from(start)
					.size(limit)
					.sort("lastname.sort", SortOrder.ASC)
					.sort("name.sort", SortOrder.ASC);

			//System.out.println(searchRequestBuilder.toString());
			String indexName = ElasticsearchUtil.getOrganizationIndexName(ESIndexNames.INDEX_USERS, orgId);
			SearchResponse sResponse = ElasticSearchConnector.getClient().search(searchSourceBuilder, indexName, ESIndexTypes.ORGANIZATION_USER);
			
			if (sResponse != null) {
				SearchHits searchHits = sResponse.getHits();
				response.setHitsNumber(searchHits.getTotalHits());
				
				if (searchHits != null) {
					for (SearchHit sh : searchHits) {
						UserData userData = getUserDataFromSearchHit(sh);
						userData.setEmail(userManager.getUserEmail(userData.getId()));
						response.addFoundNode(userData);
					}
				}
			}
	
		} catch (Exception e1) {
			logger.error(e1);
		}
		return response;
	}

	private UserData getUserDataFromSearchHit(SearchHit sh) {
		Map<String, Object> fields = sh.getSourceAsMap();
		User user = new User();
		user.setId(Long.parseLong(fields.get("id") + ""));
		user.setName((String) fields.get("name"));
		user.setLastname((String) fields.get("lastname"));
		user.setAvatarUrl((String) fields.get("avatar"));
		user.setPosition((String) fields.get("position"));
		return new UserData(user);
	}
	
	@Override
	public PaginatedResult<UserData> searchCredentialPeers(
			long orgId, String searchTerm, long limit, long credId, List<Long> peersToExcludeFromSearch) {
		PaginatedResult<UserData> response = new PaginatedResult<>();
		try {
			BoolQueryBuilder bQueryBuilder = QueryBuilders.boolQuery();
			if (searchTerm != null && !searchTerm.isEmpty()) {
				QueryBuilder qb = QueryBuilders
						.queryStringQuery(ElasticsearchUtil.escapeSpecialChars(searchTerm.toLowerCase()) + "*")
						.defaultOperator(Operator.AND)
						.field("name").field("lastname");
				
				bQueryBuilder.must(qb);
			}
			
			BoolQueryBuilder bqb = QueryBuilders.boolQuery()
					.must(bQueryBuilder)
					.filter(QueryBuilders.nestedQuery(
									"credentials",
									QueryBuilders.boolQuery().must(QueryBuilders.termQuery("credentials.id", credId)),
									ScoreMode.None));
			
			if (peersToExcludeFromSearch != null) {
				for (Long exUserId : peersToExcludeFromSearch) {
					bqb.mustNot(termQuery("id", exUserId));
				}
			}
			
			try {
				String[] includes = {"id", "name", "lastname", "avatar"};
				SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
				searchSourceBuilder
						.query(bqb)
						.fetchSource(includes, null)
						.size(3)
						.sort("lastname.sort", SortOrder.ASC)
						.sort("name.sort", SortOrder.ASC);

				String indexName = ElasticsearchUtil.getOrganizationIndexName(ESIndexNames.INDEX_USERS, orgId);
				SearchResponse sResponse = ElasticSearchConnector.getClient().search(searchSourceBuilder, indexName, ESIndexTypes.ORGANIZATION_USER);
				
				if (sResponse != null) {
					SearchHits searchHits = sResponse.getHits();
					response.setHitsNumber(searchHits.getTotalHits());
					
					if (searchHits != null) {
						for (SearchHit sh : searchHits) {
							Map<String, Object> fields = sh.getSourceAsMap();
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
		} catch (Exception e1) {
			logger.error(e1);
		}
		return null;
	}

	@Override
	public PaginatedResult<UserData> searchUsersLearningCompetence(
			long orgId, String searchTerm, int limit, long compId, List<Long> usersToExcludeFromSearch) {
		PaginatedResult<UserData> response = new PaginatedResult<>();
		try {
			BoolQueryBuilder bQueryBuilder = QueryBuilders.boolQuery();
			if (searchTerm != null && !searchTerm.isEmpty()) {
				QueryBuilder qb = QueryBuilders
						.queryStringQuery(ElasticsearchUtil.escapeSpecialChars(searchTerm.toLowerCase()) + "*").useDisMax(true)
						.defaultOperator(Operator.AND)
						.field("name").field("lastname");

				bQueryBuilder.filter(qb);
			}

			BoolQueryBuilder bqb = QueryBuilders.boolQuery()
					.filter(bQueryBuilder)
					.filter(QueryBuilders.nestedQuery("competences",
							QueryBuilders.boolQuery()
									.filter(QueryBuilders.termQuery("competences.id", compId)), ScoreMode.None));

			if (usersToExcludeFromSearch != null) {
				for (Long exUserId : usersToExcludeFromSearch) {
					bqb.mustNot(termQuery("id", exUserId));
				}
			}

			String[] includes = {"id", "name", "lastname", "avatar"};
			SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
			searchSourceBuilder
					.query(bqb)
					.fetchSource(includes, null)
					.size(limit)
					.sort("lastname.sort", SortOrder.ASC)
					.sort("name.sort", SortOrder.ASC);

			String indexName = ElasticsearchUtil.getOrganizationIndexName(ESIndexNames.INDEX_USERS, orgId);
			SearchResponse sResponse = ElasticSearchConnector.getClient().search(searchSourceBuilder, indexName, ESIndexTypes.ORGANIZATION_USER);

			if (sResponse != null) {
				SearchHits searchHits = sResponse.getHits();
				response.setHitsNumber(searchHits.getTotalHits());

				if (searchHits != null) {
					for (SearchHit sh : searchHits) {
						Map<String, Object> fields = sh.getSourceAsMap();
						User user = new User();
						user.setId(Long.parseLong(fields.get("id") + ""));
						user.setName((String) fields.get("name"));
						user.setLastname((String) fields.get("lastname"));
						user.setAvatarUrl((String) fields.get("avatar"));
						UserData userData = new UserData(user);

						response.addFoundNode(userData);
					}

					return response;
				}
			}
		} catch (Exception e1) {
			logger.error("Error", e1);
		}
		return null;
	}
	
	@Override
	public TextSearchFilteredResponse<StudentData, CompetenceStudentsSearchFilterValue> searchCompetenceStudents (
			long orgId, String searchTerm, long compId, CompetenceStudentsSearchFilterValue filter,
			CompetenceStudentsSortOption sortOption, int page, int limit) {
		TextSearchFilteredResponse<StudentData, CompetenceStudentsSearchFilterValue> response = new TextSearchFilteredResponse<>();
		try {
			int start = 0;
			start = setStart(page, limit);
			
			BoolQueryBuilder bQueryBuilder = QueryBuilders.boolQuery();
			if (searchTerm != null && !searchTerm.isEmpty()) {
				QueryBuilder qb = QueryBuilders
						.queryStringQuery(ElasticsearchUtil.escapeSpecialChars(searchTerm.toLowerCase()) + "*")
						.defaultOperator(Operator.AND)
						.field("name.sort").field("lastname.sort");
				
				bQueryBuilder.must(qb);
			}
			
			BoolQueryBuilder nestedFB = QueryBuilders.boolQuery();
			nestedFB.must(QueryBuilders.termQuery("competences.id", compId));
			NestedQueryBuilder nestedFilter1 = QueryBuilders.nestedQuery("competences",
					nestedFB, ScoreMode.None);

			bQueryBuilder.filter(nestedFilter1);
			
			BoolQueryBuilder studentsLearningCompAggrFilter = QueryBuilders.boolQuery();
			studentsLearningCompAggrFilter.filter(QueryBuilders.termQuery("competences.id", compId));
			BoolQueryBuilder studentsCompletedCompAggrFilter = QueryBuilders.boolQuery();
			studentsCompletedCompAggrFilter.filter(QueryBuilders.termQuery("competences.progress", 100));
			try {
				String[] includes = {"id", "name", "lastname", "avatar", "position"};
				SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
				searchSourceBuilder
						.query(bQueryBuilder)
						.fetchSource(includes, null)
						.aggregation(AggregationBuilders.nested("nestedAgg", "competences")
								.subAggregation(AggregationBuilders.filter("filtered", studentsLearningCompAggrFilter)
										.subAggregation(AggregationBuilders.filter("completed", studentsCompletedCompAggrFilter))));
				
				/*
				 * set search filter as a post filter so it does not influence
				 * aggregation results
				 */
				if (filter == CompetenceStudentsSearchFilterValue.COMPLETED) {
					BoolQueryBuilder completedFilter = QueryBuilders.boolQuery();
					completedFilter.must(QueryBuilders.termQuery("competences.progress", 100));
					/*
					 * need to add this condition again or post filter will be applied on other 
					 * competences for users matched by query
					 */
					completedFilter.must(QueryBuilders.termQuery("competences.id", compId));
					NestedQueryBuilder nestedFilter = QueryBuilders.nestedQuery("competences",
							completedFilter, ScoreMode.None)
							//.innerHit(new QueryInnerHitBuilder());
							.innerHit(new InnerHitBuilder());
					searchSourceBuilder.postFilter(nestedFilter);
				} else if(filter == CompetenceStudentsSearchFilterValue.UNCOMPLETED) {
					BoolQueryBuilder uncompletedFilter = QueryBuilders.boolQuery();
					uncompletedFilter.mustNot(QueryBuilders.termQuery("competences.progress", 100));
					/*
					 * need to add this condition again or post filter will be applied on other 
					 * competences for users matched by query
					 */
					uncompletedFilter.must(QueryBuilders.termQuery("competences.id", compId));
					NestedQueryBuilder nestedFilter = QueryBuilders.nestedQuery("competences",
							uncompletedFilter, ScoreMode.None).innerHit(new InnerHitBuilder());
					searchSourceBuilder.postFilter(nestedFilter);
			    } else {
					nestedFilter1.innerHit(new InnerHitBuilder());
				}
				
				searchSourceBuilder.from(start).size(limit);
				
				//add sorting
				SortOrder sortOrder = sortOption.getSortOrder() == 
						SortingOption.ASC ?
						SortOrder.ASC : SortOrder.DESC;
				for (String field : sortOption.getSortFields()) {
					if (sortOption.isNestedSort()) {
						String nestedDoc = field.substring(0, field.indexOf("."));
						BoolQueryBuilder compFilter = QueryBuilders.boolQuery();
						compFilter.must(QueryBuilders.termQuery(nestedDoc + ".id", compId));
						//searchRequestBuilder.addSort(field, sortOrder).setQuery(credFilter);
						FieldSortBuilder sortB = SortBuilders.fieldSort(field).order(sortOrder)
								.setNestedSort(new NestedSortBuilder(nestedDoc).setFilter(compFilter));
						//.setNestedPath(nestedDoc).setNestedFilter(compFilter);
						searchSourceBuilder.sort(sortB);
					} else {
						searchSourceBuilder.sort(field, sortOrder);
					}
				}
				//System.out.println(searchRequestBuilder.toString());
				String indexName = ElasticsearchUtil.getOrganizationIndexName(ESIndexNames.INDEX_USERS, orgId);
				SearchResponse sResponse = ElasticSearchConnector.getClient().search(searchSourceBuilder, indexName, ESIndexTypes.ORGANIZATION_USER);
				
				if (sResponse != null) {
					SearchHits searchHits = sResponse.getHits();
					response.setHitsNumber(searchHits.getTotalHits());
					
					if (searchHits != null) {
						for (SearchHit sh : searchHits) {
							StudentData student = new StudentData();
							Map<String, Object> fields = sh.getSourceAsMap();
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
							if (totalInnerHits == 1) {
								Map<String, Object> competence = innerHits.getAt(0).getSourceAsMap();
								
								if(competence != null) {
									student.setProgress(Integer.parseInt(
											competence.get("progress").toString()));
									String dateEnrolledString = (String) competence.get("dateEnrolled");
									if(dateEnrolledString != null && !dateEnrolledString.isEmpty()) {
										student.setDateEnrolled(ElasticsearchUtil.parseDate(dateEnrolledString));
									}
									String dateCompletedString = (String) competence.get("dateCompleted");
									if(dateCompletedString != null && !dateCompletedString.isEmpty()) {
										student.setDateCompleted(ElasticsearchUtil.parseDate(dateCompletedString));
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
	
		} catch (Exception e1) {
			logger.error("Error", e1);
		}
		return null;
	}

	@Override
	public PaginatedResult<UserData> searchUsers(
				long orgId, String searchTerm, int limit, List<UserData> usersToExcludeFromSearch, List<Long> userRoles) {

		PaginatedResult<UserData> response = new PaginatedResult<>();
		try {
			BoolQueryBuilder bQueryBuilder = getUserSearchQueryBuilder(searchTerm);

			BoolQueryBuilder bqb = QueryBuilders.boolQuery()
					.must(bQueryBuilder);

			if (usersToExcludeFromSearch != null) {
				for (UserData admin : usersToExcludeFromSearch) {
					bQueryBuilder.mustNot(termQuery("id", admin.getId()));
				}
			}

			if (userRoles != null && !userRoles.isEmpty()){
				BoolQueryBuilder bqb1 = QueryBuilders.boolQuery();
				for (Long roleId : userRoles) {
					bqb1.should(termQuery("roles.id", roleId));
				}
				bQueryBuilder.filter(bqb1);
			}

			try {
				String[] includes = {"id", "name", "lastname", "avatar","position"};
				SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
				searchSourceBuilder
						.query(bqb)
						.fetchSource(includes, null)
						.sort("lastname.sort", SortOrder.ASC)
						.sort("name.sort", SortOrder.ASC)
						.size(limit);

				String indexName = orgId > 0
						? ElasticsearchUtil.getOrganizationIndexName(ESIndexNames.INDEX_USERS, orgId)
						: ESIndexNames.INDEX_USERS;
				String indexType = orgId > 0 ? ESIndexTypes.ORGANIZATION_USER : ESIndexTypes.USER;
				SearchResponse sResponse = ElasticSearchConnector.getClient().search(searchSourceBuilder, indexName, indexType);

				if (sResponse != null) {
					SearchHits searchHits = sResponse.getHits();
					response.setHitsNumber(searchHits.getTotalHits());

					if (searchHits != null) {
						for (SearchHit sh : searchHits) {
							response.addFoundNode(getUserDataFromSearchHit(sh));
						}

						return response;
					}
				}
			} catch (SearchPhaseExecutionException spee) {
				spee.printStackTrace();
				logger.error(spee);
			}
		} catch (Exception e1) {
			logger.error("Error", e1);
		}
		return null;
	}

	@Override
	public PaginatedResult<UserData> searchOrganizationUsersWithRoleNotAddedToUnit(
			long orgId, long unitId, long roleId, String searchTerm, int page, int limit,
			boolean includeSystemUsers) {
		return searchOrganizationUsersWithRole(orgId, unitId, roleId, searchTerm, page, limit,
				includeSystemUsers, false);
	}

	@Override
	public PaginatedResult<UserData> searchUnitUsersInRole(
			long orgId, long unitId, long roleId, String searchTerm, int page, int limit,
			boolean includeSystemUsers) {
		return searchOrganizationUsersWithRole(orgId, unitId, roleId, searchTerm, page, limit,
				includeSystemUsers, true);
	}

	/**
	 * Returns (paginated) users belonging to organization with {@code orgId} id that have role with {@code roleId} id
	 *
	 * If {@code searchUsersBelongingToUnit} flag is true, only users that are already added to unit in a role
	 * with {@code roleId} id are returned. Otherwise, only users that are not already added to unit are returned.
	 *
	 * @param orgId
	 * @param unitId
	 * @param roleId
	 * @param searchTerm
	 * @param page
	 * @param limit
	 * @param includeSystemUsers
	 * @param searchUsersBelongingToUnit
	 * @return
	 */
	private PaginatedResult<UserData> searchOrganizationUsersWithRole(long orgId, long unitId,
																	  long roleId, String searchTerm,
																	  int page, int limit,
																	  boolean includeSystemUsers,
																	  boolean searchUsersBelongingToUnit) {
		PaginatedResult<UserData> response = new PaginatedResult<>();
		try {
			int start = 0;
			start = setStart(page, limit);

			BoolQueryBuilder bQueryBuilder = getUserSearchQueryBuilder(searchTerm);

			if (!includeSystemUsers) {
				bQueryBuilder.mustNot(termQuery("system", true));
			}


			BoolQueryBuilder unitRoleFilter = QueryBuilders.boolQuery();
			unitRoleFilter.filter(termQuery("roles.id", roleId));
			QueryBuilder qb = termQuery("roles.units.id", unitId);
			if (searchUsersBelongingToUnit) {
				unitRoleFilter.filter(qb);
			} else {
				unitRoleFilter.mustNot(qb);
			}

			NestedQueryBuilder nestedFilter = QueryBuilders.nestedQuery("roles", unitRoleFilter, ScoreMode.None);
			bQueryBuilder.filter(nestedFilter);

			String[] includes = {"id", "name", "lastname", "avatar", "position"};
			SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
			searchSourceBuilder
					.query(bQueryBuilder)
					.fetchSource(includes, null)
					.sort("lastname.sort", SortOrder.ASC)
					.sort("name.sort", SortOrder.ASC)
					.from(start)
					.size(limit);

			//System.out.println(searchRequestBuilder.toString());
			String indexName = ElasticsearchUtil.getOrganizationIndexName(ESIndexNames.INDEX_USERS, orgId);
			SearchResponse sResponse = ElasticSearchConnector.getClient().search(searchSourceBuilder, indexName, ESIndexTypes.ORGANIZATION_USER);

			if (sResponse != null) {
				SearchHits searchHits = sResponse.getHits();
				response.setHitsNumber(searchHits.getTotalHits());

				if (searchHits != null) {
					for (SearchHit sh : searchHits) {
						UserData userData = getUserDataFromSearchHit(sh);
						userData.setEmail(userManager.getUserEmail(userData.getId()));
						response.addFoundNode(userData);
					}
				}
			}

		} catch (Exception e1) {
			logger.error("Error", e1);
		}
		return response;
	}

	@Override
	public PaginatedResult<UserData> searchUnitUsersNotAddedToGroup(long orgId, long unitId, long roleId,
																	 long groupId, String searchTerm,
																	 int page, int limit, boolean includeSystemUsers) {
		PaginatedResult<UserData> response = new PaginatedResult<>();
		try {
			int start = 0;
			start = setStart(page, limit);

			BoolQueryBuilder bQueryBuilder = getUserSearchQueryBuilder(searchTerm);

			if (!includeSystemUsers) {
				bQueryBuilder.mustNot(termQuery("system", true));
			}

			BoolQueryBuilder unitRoleFilter = QueryBuilders.boolQuery();
			unitRoleFilter.filter(termQuery("roles.id", roleId));
			QueryBuilder qb = termQuery("roles.units.id", unitId);
			unitRoleFilter.filter(qb);

			NestedQueryBuilder nestedFilter = QueryBuilders.nestedQuery("roles", unitRoleFilter, ScoreMode.None);
			bQueryBuilder.filter(nestedFilter);

			bQueryBuilder.mustNot(termQuery("groups.id", groupId));

			String[] includes = {"id", "name", "lastname", "avatar", "position"};

			SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
			searchSourceBuilder
					.query(bQueryBuilder)
					.fetchSource(includes, null)
					.sort("lastname.sort", SortOrder.ASC)
					.sort("name.sort", SortOrder.ASC)
					.from(start)
					.size(limit);

			//System.out.println(searchRequestBuilder.toString());
			String indexName = ElasticsearchUtil.getOrganizationIndexName(ESIndexNames.INDEX_USERS, orgId);
			SearchResponse sResponse = ElasticSearchConnector.getClient().search(searchSourceBuilder, indexName, ESIndexTypes.ORGANIZATION_USER);

			if (sResponse != null) {
				SearchHits searchHits = sResponse.getHits();
				response.setHitsNumber(searchHits.getTotalHits());

				if (searchHits != null) {
					for (SearchHit sh : searchHits) {
						response.addFoundNode(getUserDataFromSearchHit(sh));
					}
				}
			}

		} catch (Exception e1) {
			logger.error("Error", e1);
		}
		return response;
	}

	private BoolQueryBuilder getUserSearchQueryBuilder(String searchTerm) {
		BoolQueryBuilder bQueryBuilder = QueryBuilders.boolQuery();
		if (searchTerm != null && !searchTerm.isEmpty()) {
			QueryBuilder qb = QueryBuilders
					.queryStringQuery(ElasticsearchUtil.escapeSpecialChars(searchTerm.toLowerCase()) + "*")
					.defaultOperator(Operator.AND)
					.field("name").field("lastname");

			bQueryBuilder.filter(qb);
		}

		return bQueryBuilder;
	}

}
