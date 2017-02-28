package org.prosolo.search.impl;

import static org.elasticsearch.index.query.QueryBuilders.termQuery;

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
import org.elasticsearch.search.sort.SortOrder;
import org.prosolo.bigdata.common.enums.ESIndexTypes;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.ESIndexNames;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.search.CompetenceTextSearch;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.indexing.ESIndexer;
import org.prosolo.services.indexing.ElasticSearchFactory;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.data.CompetenceData1;
import org.prosolo.services.nodes.data.Role;
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
