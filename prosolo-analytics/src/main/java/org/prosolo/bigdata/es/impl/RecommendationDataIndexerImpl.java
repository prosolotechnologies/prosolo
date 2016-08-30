package org.prosolo.bigdata.es.impl;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.prosolo.bigdata.common.dal.pojo.MostActiveUsersForLearningGoal;
import org.prosolo.bigdata.common.enums.ESIndexTypes;
import org.prosolo.bigdata.es.AbstractESIndexer;
import org.prosolo.bigdata.es.RecommendationDataIndexer;
import org.prosolo.common.ESIndexNames;

/**
 * @author Zoran Jeremic Jun 2, 2015
 *
 */

public class RecommendationDataIndexerImpl extends AbstractESIndexer implements
		RecommendationDataIndexer, Serializable {
	private static Logger logger = Logger
			.getLogger(RecommendationDataIndexer.class.getName());

	@Override
	public void updateMostActiveUsersForLearningGoal(
			MostActiveUsersForLearningGoal counterObject) {
		try {

			XContentBuilder builder = XContentFactory.jsonBuilder()
					.startObject();
			builder.field("learninggoalid", counterObject.getLearninggoal());
			builder.field("date", counterObject.getDate());
			Set<Entry<Long, Long>> usersSet = counterObject.getUsers()
					.entrySet();
			long[] mostactiveusers = new long[10];
			int userindex = 0;
			builder.startArray("mostactiveusers");
			for (Entry<Long, Long> users : usersSet) {
				mostactiveusers[userindex] = users.getKey();
				userindex++;
				builder.startObject();
				builder.field("id", users.getKey());
				builder.field("score", users.getValue());
				builder.endObject();

			}
			builder.endArray();
			// builder.field("mostactiveusers",mostactiveusers);
			this.delete(String.valueOf(counterObject.getLearninggoal()),
					ESIndexNames.INDEX_RECOMMENDATION_DATA,
					ESIndexTypes.MOSTACTIVEUSERSFORLEARNINGGOAL);
			System.out.println("SHOULD INDEX:" + builder.string());
			this.indexDocument(builder,
					String.valueOf(counterObject.getLearninggoal()),
					ESIndexNames.INDEX_RECOMMENDATION_DATA,
					ESIndexTypes.MOSTACTIVEUSERSFORLEARNINGGOAL);
			// indexNode(builder, String.valueOf(resource.getId()),ES_INDEX,
			// indexType);
		} catch (IOException e) {
			logger.error(e);
		}

	}
}
