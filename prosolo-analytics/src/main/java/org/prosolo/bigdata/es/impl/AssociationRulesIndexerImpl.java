package org.prosolo.bigdata.es.impl;

import org.apache.log4j.Logger;
import org.elasticsearch.action.deletebyquery.DeleteByQueryAction;
import org.elasticsearch.action.deletebyquery.DeleteByQueryRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.prosolo.bigdata.algorithms.fpgrowth.association_rules.AssocRule;
import org.prosolo.bigdata.common.enums.ESIndexTypes;
import org.prosolo.bigdata.es.AbstractESIndexer;
import org.prosolo.bigdata.es.AssociationRulesIndexer;
import org.prosolo.bigdata.es.ElasticSearchConnector;
import org.prosolo.common.ESIndexNames;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

//import java.util.Set;
//import org.prosolo.bigdata.config.Settings;
//import org.prosolo.services.indexing.ESIndexNames;
//import org.prosolo.services.indexing.ElasticSearchFactory;

/**
 * @author Zoran Jeremic May 9, 2015
 *
 */

public class AssociationRulesIndexerImpl extends AbstractESIndexer implements
		AssociationRulesIndexer, Serializable {
	private static Logger logger = Logger
			.getLogger(AssociationRulesIndexerImpl.class.getName());

	// public static String
	// INDEX_TYPE=CommonSettings.getInstance().config.elasticSearch.associationrulesIndex;
	@Override
	public void saveAssociationRulesForCompetence(long competenceid,
			AssocRule assocRule) {
		try {

			XContentBuilder builder = XContentFactory.jsonBuilder()
					.startObject();
			builder.field("id", competenceid);
			builder.field("support", assocRule.getAbsoluteSupport());
			builder.field("confidence", assocRule.getConfidence());
			long[] lhsarray = assocRule.getItemset1();
			builder.field("itemset1_size", lhsarray.length);
			builder.startArray("itemset1");

			for (int i = 0; i < lhsarray.length; i++) {
				builder.startObject();
				builder.field("id", lhsarray[i]);
				builder.endObject();
			}

			builder.endArray();
			builder.startArray("itemset2");
			long[] rhsarray = assocRule.getItemset2();
			for (int i = 0; i < rhsarray.length; i++) {
				builder.startObject();
				builder.field("id", rhsarray[i]);
				builder.endObject();
			}
			builder.endArray();
			builder.endObject();
			this.indexDocument(builder, null, ESIndexNames.INDEX_ASSOCRULES,
					ESIndexTypes.COMPETENCE_ACTIVITIES);
			// indexNode(builder, String.valueOf(resource.getId()),ES_INDEX,
			// indexType);
		} catch (IOException e) {
			logger.error(e);
		}
	}

	@Override
	public void saveFrequentCompetenceActivities(long competenceid,
			List<Long> activities) {
		XContentBuilder builder;
		try {
			builder = XContentFactory.jsonBuilder().startObject();
			builder.field("id", competenceid);
			builder.startArray("activities");
			for (Long activity : activities) {
				builder.startObject();
				builder.field("id", activity);
				builder.endObject();
			}
			builder.endArray();
			builder.endObject();
			this.indexDocument(builder, String.valueOf(competenceid),
					ESIndexNames.INDEX_RECOMMENDATION_DATA,
					ESIndexTypes.FREQ_COMPETENCE_ACTIVITIES);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void deleteAssociationRulesForCompetence(long competenceid) {
		TermQueryBuilder termCompetence = QueryBuilders.termQuery("id",
				competenceid);
		QueryBuilder boolQuery = QueryBuilders.boolQuery().must(termCompetence);
		Client client = null;
		//try {
			client = ElasticSearchConnector.getClient();
		//} catch (IndexingServiceNotAvailable e) {
			// TODO Auto-generated catch block
		//	e.printStackTrace();
		//}
		String indexName = ESIndexNames.INDEX_ASSOCRULES;
		String indexType = ESIndexTypes.COMPETENCE_ACTIVITIES;

		// THIS IS REMOVED FOR TRANSFER TO 2.3");
		//client.prepareDeleteByQuery(indexName).setQuery(boolQuery)
		//		.setTypes(indexType).execute().actionGet();
		DeleteByQueryRequestBuilder requestBuilder=new DeleteByQueryRequestBuilder(client, DeleteByQueryAction.INSTANCE);
		requestBuilder.setQuery(boolQuery).execute().actionGet();


	}

}
