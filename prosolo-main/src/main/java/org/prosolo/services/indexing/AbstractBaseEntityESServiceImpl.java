package org.prosolo.services.indexing;

import java.io.IOException;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.prosolo.bigdata.common.enums.ESIndexTypes;
import org.prosolo.bigdata.common.exceptions.IndexingServiceNotAvailable;
import org.prosolo.domainmodel.activities.Activity;
import org.prosolo.domainmodel.activities.TargetActivity;
import org.prosolo.domainmodel.annotation.Tag;
import org.prosolo.domainmodel.competences.Competence;
import org.prosolo.domainmodel.competences.TargetCompetence;
import org.prosolo.domainmodel.course.Course;
import org.prosolo.domainmodel.general.BaseEntity;
import org.prosolo.domainmodel.general.Node;
import org.prosolo.domainmodel.user.LearningGoal;
import org.prosolo.domainmodel.user.User;

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
		} catch (ElasticsearchException e) {
			logger.error(e);
		} catch (IndexingServiceNotAvailable e) {
			// TODO Auto-generated catch block
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
		} catch (ElasticsearchException e) {
			logger.error(e);
		} catch (IndexingServiceNotAvailable e) {
			logger.error(e);
		}

		return entity;
	}
	
	@Override
	public void indexNode(XContentBuilder builder, String indexId, String indexName, String indexType) {
		try {
			Client client = ElasticSearchFactory.getClient();
			esIndexer.addMapping(client,indexName, indexType);
			IndexRequestBuilder requestBuilder = client.prepareIndex(indexName,	indexType, indexId);
			requestBuilder.setSource(builder);
			
			@SuppressWarnings("unused")
			IndexResponse response = requestBuilder.execute().actionGet();
			// client.close();
		} catch (IndexingServiceNotAvailable e) {
			logger.error(e);
		}
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
		}
		return indexType;
	}
	
	@Override
	public String getIndexNameForNode(BaseEntity node) {
		String indexName = null;
		if (node instanceof User) {
			indexName = ESIndexNames.INDEX_USERS;
		} else {
			indexName = ESIndexNames.INDEX_NODES;
		}
		return indexName;
	}
}
