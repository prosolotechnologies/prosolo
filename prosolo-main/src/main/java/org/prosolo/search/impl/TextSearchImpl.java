package org.prosolo.search.impl;

import org.prosolo.search.TextSearch;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.springframework.stereotype.Service;


/**
 * @author Zoran Jeremic
 * @date Jul 1, 2012
 */
@Deprecated
@Service("org.prosolo.search.TextSearch")
public class TextSearchImpl extends AbstractManagerImpl implements TextSearch {

	private static final long serialVersionUID = -8919953696206394473L;
	//private static Logger logger = Logger.getLogger(TextSearchImpl.class);
	
	//@Autowired private DefaultManager defaultManager;
	//@Autowired private ESIndexer esIndexer;
	
//	@Override
//	@Transactional
//	public TextSearchResponse searchActivities(
//			String searchString, int page, int limit, boolean loadOneMore,
//			long[] activitiesToExclude) {
//		
//		TextSearchResponse response = new TextSearchResponse();
//		
//		try {
//			int start = setStart(page, limit);
//			limit = setLimit(limit, loadOneMore);
//			
//			Client client = ElasticSearchFactory.getClient();
//			esIndexer.addMapping(client,ESIndexNames.INDEX_NODES, ESIndexTypes.ACTIVITY);
//			
//			QueryBuilder qb = QueryBuilders
//					.queryStringQuery(searchString.toLowerCase() + "*").useDisMax(true)
//					.defaultOperator(QueryStringQueryBuilder.Operator.AND)
//					.field("title");
//			
//			BoolQueryBuilder bQueryBuilder = QueryBuilders.boolQuery();
//			bQueryBuilder.should(qb);
//			
//			if (activitiesToExclude != null) {
//				for (long activityId : activitiesToExclude) {
//					bQueryBuilder.mustNot(termQuery("id", activityId));
//				}
//			}
//			
//			SearchResponse sResponse = client
//					.prepareSearch(ESIndexNames.INDEX_NODES)
//					.setTypes(ESIndexTypes.ACTIVITY)
//					.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
//					.setQuery(bQueryBuilder).setFrom(start).setSize(limit)
//					.addSort("title", SortOrder.ASC)
//					.setExplain(true).execute().actionGet();
//			
//			if (sResponse != null) {
//				response.setHitsNumber(sResponse.getHits().getTotalHits());
//				
//				for (SearchHit hit : sResponse.getHits()) {
//					Long id = ((Integer) hit.getSource().get("id")).longValue();
//					try {
//						Activity activity = defaultManager.get(Activity.class, id);
//						//activity = HibernateUtil.initializeAndUnproxy(activity);
//						if (activity != null) {
//							response.addFoundNode(activity);
//						}
//					} catch (ResourceCouldNotBeLoadedException e) {
//						logger.error("Activity was not found: " + id);
//					}
//				}
//			}
//		} catch (NoNodeAvailableException e1) {
//			logger.error(e1);
//		}
//		return response;
//	}

//	@Override
//	public List<Reminder> searchReminders(String searchString,
//			ReminderStatus status, int page, int limit, boolean loadOneMore) {
//		return null;
//	}
	
//	@Override
//	public TextSearchResponse searchTags(String searchQuery, int page, int limit,
//			boolean loadOneMore, Collection<Tag> tagsToExclude) {
//		
//		TextSearchResponse response = new TextSearchResponse();
//		
//		try {
//			int start = setStart(page, limit);
//			limit = setLimit(limit, loadOneMore);
//			Client client = ElasticSearchFactory.getClient();
//			esIndexer.addMapping(client, ESIndexNames.INDEX_NODES, ESIndexTypes.TAGS);
//			
//			QueryBuilder qb = QueryBuilders
//					.queryStringQuery(searchQuery.toLowerCase() + "*").useDisMax(true)
//					.defaultOperator(QueryStringQueryBuilder.Operator.AND)
//					.field("title");
//			
//			BoolQueryBuilder bQueryBuilder = QueryBuilders.boolQuery();
//			bQueryBuilder.should(qb);
//			
//			if (tagsToExclude != null) {
//				for (Tag tag :tagsToExclude) {
//					if (tag != null) {
//						bQueryBuilder.mustNot(termQuery("id", tag.getId()));
//					}
//				}
//			}
//			
//			SearchResponse sResponse = client
//					.prepareSearch(ESIndexNames.INDEX_NODES)
//					.setTypes(ESIndexTypes.TAGS)
//					.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
//					.setQuery(bQueryBuilder).setFrom(start).setSize(limit)
//					.addSort("title", SortOrder.ASC)
//					.setExplain(true).execute().actionGet();
//			
//			
//			if (sResponse != null) {
//				response.setHitsNumber(sResponse.getHits().getTotalHits());
//				
//				for (SearchHit hit : sResponse.getHits()) {
//					Long id = ((Integer) hit.getSource().get("id")).longValue();
//					try {
//						Tag annotation = defaultManager.loadResource(Tag.class, id);
//						
//						if (annotation != null) {
//							response.addFoundNode(annotation);
//						}
//					} catch (ResourceCouldNotBeLoadedException e) {
//						logger.error("Annotation was not found: " + id);
//					}
//				}
//			}
//		} catch (NoNodeAvailableException e1) {
//			logger.error(e1);
//		}
//		return response;
//	}
	
}
