package org.prosolo.services.indexing.impl;

 
import java.io.IOException;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.prosolo.common.ESIndexNames;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.elasticsearch.impl.AbstractESIndexerImpl;
import org.prosolo.services.annotation.TagManager;
import org.prosolo.services.indexing.NodeEntityESService;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.CredentialManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Deprecated
@Service("org.prosolo.services.indexing.NodeEntityESService")
@Transactional(readOnly=false)
public class NodeEntityESServiceImpl extends AbstractESIndexerImpl implements NodeEntityESService {

	private static Logger logger = Logger.getLogger(NodeEntityESServiceImpl.class.getName());
	
	@Autowired private TagManager tagManager;
	@Autowired private Competence1Manager competenceManager;
	@Autowired private CredentialManager credentialManager;
		 
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
	public void saveNodeToES(BaseEntity resource) {
		String indexType = getIndexTypeForNode(resource);
		logger.info("saveNodeToES:"+resource.getClass().getSimpleName()+" id:"+resource.getId());
		saveResourceNode(resource, indexType);
	}
	
	private void saveResourceNode(BaseEntity resource, String indexType) {
//		try {
//			XContentBuilder builder = XContentFactory.jsonBuilder().startObject();
//			builder.field("id", resource.getId());
//			builder.field("title", resource.getTitle());
//			builder.field("description", resource.getDescription());
//			builder.startArray("tags");
//
//
//			Collection<Tag> tags = null;
//			if (resource instanceof Credential1) {
//				tags = credentialManager.getTagsForCredential(resource.getId());
//			} else if (resource instanceof Competence1) {
//				tags = competenceManager.getTagsForCompetence(resource.getId());
//			}
//			if (tags != null) {
//				for (Tag tag : tags) {
//					if (tag != null) {
//						builder.startObject();
//						builder.field("title", tag.getTitle());
//						builder.endObject();
//					}
//				}
//			}
//			builder.endArray();
//			builder.startArray("hashtags");
//
//			Collection<Tag> hashtags = null;
//			if (resource instanceof Credential1) {
//				hashtags = credentialManager.getHashtagsForCredential(resource.getId());
//			}
//
//			if (hashtags != null) {
//				for (Tag tag : hashtags) {
//					if (tag != null) {
//						builder.startObject();
//						builder.field("title", tag.getTitle());
//						builder.endObject();
//					}
//				}
//			}
//			builder.endArray();
//
//			builder.endObject();
//			indexNode(builder, String.valueOf(resource.getId()),ESIndexNames.INDEX_NODES, indexType);
//		} catch (IOException e) {
//			logger.error(e);
//		}
	}

 

}
