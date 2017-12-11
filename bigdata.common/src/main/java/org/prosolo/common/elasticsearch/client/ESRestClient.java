package org.prosolo.common.elasticsearch.client;

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

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * @author stefanvuckovic
 * @date 2017-11-28
 * @since 1.2.0
 */
public interface ESRestClient {

    public boolean waitForGreenStatus() throws IOException;

    public UpdateResponse update(UpdateRequest updateRequest) throws IOException;

    public IndexResponse index(IndexRequest indexRequest) throws IOException;

    public DeleteResponse delete(DeleteRequest deleteRequest) throws IOException;

    public SearchResponse search(SearchRequest searchRequest) throws IOException;

    public SearchResponse search(SearchSourceBuilder searchSourceBuilder, String index, String type) throws IOException;

    boolean deleteIndex(String indexName) throws IOException;

    boolean exists(String indexName) throws IOException;

    boolean deleteByQuery(String indexName, String indexType, QueryBuilder qb) throws IOException;
}
