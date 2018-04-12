package org.prosolo.common.elasticsearch.client.impl;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.log4j.Logger;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
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
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.cluster.health.ClusterHealthStatus;
import org.elasticsearch.common.io.Streams;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.prosolo.bigdata.common.enums.ESIndexTypes;
import org.prosolo.common.ESIndexNames;
import org.prosolo.common.config.CommonSettings;
import org.prosolo.common.config.ElasticSearchConfig;
import org.prosolo.common.elasticsearch.client.ESRestClient;
import org.prosolo.common.util.Pair;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

import static org.elasticsearch.client.Requests.createIndexRequest;
import static org.prosolo.common.util.ElasticsearchUtil.copyToStringFromClasspath;

/**
 * @author stefanvuckovic
 * @date 2017-11-28
 * @since 1.2.0
 */
public class ESRestClientImpl implements ESRestClient {

    private static Logger logger = Logger.getLogger(ESRestClientImpl.class);

    private final RestHighLevelClient highLevelClient;

    private ESRestClientImpl(RestHighLevelClient highLevelClient) {
        this.highLevelClient = highLevelClient;
    }

    public static ESRestClientImpl of(RestClientBuilder restClientBuilder) {
        return new ESRestClientImpl(new RestHighLevelClient(restClientBuilder));
    }

    @Override
    public boolean waitForGreenStatus() throws IOException {
        Response response = highLevelClient.getLowLevelClient().performRequest("GET", "/_cluster/health?wait_for_status=green");

        ClusterHealthStatus healthStatus;
        try (InputStream is = response.getEntity().getContent()) {
            Map<String, Object> map = XContentHelper.convertToMap(XContentType.JSON.xContent(), is, true);
            healthStatus = ClusterHealthStatus.fromString((String) map.get("status"));
        }

        return healthStatus == ClusterHealthStatus.GREEN;
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
    public boolean deleteIndex(String... indexNames) throws IOException {
        try {
            highLevelClient.indices().delete(new DeleteIndexRequest(indexNames));
            return true;
        } catch (ElasticsearchException e) {
            if (e.status() == RestStatus.NOT_FOUND) {
                logger.error("Index not deleted because it can't be found", e);
            }
            return false;
        }
    }

    @Override
    public boolean exists(String indexName) throws IOException {
        Response response = highLevelClient.getLowLevelClient().performRequest("HEAD", "/" + indexName);
        int status = response.getStatusLine().getStatusCode();
        logger.info("INDEX EXISTS RESPONSE STATUS: " + status);
        return status == HttpStatus.SC_OK;
    }

    @Override
    public boolean deleteByQuery(String indexName, String indexType, QueryBuilder qb) throws IOException {
        HttpEntity entity = new NStringEntity(qb.toString(), ContentType.APPLICATION_JSON);
        Response response = highLevelClient.getLowLevelClient().performRequest(
                "POST",
                "/" + indexName + "/" + indexType + "/" + "_delete_by_query",
                Collections.emptyMap(),
                entity);
        int status = response.getStatusLine().getStatusCode();
        logger.info("DELETE BY QUERY RESPONSE STATUS: " + status);
        return status == HttpStatus.SC_OK;
    }

    @Override
    public boolean createIndex(String indexName) throws IOException {
        if (!exists(indexName)) {
            ElasticSearchConfig elasticSearchConfig = CommonSettings.getInstance().config.elasticSearch;
            Settings.Builder elasticsearchSettings = Settings.builder()
                    .loadFromStream("index-analysis-settings.json", Streams.class.getResourceAsStream("/org/prosolo/common/elasticsearch/mappings/index-analysis-settings.json"), false)
                    //.put("http.enabled", "false")
                    //.put("cluster.name", elasticSearchConfig.clusterName)
                    .put("index.number_of_replicas",
                            elasticSearchConfig.replicasNumber)
                    .put("index.number_of_shards",
                            elasticSearchConfig.shardsNumber);

            Pair<String, String> mapping = getMappingTypeAndContent(indexName);
            CreateIndexRequest req = createIndexRequest(indexName).settings(elasticsearchSettings);
            if (mapping != null) {
                req.mapping(mapping.getFirst(), mapping.getSecond(), XContentType.JSON);
            }
            CreateIndexResponse response = highLevelClient.indices().create(req);
            return response.isAcknowledged();
        }
        return false;
    }

    private Pair<String, String> getMappingTypeAndContent(String indexName) {
        String type = null;
        String content = null;
        if (indexName.startsWith(ESIndexNames.INDEX_CREDENTIALS)) {
            type = ESIndexTypes.CREDENTIAL;
            content = getMappingStringForType(type);
        } else if (indexName.startsWith(ESIndexNames.INDEX_COMPETENCES)) {
            type = ESIndexTypes.COMPETENCE;
            content = getMappingStringForType(type);
        } else if (indexName.startsWith(ESIndexNames.INDEX_USERS)) {
            if (indexName.equals(ESIndexNames.INDEX_USERS)) {
                type = ESIndexTypes.USER;
                content = getMappingStringForType(type);
            } else {
                //index has organization suffix so it is organization user index
                type = ESIndexTypes.ORGANIZATION_USER;
                content = getMappingStringForType(type);
            }
        } else if (indexName.startsWith(ESIndexNames.INDEX_USER_GROUP)) {
            type = ESIndexTypes.USER_GROUP;
            content = getMappingStringForType(type);
        } else if (indexName.startsWith(ESIndexNames.INDEX_RUBRIC_NAME)) {
            type = ESIndexTypes.RUBRIC;
            content = getMappingStringForType(type);
        } else if (indexName.startsWith(ESIndexNames.INDEX_EVIDENCE)) {
            type = ESIndexTypes.EVIDENCE;
            content = getMappingStringForType(type);
        } else if (indexName.equals(ESIndexNames.INDEX_ASSOCRULES)) {
            type = ESIndexTypes.COMPETENCE_ACTIVITIES;
            content = getMappingStringForType(type);
        }
        return type != null && content != null ? new Pair<>(type, content) : null;
    }

    private String getMappingStringForType(String indexType) {
        //temporary solution until we completely move to organization indexes
        String mappingPath = "/org/prosolo/common/elasticsearch/mappings/" + indexType + "-mapping" + ".json";
        String mapping = null;

        try {
            mapping = copyToStringFromClasspath(mappingPath);
        } catch (IOException e1) {
            logger.error("Error", e1);
        }
        return mapping;
    }

}
