package org.prosolo.services.es.impl;
 
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.index.query.AndFilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.FilteredQueryBuilder;
import org.elasticsearch.index.query.OrFilterBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermFilterBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.prosolo.recommendation.impl.RecommendedDocument;
import org.prosolo.services.es.MoreDocumentsLikeThis;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.indexing.ESIndexNames;
import org.prosolo.services.indexing.ElasticSearchFactory;
import org.springframework.stereotype.Service;

@Service("org.prosolo.services.es.MoreDocumentsLikeThis")
public class MoreDocumentsLikeThisImpl extends AbstractManagerImpl implements
		MoreDocumentsLikeThis {
	
	private Logger logger = Logger.getLogger(MoreDocumentsLikeThisImpl.class);

	private static final long serialVersionUID = -7161733128379263294L;

	@SuppressWarnings("unused")
	private String moreDocumentsLikeThisFields = "file";

	@Override
	public List<String> findDocumentDuplicates(String likeText) {
		List<String> duplicates = new ArrayList<String>();

		try {
			Client client = ElasticSearchFactory.getClient();
		
			QueryBuilder qb = null;
			qb = QueryBuilders.moreLikeThisQuery("file", "url", "title").likeText(likeText).minTermFreq(0).minDocFreq(1).maxQueryTerms(1000000);
			SearchResponse sr = null;
			
			try {
				sr = client.prepareSearch(ESIndexNames.INDEX_DOCUMENTS)
						.setQuery(qb)
						.addFields("url", "title", "contentType", "uniqueness")
						.setFrom(0)
						.setSize(5)
						.execute()
						.actionGet();
			} catch (Exception ex) {
				logger.error("Error:" + ex.getLocalizedMessage());
				return duplicates;
			}
			
			if (sr != null) {
				SearchHits searchHits = sr.getHits();
				Iterator<SearchHit> hitsIter = searchHits.iterator();
				
				while (hitsIter.hasNext()) {
					SearchHit searchHit = hitsIter.next();
					if (searchHit.getScore() > 0.7) {
						if (searchHit.getFields().containsKey("uniqueness")) {
							duplicates.add(searchHit.getFields().get("uniqueness").getValue().toString());
						}
					}
				}
			}
		} catch (NoNodeAvailableException e) {
			logger.error(e);
		}
		return duplicates;
	}

	@Override
	public List<RecommendedDocument> getSuggestedDocumentsForLearningGoal(
			String likeText, long userId, int limit) {
		
		List<RecommendedDocument> foundDocs = new ArrayList<RecommendedDocument>();
		
		try {
			int size = limit * 2;
			List<String> uniquenessList = new ArrayList<String>();
		
			QueryBuilder qb = null;
			// create the query
			
			qb = QueryBuilders.moreLikeThisQuery("file","title")
					.likeText(likeText).minTermFreq(1).minDocFreq(1)
					.maxQueryTerms(1);
			
			TermFilterBuilder publicVisibilityTerm = FilterBuilders.termFilter("visibility", "public");
			TermFilterBuilder privateVisibilityTerm = FilterBuilders.termFilter("visibility", "private");
			TermFilterBuilder ownerIdTerm = FilterBuilders.termFilter("ownerId", userId);
	
			AndFilterBuilder andFilterBuilder = FilterBuilders.andFilter(privateVisibilityTerm, ownerIdTerm);
			// create OR filter
			OrFilterBuilder filterBuilder = FilterBuilders.orFilter(publicVisibilityTerm, andFilterBuilder);
			FilteredQueryBuilder filteredQueryBuilder = QueryBuilders.filteredQuery(qb, filterBuilder);
			Client client = ElasticSearchFactory.getClient();
			String indexName = ESIndexNames.INDEX_DOCUMENTS;
			
			SearchResponse sr = client.prepareSearch(indexName)
					.setQuery(filteredQueryBuilder)
					.addFields("url", "title", "contentType","uniqueness").setFrom(0)
					.setSize(size).setExplain(true).execute().actionGet();
			
			if (sr != null) {
				SearchHits searchHits = sr.getHits();
				Iterator<SearchHit> hitsIter = searchHits.iterator();
				
				while (hitsIter.hasNext()) {
					SearchHit searchHit = hitsIter.next();
					
					if (searchHit.getFields().containsKey("uniqueness")) {
						String uniqueness = searchHit.getFields().get("uniqueness").getValue().toString();
						
						if (!uniquenessList.contains(uniqueness)) {
							uniquenessList.add(uniqueness);
							RecommendedDocument recDoc = new RecommendedDocument(searchHit);
							foundDocs.add(recDoc);
						}
					}
				}
			}
		} catch (NoNodeAvailableException e) {
			logger.error(e);
		}
		return foundDocs;
	}

}
