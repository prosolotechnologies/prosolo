package org.prosolo.common.elasticsearch.client.impl;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.elasticsearch.ElasticsearchException;
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
import org.elasticsearch.client.*;
import org.elasticsearch.cluster.health.ClusterHealthStatus;
import org.elasticsearch.common.io.Streams;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.*;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.index.mapper.Mapping;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.prosolo.bigdata.common.enums.ESIndexTypes;
import org.prosolo.common.ESIndexNames;
import org.prosolo.common.config.CommonSettings;
import org.prosolo.common.config.ElasticSearchConfig;
import org.prosolo.common.elasticsearch.client.ESRestClient;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

import static org.elasticsearch.client.Requests.putMappingRequest;
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
    public boolean deleteIndex(String... indexNames) throws IOException {
        try {
            highLevelClient.indices().deleteIndex(new DeleteIndexRequest(indexNames));
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
        logger.info("DELETE INDEX RESPONSE STATUS: " + status);
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
        logger.info("DELETE INDEX RESPONSE STATUS: " + status);
        return status == HttpStatus.SC_OK;
    }

    @Override
    public boolean createIndex(String indexName) throws IOException {
        ElasticSearchConfig elasticSearchConfig = CommonSettings.getInstance().config.elasticSearch;
        Settings.Builder elasticsearchSettings =  Settings.builder()
                .loadFromStream("index-analysis-settings.json", Streams.class.getResourceAsStream("/org/prosolo/services/indexing/index-analysis-settings.json"), false)
                //.put("http.enabled", "false")
                //.put("cluster.name", elasticSearchConfig.clusterName)
                .put("index.number_of_replicas",
                        elasticSearchConfig.replicasNumber)
                .put("index.number_of_shards",
                        elasticSearchConfig.shardsNumber);
        XContentBuilder jsonBuilder = XContentFactory.jsonBuilder().startObject("settings");
        XContentParser settingsParser = JsonXContent.jsonXContent
                .createParser(NamedXContentRegistry.EMPTY, elasticsearchSettings.build().toString());
        jsonBuilder.copyCurrentStructure(settingsParser);
        jsonBuilder.endObject();
        jsonBuilder.startObject("mappings");
        XContentParser mappingsParser = JsonXContent.jsonXContent
                .createParser(NamedXContentRegistry.EMPTY, getMappingString(indexName));
        jsonBuilder.copyCurrentStructure(mappingsParser);
        jsonBuilder.endObject();

        HttpEntity entity = new NStringEntity(jsonBuilder.string(), ContentType.APPLICATION_JSON);

        Response response = highLevelClient.getLowLevelClient().performRequest("PUT", "/" + indexName, Collections.emptyMap(), entity);

        int status = response.getStatusLine().getStatusCode();
        logger.info("CREATE INDEX RESPONSE STATUS: " + status);
        return status == HttpStatus.SC_OK;
    }

    private String getMappingString(String indexName) {
        if (indexName.startsWith(ESIndexNames.INDEX_CREDENTIALS)) {
           return getMappingString(ESIndexTypes.CREDENTIAL);
        } else if (indexName.startsWith(ESIndexNames.INDEX_USERS)) {
            if (indexName.equals(ESIndexNames.INDEX_USERS)) {
                return getMappingStringForType(ESIndexTypes.USER);
            } else {
                //index has organization suffix so it is organization user index
                return getMappingStringForType(ESIndexTypes.ORGANIZATION_USER);
            }
        } else if (indexName.startsWith(ESIndexNames.INDEX_USER_GROUP)) {
            return getMappingStringForType(ESIndexTypes.USER_GROUP);
        } else if (indexName.startsWith(ESIndexNames.INDEX_RUBRIC_NAME)) {
           return getMappingStringForType(ESIndexTypes.RUBRIC);
        } else if (indexName.equals(ESIndexNames.INDEX_ASSOCRULES)) {
            return getMappingStringForType(ESIndexTypes.COMPETENCE_ACTIVITIES);
        }
        return "";
    }

    private String getMappingStringForType(String indexType) {
        //temporary solution until we completely move to organization indexes
        String mappingPath = "/org/prosolo/services/indexing/" + indexType + "-mapping" + ".json";
        String mapping = null;

        try {
            mapping = copyToStringFromClasspath(mappingPath);
        } catch (IOException e1) {
            logger.error(e1);
        }
        return mapping;
    }

}
