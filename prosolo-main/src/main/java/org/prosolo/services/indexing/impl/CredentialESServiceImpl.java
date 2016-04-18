package org.prosolo.services.indexing.impl;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.credential.CredentialBookmark;
import org.prosolo.services.indexing.AbstractBaseEntityESServiceImpl;
import org.prosolo.services.indexing.CredentialESService;
import org.prosolo.services.indexing.ESIndexNames;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.observers.learningResources.CredentialChangeTracker;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("org.prosolo.services.indexing.CredentialESService")
public class CredentialESServiceImpl extends AbstractBaseEntityESServiceImpl implements CredentialESService {
	
	private static Logger logger = Logger.getLogger(CredentialESServiceImpl.class);
	
	@Inject
	private CredentialManager credentialManager;
	
	@Override
	@Transactional
	public void saveCredentialNode(Credential1 cred) {
	 	try {
			XContentBuilder builder = XContentFactory.jsonBuilder().startObject();
			builder.field("id", cred.getId());
			builder.field("title", cred.getTitle());
			builder.field("description", cred.getDescription());
			Date date = cred.getDateCreated();
			DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			builder.field("dateCreated", df.format(date));
			
			builder.startArray("tags");
			List<Tag> tags = credentialManager.getCredentialTags(cred.getId());
			for(Tag tag : tags){
				builder.startObject();
 				builder.field("title", tag.getTitle());
 				builder.endObject();
			}
			builder.endArray();
			
			builder.startArray("hashtags");
			List<Tag> hashtags = credentialManager.getCredentialHashtags(cred.getId());
			for(Tag hashtag : hashtags){
				builder.startObject();
 				builder.field("title", hashtag.getTitle());
 				builder.endObject();
			}
			builder.endArray();
			
			builder.field("creatorId", cred.getCreatedBy().getId());
			builder.field("type", cred.getType());
			
			builder.startArray("bookmarkedBy");
			List<CredentialBookmark> bookmarks = credentialManager.getBookmarkedByIds(cred.getId());
			for(CredentialBookmark cb : bookmarks) {
				builder.startObject();
				builder.field("id", cb.getUser().getId());
				builder.endObject();
			}
			builder.endArray();
			builder.endObject();
			System.out.println("JSON: " + builder.prettyPrint().string());
			String indexType = getIndexTypeForNode(cred);
			indexNode(builder, String.valueOf(cred.getId()), ESIndexNames.INDEX_NODES, indexType);
		} catch (IOException e) {
			logger.error(e);
			e.printStackTrace();
		}
	}
	
	@Override
	@Transactional
	public void updateCredentialNode(Credential1 cred, CredentialChangeTracker changeTracker) {
		if(changeTracker != null && changeTracker.isPublished() &&
				(changeTracker.isTitleChanged() || changeTracker.isDescriptionChanged() ||
				 changeTracker.isTagsChanged() || changeTracker.isHashtagsChanged())) {
			saveCredentialNode(cred);
		}
	}
}
