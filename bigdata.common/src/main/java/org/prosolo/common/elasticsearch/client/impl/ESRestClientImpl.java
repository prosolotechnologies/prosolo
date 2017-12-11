package org.prosolo.common.elasticsearch.client.impl;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.cluster.health.ClusterHealthStatus;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.prosolo.common.elasticsearch.client.ESRestClient;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

/**
 * @author stefanvuckovic
 * @date 2017-11-28
 * @since 1.2.0
 */
public class ESRestClientImpl implements ESRestClient {

    private static Logger logger = Logger.getLogger(ESRestClientImpl.class);

    private final RestHighLevelClient highLevelClient;
    private final RestClient lowLevelClient;

    private ESRestClientImpl(RestClient lowLevelClient, RestHighLevelClient highLevelClient) {
        this.highLevelClient = highLevelClient;
        this.lowLevelClient = lowLevelClient;
    }

    public static ESRestClientImpl of(RestClient lowLevelClient) {
        return new ESRestClientImpl(lowLevelClient, new RestHighLevelClient(lowLevelClient));
    }

    @Override
    public boolean waitForGreenStatus() throws IOException {
        Response response = lowLevelClient.performRequest("GET", "/_cluster/health?wait_for_status=green");

        ClusterHealthStatus healthStatus;
        try (InputStream is = response.getEntity().getContent()) {
            Map<String, Object> map = XContentHelper.convertToMap(XContentType.JSON.xContent(), is, true);
            healthStatus = ClusterHealthStatus.fromString((String) map.get("status"));
        }

        if (healthStatus == ClusterHealthStatus.GREEN) {
            return true;
        }
        return false;
    }

    @Override
    public UpdateResponse update(UpdateRequest updateRequest) throws IOException {
        return highLevelClient.update(updateRequest);
    }

    @Override
    public IndexResponse index(IndexRequest indexRequest) throws IOException {
        return highLevelClient.index(indexRequest);
    }

    @Override
    public DeleteResponse delete(DeleteRequest deleteRequest) throws IOException {
        return highLevelClient.delete(deleteRequest);
    }

    @Override
    public SearchResponse search(SearchRequest searchRequest) throws IOException {
        return highLevelClient.search(searchRequest);
    }

    @Override
    public SearchResponse search(SearchSourceBuilder searchSourceBuilder, String index, String type) throws IOException {
        SearchRequest sr = new SearchRequest(index);
        if (type != null) {
            sr.types(type);
        }
        sr.source(searchSourceBuilder);
        //this search type is used in all places - if needed, it can be passed as an argument
        sr.searchType(SearchType.DFS_QUERY_THEN_FETCH);
        return search(sr);
    }

    @Override
    public boolean deleteIndex(String indexName) throws IOException {
        //TODO es migration - when migrated to 6.0, replace this implementation with delete index request which is supported in this release
        Response response = lowLevelClient.performRequest("DELETE", "/" + indexName);
        int status = response.getStatusLine().getStatusCode();
        logger.info("DELETE INDEX RESPONSE STATUS: " + status);
        String res = EntityUtils.toString(response.getEntity());
        logger.info("DELETE INDEX RESPONSE BODY: " + res);
        return status == HttpStatus.SC_OK;
    }

    @Override
    public boolean exists(String indexName) throws IOException {
        Response response = lowLevelClient.performRequest("HEAD", "/" + indexName);
        int status = response.getStatusLine().getStatusCode();
        logger.info("DELETE INDEX RESPONSE STATUS: " + status);
        return status == HttpStatus.SC_OK;
    }

    @Override
    public boolean deleteByQuery(String indexName, String indexType, QueryBuilder qb) throws IOException {
        HttpEntity entity = new NStringEntity(qb.toString(), ContentType.APPLICATION_JSON);
        Response response = lowLevelClient.performRequest(
                "POST",
                "/" + indexName + "/" + indexType + "/" + "_delete_by_query",
                Collections.emptyMap(),
                entity);
        int status = response.getStatusLine().getStatusCode();
        logger.info("DELETE INDEX RESPONSE STATUS: " + status);
        return status == HttpStatus.SC_OK;
    }

}
