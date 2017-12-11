package org.prosolo.common.elasticsearch.client.impl;

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
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.prosolo.common.elasticsearch.client.ESRestClient;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * @author stefanvuckovic
 * @date 2017-11-28
 * @since 1.2.0
 */
public class ESRestClientImpl implements ESRestClient {

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

//    public boolean deleteIndex(String indexName) throws IOException {
//        //TODO es migration - when migrated to 6.0, replace this implementation with delete index request which is supported in this release
//        Response response = lowLevelClient.performRequest("DELETE", indexName);
//    }

}
