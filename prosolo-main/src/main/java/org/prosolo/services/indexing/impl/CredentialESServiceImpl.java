package org.prosolo.services.indexing.impl;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.hibernate.Session;
import org.prosolo.bigdata.common.enums.ESIndexTypes;
import org.prosolo.common.ESIndexNames;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.credential.CredentialBookmark;
import org.prosolo.services.indexing.AbstractBaseEntityESServiceImpl;
import org.prosolo.services.indexing.CredentialESService;
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
	public void saveCredentialNode(Credential1 cred, Session session) {
	 	try {
			XContentBuilder builder = XContentFactory.jsonBuilder().startObject();
			builder.field("id", cred.getId());
			builder.field("published", cred.isPublished());
			builder.field("title", cred.getTitle());
			builder.field("description", cred.getDescription());
			Date date = cred.getDateCreated();
			if(date != null) {
				DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
				builder.field("dateCreated", df.format(date));
			}
			
			builder.startArray("tags");
			List<Tag> tags = credentialManager.getCredentialTags(cred.getId(), session);
			for(Tag tag : tags){
				builder.startObject();
 				builder.field("title", tag.getTitle());
 				builder.endObject();
			}
			builder.endArray();
			
			builder.startArray("hashtags");
			List<Tag> hashtags = credentialManager.getCredentialHashtags(cred.getId(), session);
			for(Tag hashtag : hashtags){
				builder.startObject();
 				builder.field("title", hashtag.getTitle());
 				builder.endObject();
			}
			builder.endArray();
			
			builder.field("creatorId", cred.getCreatedBy().getId());
			builder.field("type", cred.getType());
			
			builder.startArray("bookmarkedBy");
			List<CredentialBookmark> bookmarks = credentialManager.getBookmarkedByIds(
					cred.getId(), session);
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
	public void updateCredentialNode(Credential1 cred, CredentialChangeTracker changeTracker, 
			Session session) {
		if(changeTracker != null &&
				(changeTracker.isVersionChanged() || changeTracker.isTitleChanged() || 
						changeTracker.isDescriptionChanged() || changeTracker.isTagsChanged() 
						|| changeTracker.isHashtagsChanged())) {
			saveCredentialNode(cred, session);
		}
	}
	
//	@Override
//	@Transactional
//	public void updateCredentialDraftVersionCreated(String id) {
//		try {
//			XContentBuilder doc = XContentFactory.jsonBuilder()
//		            .startObject()
//	                .field("hasDraft", true)
//	                .field("published", false)
//	                .endObject();
//			partialUpdate(ESIndexNames.INDEX_NODES, ESIndexTypes.CREDENTIAL, id, doc);
//		} catch(Exception e) {
//			logger.error(e);
//			e.printStackTrace();
//		}
//	}
	
	@Override
	public void addBookmarkToCredentialIndex(long credId, long userId) {
		String script = "if (ctx._source[\"bookmarkedBy\"] == null) { " +
				"ctx._source.bookmarkedBy = bookmark " +
				"} else { " +
				"ctx._source.bookmarkedBy += bookmark " +
				"}";
		updateCredentialBookmarks(credId, userId, script);
	}
	
	@Override
	public void removeBookmarkFromCredentialIndex(long credId, long userId) {
		String script = "ctx._source.bookmarkedBy -= bookmark";
		updateCredentialBookmarks(credId, userId, script);
	}
	
	@Override
	public void updateCredentialBookmarks(long credId, Session session) {
		try {
			XContentBuilder builder = XContentFactory.jsonBuilder()
		            .startObject();
			builder.startArray("bookmarkedBy");
			List<CredentialBookmark> bookmarks = credentialManager.getBookmarkedByIds(
					credId, session);
			for(CredentialBookmark cb : bookmarks) {
				builder.startObject();
				builder.field("id", cb.getUser().getId());
				builder.endObject();
			}
			builder.endArray();
			builder.endObject();
			
			partialUpdate(ESIndexNames.INDEX_NODES, ESIndexTypes.CREDENTIAL, credId + "", builder);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
	}
	
	private void updateCredentialBookmarks(long credId, long userId, String script) {
		try {
			Map<String, Object> params = new HashMap<>();
			Map<String, Object> param = new HashMap<>();
			param.put("id", userId + "");
			params.put("bookmark", param);
			
			partialUpdateByScript(ESIndexNames.INDEX_NODES, ESIndexTypes.CREDENTIAL, 
					credId+"", script, params);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
	}
}
