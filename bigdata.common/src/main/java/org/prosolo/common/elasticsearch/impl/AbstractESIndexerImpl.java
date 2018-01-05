package org.prosolo.common.elasticsearch.impl;

import org.apache.log4j.Logger;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.prosolo.bigdata.common.enums.ESIndexTypes;
import org.prosolo.common.ESIndexNames;
import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.rubric.Rubric;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.UserGroup;
import org.prosolo.common.elasticsearch.AbstractESIndexer;
import org.prosolo.common.elasticsearch.client.ESRestClient;
import org.prosolo.common.elasticsearch.ElasticSearchConnector;
import org.prosolo.common.util.ElasticsearchUtil;

import java.io.IOException;
import java.util.Map;

import static org.elasticsearch.client.Requests.putMappingRequest;
import static org.prosolo.common.util.ElasticsearchUtil.copyToStringFromClasspath;

//import static org.elasticsearch.common.io.Streams.copyToStringFromClasspath;

/**
 * @author Zoran Jeremic May 9, 2015
 *
 */

public class AbstractESIndexerImpl implements AbstractESIndexer {
	private static Logger logger = Logger.getLogger(AbstractESIndexerImpl.class
			.getName());

	@Override
	public void delete(String id, String indexName, String indexType) {
		DeleteResponse deleteResponse;
		try {
			deleteResponse = ElasticSearchConnector.getClient()
					.delete(new DeleteRequest(indexName, indexType, id));

			if (deleteResponse.getResult() == DocWriteResponse.Result.NOT_FOUND) {
				logger.error("Entity " + id
						+ " was not deleted as it was not found");
			}
		} catch (ElasticsearchException|IOException e) {
			logger.error("Error", e);
		}
	}

	//TODO es this method should be removed - there is no reason to have generic delete method that would accept any domain object
	@Override
	public void deleteNodeFromES(BaseEntity node) {
		String id = String.valueOf(node.getId());
		logger.debug("deleting node id:" + id + " title:" + node.getTitle());
		String indexType = getIndexTypeForNode(node);
		String indexName = getIndexNameForNode(node);
		delete(id, indexName, indexType);
	}

	@Override
	public void indexNode(XContentBuilder builder, String indexId,
						  String indexName, String indexType) {
		try {
			IndexRequest indexReq = new IndexRequest(indexName, indexType, indexId).source(builder);
			ESRestClient client = ElasticSearchConnector.getClient();
			//TODO es migration - is mapping necessary
			//addMapping(client, indexName, indexType);
			client.index(indexReq);
			logger.info("Saving document for index name: " + indexName + ", indexType " + indexType + ", document id: " + indexId);
		} catch (IOException e) {
			logger.error("Error", e);
		}
	}

	@Override
	public void partialUpdate(String indexName, String indexType, String docId, 
			XContentBuilder partialDoc) {
		try {
			logger.info("Partial update for index name: " + indexName + ", indexType " + indexType + ", document id: " + docId);
			UpdateRequest updateRequest = new UpdateRequest(indexName, indexType, docId)
			        .doc(partialDoc);
			updateRequest.retryOnConflict(5);
			ElasticSearchConnector.getClient().update(updateRequest);
		} catch (Exception e) {
			logger.error("Error", e);
		}
	}

	@Override
	public void partialUpdateByScript(String indexName, String indexType, String docId,
									  String script, Map<String, Object> scriptParams) {
		try {
			UpdateRequest updateRequest = new UpdateRequest(indexName, indexType, docId);
			if(scriptParams != null) {
//				for(Entry<String, Object> param : scriptParams.entrySet()) {
//					updateRequest.addScriptParam(param.getKey(), param.getValue());
//				}
				updateRequest.script(new Script(ScriptType.INLINE, ElasticsearchUtil.DEFAULT_SCRIPT_LANG, script, scriptParams));
				//updateRequest.scriptParams(scriptParams);
				updateRequest.retryOnConflict(5);
			}
			ElasticSearchConnector.getClient().update(updateRequest);
		} catch(Exception e) {
			logger.error("Error", e);
		}
	}

	@Override
	public String getIndexTypeForNode(BaseEntity node) {
		String indexType = null;

		if (node instanceof User) {
			indexType = ESIndexTypes.USER;
		} else if (node instanceof Credential1) {
			indexType = ESIndexTypes.CREDENTIAL;
		} else if (node instanceof Competence1) {
			indexType = ESIndexTypes.COMPETENCE;
		} else if (node instanceof UserGroup) {
			indexType = ESIndexTypes.USER_GROUP;
		}
		return indexType;
	}

	@Override
	public String getIndexNameForNode(BaseEntity node) {
		String indexName = null;
		if (node instanceof User) {
			indexName = ESIndexNames.INDEX_USERS;
		} else if(node instanceof UserGroup) {
			indexName = ESIndexNames.INDEX_USER_GROUP;
		} else if (node instanceof Rubric) {
			indexName = ESIndexNames.INDEX_RUBRIC_NAME;
		} else if (node instanceof Credential1) {
			indexName = ESIndexNames.INDEX_CREDENTIALS;
		} else if (node instanceof Competence1) {
			indexName = ESIndexNames.INDEX_COMPETENCES;
		}
		return indexName;
	}
}
