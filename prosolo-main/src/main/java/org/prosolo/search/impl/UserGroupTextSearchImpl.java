package org.prosolo.search.impl;

import org.apache.log4j.Logger;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.prosolo.bigdata.common.enums.ESIndexTypes;
import org.prosolo.common.ESIndexNames;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.elasticsearch.ElasticSearchConnector;
import org.prosolo.common.util.ElasticsearchUtil;
import org.prosolo.search.UserGroupTextSearch;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.nodes.data.BasicObjectInfo;
import org.prosolo.services.nodes.data.ResourceVisibilityMember;
import org.prosolo.services.user.UserGroupManager;
import org.prosolo.services.user.data.UserGroupData;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.elasticsearch.index.query.QueryBuilders.termQuery;

//import org.elasticsearch.index.query.BoolQueryBuilder;
//import org.elasticsearch.index.query.NestedFilterBuilder;


/**
 * 
 * @author stefanvuckovic
 *
 */
@Service("org.prosolo.search.UserGroupTextSearch")
public class UserGroupTextSearchImpl extends AbstractManagerImpl implements UserGroupTextSearch {

	private static final long serialVersionUID = 4773327429040878955L;

	private static Logger logger = Logger.getLogger(UserGroupTextSearchImpl.class);
	
	private static final int maxResults = 1000;
	
	//@Inject private ESIndexer esIndexer;
	@Inject private UserGroupManager userGroupManager;
	
	private int setStart(int page, int limit){
		int start = 0;
		if (page >= 0 && limit > 0) {
			start = page * limit;
		}
		return start;
	}

	@Override
	public PaginatedResult<UserGroupData> searchUserGroups (
			long orgId, long unitId, String searchString, int page, int limit) {
		
		PaginatedResult<UserGroupData> response = new PaginatedResult<>();
		
		try {
			List<Long> unitIds = Arrays.asList(unitId);
			SearchResponse sResponse = getUserGroupsSearchResponse(orgId, unitIds, searchString, page, limit);
	
			if (sResponse != null) {
				response.setHitsNumber(sResponse.getHits().getTotalHits());

				for (SearchHit hit : sResponse.getHits()) {
					logger.info("ID: " + hit.getSourceAsMap().get("id"));
					long id = Long.parseLong(hit.getSourceAsMap().get("id").toString());
					UserGroupData group = userGroupManager.getUserGroupDataWithUserCountAndCanBeDeletedInfo(id);
					response.addFoundNode(group);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
		}
		return response;
	}

	@Override
	public PaginatedResult<BasicObjectInfo> searchUserGroupsAndReturnBasicInfo(
			long orgId, long unitId, String searchString, int page, int limit) {

		PaginatedResult<BasicObjectInfo> response = new PaginatedResult<>();

		try {
			List<Long> unitIds = Arrays.asList(unitId);
			SearchResponse sResponse = getUserGroupsSearchResponse(orgId, unitIds, searchString, page, limit);

			if (sResponse != null) {
				response.setHitsNumber(sResponse.getHits().getTotalHits());

				for (SearchHit hit : sResponse.getHits()) {
					long id = Long.parseLong(hit.getSourceAsMap().get("id").toString());
					String name = (String) hit.getSourceAsMap().get("name");
					BasicObjectInfo group = new BasicObjectInfo(id, name);
					response.addFoundNode(group);
				}
			}
		} catch (Exception e) {
			logger.error("error", e);
		}
		return response;
	}
	
	private SearchResponse getUserGroupsSearchResponse(long orgId, List<Long> unitIds, String searchString, int page, int limit) throws IOException {
		int start = 0;
		int size = maxResults;
		if(limit > 0) {
			start = setStart(page, limit);
			size = limit;
		}
		
		QueryBuilder qb = QueryBuilders
				.queryStringQuery(ElasticsearchUtil.escapeSpecialChars(searchString.toLowerCase()) + "*")
				.defaultOperator(Operator.AND)
				.field("name");
		
		BoolQueryBuilder bQueryBuilder = QueryBuilders.boolQuery();
		bQueryBuilder.filter(qb);

		if (unitIds != null && !unitIds.isEmpty()) {
			BoolQueryBuilder unitFilter = QueryBuilders.boolQuery();
			for (long unitId : unitIds) {
				unitFilter.should(termQuery("unit", unitId));
			}
			bQueryBuilder.filter(unitFilter);
		}

		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder
				.query(bQueryBuilder)
				.from(start)
				.size(size)
				.sort(new FieldSortBuilder("name.sort").order(SortOrder.ASC));
		return ElasticSearchConnector.getClient().search(searchSourceBuilder, ElasticsearchUtil.getOrganizationIndexName(ESIndexNames.INDEX_USER_GROUP, orgId), ESIndexTypes.USER_GROUP);
	}
	
	@Override
	public PaginatedResult<ResourceVisibilityMember> searchUsersAndGroups(
			long orgId, String searchTerm, int limit, List<Long> usersToExclude, List<Long> groupsToExclude, long roleId,
			List<Long> unitIds) {
		PaginatedResult<ResourceVisibilityMember> response = new PaginatedResult<>();
		try {
			if (unitIds == null || unitIds.isEmpty()) {
				return response;
			}

			SearchHit[] userHits = getResourceVisibilityUsers(orgId, searchTerm, limit, usersToExclude, roleId,
					unitIds);
			SearchHit[] groupHits = getUserGroups(orgId, searchTerm, limit, groupsToExclude, unitIds);
			
			int userLength = userHits.length, groupLength = groupHits.length;
			int groupNumber = limit / 2 < groupLength ? limit / 2 : groupLength; 
			int userNumber = limit - groupNumber < userLength ? limit - groupNumber : userLength;
			if(groupNumber + userNumber < limit) {
				groupNumber = limit - userNumber < groupLength ? limit - userNumber : groupLength;
			}
			for(int i = 0; i < groupNumber; i++) {
				SearchHit hit = groupHits[i];
				long id = Long.parseLong(hit.getSourceAsMap().get("id").toString());
				String name = (String) hit.getSourceAsMap().get("name");
				long userCount = userGroupManager.getNumberOfUsersInAGroup(id);
				response.addFoundNode(new ResourceVisibilityMember(0, id, name, userCount, null, false, false));
			}
			for(int i = 0; i < userNumber; i++) {
				SearchHit hit = userHits[i];
				response.addFoundNode(extractVisibilityUserResult(hit));
			}
//			if(userHits.length > 0 || groupHits.length > 0) {
//				int userInd = 0, userEnd = userHits.length - 1, groupInd = 0, groupEnd = groupHits.length - 1, counter = 0;
//				//all results count from both lists
//				int resNumber = userEnd + 1 + groupEnd + 1;
//				int finalResNumber = limit < resNumber ? limit : resNumber;
//				while(counter < finalResNumber) {
//					if(groupInd > groupEnd || (userInd <= userEnd && userHits[userInd].getScore() >= groupHits[groupInd].getScore())) {
//						SearchHit hit = userHits[userInd++];
//						response.addFoundNode(extractCredentialUserResult(hit));
//					} else {
//						SearchHit hit = groupHits[groupInd++];
//						long id = Long.parseLong(hit.getSource().get("id").toString());
//						String name = (String) hit.getSource().get("name");
//						long userCount = userGroupManager.getNumberOfUsersInAGroup(id);
//						response.addFoundNode(new ResourceVisibilityMember(0, id, name, userCount, null, false));
//					}
//					counter++;
//				}
//			}
		} catch (Exception e1) {
			e1.printStackTrace();
			logger.error(e1);
		}
		return response;
	}

	@Override
	public PaginatedResult<ResourceVisibilityMember> searchUsersInUnitsWithRole(long orgId, String searchTerm,
																		 int limit, List<Long> unitIds,
																		 List<Long> usersToExclude, long roleId) {
		PaginatedResult<ResourceVisibilityMember> response = new PaginatedResult<>();

		SearchHit[] userHits = getResourceVisibilityUsers(orgId, searchTerm, limit, usersToExclude, roleId, unitIds);
		for(int i = 0; i < userHits.length; i++) {
			SearchHit hit = userHits[i];
			response.addFoundNode(extractVisibilityUserResult(hit));
		}

		return response;
	}
	
	private SearchHit[] getResourceVisibilityUsers(long orgId, String searchTerm, int limit,
			List<Long> usersToExclude, long roleId, List<Long> unitIds) {
		try {
			if (unitIds == null || unitIds.isEmpty()) {
				return new SearchHit[0];
			}
			
			//search users
			BoolQueryBuilder bQueryBuilder = QueryBuilders.boolQuery();
			if(searchTerm != null && !searchTerm.isEmpty()) {
				QueryBuilder qb = QueryBuilders
						.queryStringQuery(ElasticsearchUtil.escapeSpecialChars(searchTerm.toLowerCase()) + "*")
						.defaultOperator(Operator.AND)
						.field("name").field("lastname");
				
				bQueryBuilder.must(qb);
			}
			
			bQueryBuilder.mustNot(termQuery("system", true));

			BoolQueryBuilder unitRoleFilter = QueryBuilders.boolQuery();
			unitRoleFilter.filter(termQuery("roles.id", roleId));
			BoolQueryBuilder unitFilter = QueryBuilders.boolQuery();
			for (long unitId : unitIds) {
				unitFilter.should(termQuery("roles.units.id", unitId));
			}
			unitRoleFilter.filter(unitFilter);

			NestedQueryBuilder nestedFilter = QueryBuilders.nestedQuery("roles", unitRoleFilter, ScoreMode.None);
			bQueryBuilder.filter(nestedFilter);
			
			if (usersToExclude != null) {
				for (Long exUserId : usersToExclude) {
					bQueryBuilder.mustNot(termQuery("id", exUserId));
				}
			}

			String[] includes = {"id", "name", "lastname", "avatar", "position"};
			SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
			searchSourceBuilder
					.query(bQueryBuilder)
					.size(limit)
					.fetchSource(includes, null)
					.sort(new FieldSortBuilder("lastname.sort").order(SortOrder.ASC))
					.sort(new FieldSortBuilder("name.sort").order(SortOrder.ASC));

			//System.out.println(searchRequestBuilder.toString());
			SearchResponse userResponse = ElasticSearchConnector.getClient().search(searchSourceBuilder, ElasticsearchUtil.getOrganizationIndexName(ESIndexNames.INDEX_USERS, orgId), ESIndexTypes.ORGANIZATION_USER);

			SearchHit[] userHits = null;
			if (userResponse != null) {
				SearchHits hits = userResponse.getHits();
				if(hits != null) {
					userHits = hits.getHits();
				}
			}
			if(userHits == null) {
				userHits = new SearchHit[0];
			}
			return userHits;
		} catch (Exception e1) {
			logger.error("Error", e1);
		}
		return new SearchHit[0];
	}
	
	private SearchHit[] getUserGroups(long orgId, String searchTerm, int limit, List<Long> groupsToExclude,
									  List<Long> unitIds) {
		try {
			if (unitIds == null || unitIds.isEmpty()) {
				return new SearchHit[0];
			}

			QueryBuilder qb = QueryBuilders
					.queryStringQuery(ElasticsearchUtil.escapeSpecialChars(searchTerm.toLowerCase()) + "*")
					.defaultOperator(Operator.AND)
					.field("name")
					/*
					rewrite is used for multi term queries like query string query with wildcard. By default
					when wildcard term is rewritten scores are not computed but each matched document receives constant
					score. When changed to top_terms_N, it does compute score for top N terms.

					There is no particular reason why N is set to 50. Alternative for this rewrite method would be to use
					'scoring_boolean' rewrite which would compute scores for all terms matched by the wildcard which can lead to
					exceptions
					 */
					.rewrite("top_terms_50");
			
			BoolQueryBuilder bqBuilder = QueryBuilders.boolQuery();
			bqBuilder.must(qb);

			BoolQueryBuilder unitFilter = QueryBuilders.boolQuery();
			for (long unitId : unitIds) {
				unitFilter.should(termQuery("unit", unitId));
			}
			bqBuilder.filter(unitFilter);
			
			if (groupsToExclude != null) {
				for (Long g : groupsToExclude) {
					bqBuilder.mustNot(termQuery("id", g));
				}
			}

			SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
			searchSourceBuilder
					.query(bqBuilder)
					.size(limit)
					.sort("_score", SortOrder.DESC)
					.sort(new FieldSortBuilder("name.sort").order(SortOrder.ASC));
			SearchResponse groupResponse = ElasticSearchConnector.getClient().search(searchSourceBuilder, ElasticsearchUtil.getOrganizationIndexName(ESIndexNames.INDEX_USER_GROUP, orgId), ESIndexTypes.USER_GROUP);

			SearchHit[] groupHits = null;
			if (groupResponse != null) {
				SearchHits hits = groupResponse.getHits();
				if(hits != null) {
					groupHits = hits.getHits();
				}
			}
			if (groupHits == null) {
				groupHits = new SearchHit[0];
			}
			
			return groupHits;
		} catch (Exception e1) {
			e1.printStackTrace();
			logger.error(e1);
		}
		return new SearchHit[0];
	}
	
	private ResourceVisibilityMember extractVisibilityUserResult(SearchHit hit) {
		Map<String, Object> fields = hit.getSourceAsMap();
		User user = new User();
		user.setId(Long.parseLong(fields.get("id") + ""));
		user.setName((String) fields.get("name"));
		user.setLastname((String) fields.get("lastname"));
		user.setAvatarUrl((String) fields.get("avatar"));
		user.setPosition((String) fields.get("position"));
		return new ResourceVisibilityMember(0, user, null, false, false);
	}
	
	@Override
	public PaginatedResult<ResourceVisibilityMember> searchVisibilityUsers(long orgId, String searchTerm,
                                                                           int limit, List<Long> unitIds,
																		   List<Long> usersToExclude) {
		PaginatedResult<ResourceVisibilityMember> response = new PaginatedResult<>();
		SearchHit[] userHits = getResourceVisibilityUsers(orgId, searchTerm, limit, usersToExclude, 0,
				unitIds);
			
		for(SearchHit h : userHits) {
			response.addFoundNode(extractVisibilityUserResult(h));
		}
			
		return response;
	}
	
}
