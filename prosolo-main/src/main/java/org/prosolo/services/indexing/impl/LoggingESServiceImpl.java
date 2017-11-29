package org.prosolo.services.indexing.impl;/**
 * Created by zoran on 28/09/16.
 */

import org.apache.log4j.Logger;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Client;
import org.json.simple.JSONObject;
import org.prosolo.bigdata.common.enums.ESIndexTypes;
import org.prosolo.common.ESIndexNames;
import org.prosolo.services.indexing.LoggingESService;
import org.springframework.stereotype.Service;

/**
 * zoran 28/09/16
 */
@Service("org.prosolo.services.indexing.LoggingESService")
public class LoggingESServiceImpl implements LoggingESService {
    private static Logger logger = Logger.getLogger(LoggingESService.class.getName());

    @Override
    public void storeEventObservedLog(JSONObject logObject){
        logger.debug("storing event observed log");
        try{
            String indexName = ESIndexNames.INDEX_LOGS;
            String indexType = ESIndexTypes.LOG;
            Client client = ElasticSearchFactory.getClient();
            IndexRequest logIndexRequest=new IndexRequest(indexName,indexType);
            logIndexRequest.source(logObject);
            client.index(logIndexRequest);
          }catch(Exception ex){
            logger.error(ex);
        }

        logger.debug("stored event observed log");
    }
}
