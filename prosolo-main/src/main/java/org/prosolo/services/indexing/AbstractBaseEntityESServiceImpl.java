package org.prosolo.services.indexing;

import java.io.IOException;
import java.util.Map;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.prosolo.bigdata.common.enums.ESIndexTypes;
import org.prosolo.common.ESIndexNames;
import org.prosolo.common.domainmodel.activities.Activity;
import org.prosolo.common.domainmodel.activities.TargetActivity;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.competences.Competence;
import org.prosolo.common.domainmodel.competences.TargetCompetence;
import org.prosolo.common.domainmodel.course.Course;
import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.general.Node;
import org.prosolo.common.domainmodel.user.LearningGoal;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.UserGroup;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Zoran Jeremic 2013-06-29
 *
 */ 
public abstract class AbstractBaseEntityESServiceImpl implements AbstractBaseEntityESService {
	
	private static Logger logger = Logger.getLogger(AbstractBaseEntityESService.class.getName());

	@Inject protected ESIndexer esIndexer;

	@Override
	public void delete(String id, String indexName, String indexType) {
		DeleteResponse deleteResponse;
		try {
			deleteResponse = ElasticSearchFactory.getClient().prepareDelete(indexName, indexType, id).execute().actionGet();
			
			if (!deleteResponse.isFound()){
				logger.error("Entity " + id + " was not existing");
			}
		} catch (NoNodeAvailableException e) {
			logger.error(e);
		} catch (ElasticsearchException e) {
			logger.error(e);
		}
		
	}

	@Override
	public void deleteNodeFromES(BaseEntity node) {
		String id = String.valueOf(node.getId());
		logger.debug("deleting node id:" + id + " title:" + node.getTitle());
		String indexType = getIndexTypeForNode(node);
		String indexName = getIndexNameForNode(node);
		delete(id, indexName, indexType);
	}

	@SuppressWarnings("unchecked")
	public <T extends Node> T get(String id) {
		ObjectMapper objectMapper = new ObjectMapper();
		T entity = null;
		try {
			GetResponse getResponse = ElasticSearchFactory.getClient()
					.prepareGet("nodes", "nodes", id).execute().actionGet();
			
			if (getResponse.isExists()) {
				entity = (T) objectMapper.readValue(getResponse.getSourceAsBytes(), Node.class);
			}
		} catch (IOException e) {
			throw new RuntimeException("Can not read entity " + id);
		} catch (NoNodeAvailableException e) {
			logger.error(e);
		} catch (ElasticsearchException e) {
			logger.error(e);
		}

		return entity;
	}
	
	@Override
	public void indexNode(XContentBuilder builder, String indexId, String indexName, String indexType) {
		Client client = ElasticSearchFactory.getClient();
		esIndexer.addMapping(client,indexName, indexType);
		IndexRequestBuilder requestBuilder = client.prepareIndex(indexName,	indexType, indexId);
		requestBuilder.setSource(builder);
		
		@SuppressWarnings("unused")
		IndexResponse response = requestBuilder.execute().actionGet();
	}
	
	@Override
	public String getIndexTypeForNode(BaseEntity node) {
		String indexType = null;
	 
		if (node instanceof TargetCompetence || node instanceof Competence) {
			indexType = ESIndexTypes.COMPETENCE;
		} else if (node instanceof LearningGoal) {
			indexType = ESIndexTypes.LEARNINGGOAL;
		} else if (node instanceof User) {
			indexType = ESIndexTypes.USER;
		} else if (node instanceof Course) {
			indexType = ESIndexTypes.COURSE;
		} else if (node instanceof Activity || node instanceof TargetActivity) {
			indexType = ESIndexTypes.ACTIVITY;
		} else if (node instanceof Tag) {
			indexType = ESIndexTypes.TAGS;
		} else if (node instanceof Credential1) {
			indexType = ESIndexTypes.CREDENTIAL;
		} else if (node instanceof Competence1) {
			indexType = ESIndexTypes.COMPETENCE1;
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
		} else {
			indexName = ESIndexNames.INDEX_NODES;
		}
		return indexName;
	}
	
	@Override
	public void partialUpdateByScript(String indexName, String indexType, String docId,
			String script, Map<String, Object> scriptParams) {
		try {
			UpdateRequest updateRequest = new UpdateRequest(indexName, indexType, docId)
			        .script(script);
			if(scriptParams != null) {
//				for(Entry<String, Object> param : scriptParams.entrySet()) {
//					updateRequest.addScriptParam(param.getKey(), param.getValue());
//				}
				updateRequest.scriptParams(scriptParams);
				updateRequest.retryOnConflict(5);
			}
			ElasticSearchFactory.getClient().update(updateRequest).get();
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
	}
	
	@Override
	public void partialUpdate(String indexName, String indexType, String docId, 
			XContentBuilder partialDoc) {
		try {
			UpdateRequest updateRequest = new UpdateRequest(indexName, 
					indexType, docId)
			        .doc(partialDoc);
			updateRequest.retryOnConflict(5);
			ElasticSearchFactory.getClient().update(updateRequest).get();
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
	}
}
