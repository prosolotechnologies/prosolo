package org.prosolo.bigdata.es.impl;

import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.IndexingServiceNotAvailable;
import org.prosolo.bigdata.es.ESAdministration;
import org.prosolo.common.ESIndexNames;
import org.prosolo.common.elasticsearch.ElasticSearchConnector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//import static org.elasticsearch.common.io.Streams.copyToStringFromClasspath;
//import org.prosolo.bigdata.config.Settings;

/**
 * @author Zoran Jeremic May 9, 2015
 *
 */

public class ESAdministrationImpl implements ESAdministration {
	private static final long serialVersionUID = 830150223713546004L;
	private static Logger logger = Logger.getLogger(ESAdministrationImpl.class);

	@Override
	public boolean createIndexes() throws IndexingServiceNotAvailable {
		//List<String> indexes = ESIndexNames.getAllIndexes();
		List<String> indexes = new ArrayList<String>();
		indexes.add(ESIndexNames.INDEX_JOBS_LOGS);
		indexes.add(ESIndexNames.INDEX_RECOMMENDATION_DATA);
		indexes.add(ESIndexNames.INDEX_LOGS);
		indexes.add(ESIndexNames.INDEX_ASSOCRULES);

		try {
			for (String index : indexes) {
				ElasticSearchConnector.getClient().createIndex(index);
			}
		} catch (IOException e) {
			logger.error("Error", e);
		}
		return true;
	}

	@Override
	public boolean deleteIndexes() throws IndexingServiceNotAvailable {
		List<String> indexes = ESIndexNames.getAllIndexes();
		try {
			ElasticSearchConnector.getClient().deleteIndex(indexes.toArray(new String[indexes.size()]));
		} catch(Exception e) {
			logger.error("Error", e);
		}
		return true;
	}

}
