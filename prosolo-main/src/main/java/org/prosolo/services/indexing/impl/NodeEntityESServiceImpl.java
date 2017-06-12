package org.prosolo.services.indexing.impl;

 
import java.io.IOException;
import java.util.Set;

import org.apache.log4j.Logger;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.prosolo.common.domainmodel.activities.Activity;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.competences.Competence;
import org.prosolo.common.domainmodel.competences.TargetCompetence;
import org.prosolo.common.domainmodel.course.Course;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.general.Node;
import org.prosolo.common.domainmodel.organization.VisibilityType;
import org.prosolo.services.annotation.TagManager;
import org.prosolo.services.indexing.AbstractBaseEntityESServiceImpl;
import org.prosolo.common.ESIndexNames;
import org.prosolo.services.indexing.NodeEntityESService;
import org.prosolo.services.nodes.CompetenceManager;
import org.prosolo.services.nodes.CredentialManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service("org.prosolo.services.indexing.NodeEntityESService")
@Transactional(readOnly=false)
public class NodeEntityESServiceImpl extends AbstractBaseEntityESServiceImpl implements NodeEntityESService {

	private static Logger logger = Logger.getLogger(NodeEntityESServiceImpl.class.getName());
	
	@Autowired private TagManager tagManager;
	@Autowired private CompetenceManager competenceManager;
	@Autowired private CredentialManager credentialManager;
		 
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
	public void saveNodeToES(BaseEntity resource) {
		String indexType = getIndexTypeForNode(resource);
		logger.info("saveNodeToES:"+resource.getClass().getSimpleName()+" id:"+resource.getId());
		saveResourceNode(resource, indexType);
	}
	
	private void saveResourceNode(BaseEntity resource, String indexType) {
		try {
			XContentBuilder builder = XContentFactory.jsonBuilder().startObject();
			builder.field("id", resource.getId());
			builder.field("title", resource.getTitle());
			builder.field("description", resource.getDescription());			
			builder.startArray("tags");
			
			
			Set<Tag> tags = null;
			if (resource instanceof Credential1) {
				
			}
			tags = tagManager.getTagsForResource(resource);			
			if (tags != null) {
				for (Tag tag : tags) {
					if (tag != null) {
						builder.startObject();
						builder.field("title", tag.getTitle());
						builder.endObject();
					}
				}
			}
			builder.endArray();			
			builder.startArray("hashtags");			
			Set<Tag> hashtags = tagManager.getHashtagsForResource(resource);			
			if (hashtags != null) {
				for (Tag tag : hashtags) {
					if (tag != null) {
						builder.startObject();
						builder.field("title", tag.getTitle());
						builder.endObject();
					}
				}
			}
			builder.endArray();
			if(resource instanceof Activity){
				builder.startArray("competences");
				Set<Long> competencesIds=competenceManager.getCompetencesHavingAttachedActivity(resource.getId());
				if(competencesIds !=null){
					for(Long compId:competencesIds){
						if(compId !=null){
							builder.startObject();
							builder.field("id", compId);
							builder.endObject();
						}
					}
				}
				builder.endArray();
			}
			
			builder.endObject();
			indexNode(builder, String.valueOf(resource.getId()),ESIndexNames.INDEX_NODES, indexType);
		} catch (IOException e) {
			logger.error(e);
		}
	}

 

}
