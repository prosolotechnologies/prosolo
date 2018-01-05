package org.prosolo.common.elasticsearch;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.prosolo.common.domainmodel.general.BaseEntity;

import java.util.Map;

/**
 * @author Zoran Jeremic 2013-06-29
 *
 */
public interface AbstractESIndexer {

	void delete(String id, String indexName, String indexType);

	void indexNode(XContentBuilder builder, String indexId, String indexName, String indexType);

	String getIndexTypeForNode(BaseEntity resource);

	void deleteNodeFromES(BaseEntity resource);

	String getIndexNameForNode(BaseEntity resource);

	void partialUpdateByScript(String indexName, String indexType, String docId,
                               String script, Map<String, Object> scriptParams);

	void partialUpdate(String indexName, String indexType, String docId,
                       XContentBuilder partialDoc);

}
