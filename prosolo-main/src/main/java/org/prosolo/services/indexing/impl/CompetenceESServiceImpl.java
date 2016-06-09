package org.prosolo.services.indexing.impl;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.prosolo.bigdata.common.enums.ESIndexTypes;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.services.indexing.AbstractBaseEntityESServiceImpl;
import org.prosolo.services.indexing.CompetenceESService;
import org.prosolo.services.indexing.ESIndexNames;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.observers.learningResources.CompetenceChangeTracker;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("org.prosolo.services.indexing.CompetenceESService")
public class CompetenceESServiceImpl extends AbstractBaseEntityESServiceImpl implements CompetenceESService {
	
	private static Logger logger = Logger.getLogger(CompetenceESServiceImpl.class);
	
	@Inject
	private Competence1Manager compManager;
	
	@Override
	@Transactional
	public void saveCompetenceNode(Competence1 comp, long originalVersionId) {
	 	try {
			XContentBuilder builder = XContentFactory.jsonBuilder().startObject();
			builder.field("id", comp.getId());
			builder.field("originalVersionId", originalVersionId);
			builder.field("published", comp.isPublished());
			builder.field("isDraft", comp.isDraft());
			builder.field("hasDraft", comp.isHasDraft());
			builder.field("title", comp.getTitle());
			builder.field("description", comp.getDescription());
			
			builder.startArray("tags");
			List<Tag> tags = compManager.getCompetenceTags(comp.getId());
			for(Tag tag : tags){
				builder.startObject();
 				builder.field("title", tag.getTitle());
 				builder.endObject();
			}
			builder.endArray();
			builder.field("type", comp.getType());
		
			builder.endObject();
			System.out.println("JSON: " + builder.prettyPrint().string());
			String indexType = getIndexTypeForNode(comp);
			indexNode(builder, String.valueOf(comp.getId()), ESIndexNames.INDEX_NODES, indexType);
		} catch (IOException e) {
			logger.error(e);
			e.printStackTrace();
		}
	}
	
	@Override
	@Transactional
	public void updateCompetenceNode(Competence1 comp, long originalVersionId, 
			CompetenceChangeTracker changeTracker) {
		if(changeTracker != null &&
				(changeTracker.isVersionChanged() || changeTracker.isTitleChanged() || 
						changeTracker.isDescriptionChanged() || changeTracker.isTagsChanged())) {
			saveCompetenceNode(comp, originalVersionId);
		}
	}
	
	@Override
	@Transactional
	public void updateCompetenceDraftVersionCreated(String id) {
		try {
			XContentBuilder doc = XContentFactory.jsonBuilder()
		            .startObject()
	                .field("hasDraft", true)
	                .field("published", false)
	                .endObject();
			partialUpdate(ESIndexNames.INDEX_NODES, ESIndexTypes.COMPETENCE1, id, doc);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
	}
}
