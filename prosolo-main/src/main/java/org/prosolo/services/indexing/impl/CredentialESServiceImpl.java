package org.prosolo.services.indexing.impl;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import org.prosolo.common.domainmodel.credential.CredentialUserGroup;
import org.prosolo.common.domainmodel.credential.TargetCredential1;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.common.domainmodel.user.UserGroupUser;
import org.prosolo.services.indexing.AbstractBaseEntityESServiceImpl;
import org.prosolo.services.indexing.CredentialESService;
import org.prosolo.services.nodes.CredentialInstructorManager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.UserGroupManager;
import org.prosolo.services.nodes.observers.learningResources.CredentialChangeTracker;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("org.prosolo.services.indexing.CredentialESService")
public class CredentialESServiceImpl extends AbstractBaseEntityESServiceImpl implements CredentialESService {
	
	private static Logger logger = Logger.getLogger(CredentialESServiceImpl.class);
	
	@Inject
	private CredentialManager credentialManager;
	@Inject
	private UserGroupManager userGroupManager;
	@Inject
	private CredentialInstructorManager credInstructorManager;
	
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
			builder.field("visibleToAll", cred.isVisibleToAll());
			
			builder.startArray("bookmarkedBy");
			List<CredentialBookmark> bookmarks = credentialManager.getBookmarkedByIds(
					cred.getId(), session);
			for(CredentialBookmark cb : bookmarks) {
				builder.startObject();
				builder.field("id", cb.getUser().getId());
				builder.endObject();
			}
			builder.endArray();
			List<Long> instructorsUserIds = credInstructorManager
					.getCredentialInstructorsUserIds(cred.getId());
			builder.startArray("instructors");
			for(Long id : instructorsUserIds) {
				builder.startObject();
				builder.field("id", id);
				builder.endObject();
			}
			builder.endArray();
			List<CredentialUserGroup> credGroups = userGroupManager.getAllCredentialUserGroups(
					cred.getId());
			List<CredentialUserGroup> editGroups = credGroups.stream().filter(
					g -> g.getPrivilege() == UserGroupPrivilege.Edit).collect(Collectors.toList());
			List<CredentialUserGroup> viewGroups = credGroups.stream().filter(
					g -> g.getPrivilege() == UserGroupPrivilege.View).collect(Collectors.toList());
			builder.startArray("usersWithEditPrivilege");
			for(CredentialUserGroup g : editGroups) {
				for(UserGroupUser user : g.getUserGroup().getUsers()) {
					builder.startObject();
					builder.field("id", user.getUser().getId());
					builder.endObject();
				}
			}
			builder.endArray();
			builder.startArray("usersWithViewPrivilege");
			for(CredentialUserGroup g : viewGroups) {
				for(UserGroupUser user : g.getUserGroup().getUsers()) {
					builder.startObject();
					builder.field("id", user.getUser().getId());
					builder.endObject();
				}
			}
			builder.endArray();
			List<TargetCredential1> targetCreds = credentialManager.getTargetCredentialsForCredential(
					cred.getId(), false);
			builder.startArray("students");
			for(TargetCredential1 tc : targetCreds) {
				builder.startObject();
				builder.field("id", tc.getUser().getId());
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
				(changeTracker.isStatusChanged() || changeTracker.isTitleChanged() || 
						changeTracker.isDescriptionChanged() || changeTracker.isTagsChanged() 
						|| changeTracker.isHashtagsChanged() || changeTracker.isPublished())) {
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
	
	@Override
	public void addUserToCredentialIndex(long credId, long userId, UserGroupPrivilege privilege) {
		String field = privilege == UserGroupPrivilege.Edit ? "usersWithEditPrivilege" : "usersWithViewPrivilege";
		String script = "if (ctx._source[\"" + field + "\"] == null) { " +
				"ctx._source." + field + " = user " +
				"} else { " +
				"ctx._source." + field + " += user " +
				"}";
		updateCredentialUsers(credId, userId, script);
	}
	
	@Override
	public void removeUserFromCredentialIndex(long credId, long userId, UserGroupPrivilege privilege) {
		String field = privilege == UserGroupPrivilege.Edit ? "usersWithEditPrivilege" : "usersWithViewPrivilege";
		String script = "ctx._source." + field + " -= user";
		updateCredentialUsers(credId, userId, script);
	}
	
	private void updateCredentialUsers(long credId, long userId, String script) {
		try {
			Map<String, Object> params = new HashMap<>();
			Map<String, Object> param = new HashMap<>();
			param.put("id", userId + "");
			params.put("user", param);
			
			partialUpdateByScript(ESIndexNames.INDEX_NODES, ESIndexTypes.CREDENTIAL, 
					credId+"", script, params);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
	}
	
	@Override
	public void addStudentToCredentialIndex(long credId, long userId) {
		String script = "if (ctx._source[\"students\"] == null) { " +
				"ctx._source.students = student " +
				"} else { " +
				"ctx._source.students += student " +
				"}";
		updateCredentialStudents(credId, userId, script);
	}
	
	@Override
	public void removeStudentFromCredentialIndex(long credId, long userId) {
		String script = "ctx._source.students -= student";
		updateCredentialStudents(credId, userId, script);
	}
	
	private void updateCredentialStudents(long credId, long userId, String script) {
		try {
			Map<String, Object> params = new HashMap<>();
			Map<String, Object> param = new HashMap<>();
			param.put("id", userId + "");
			params.put("student", param);
			
			partialUpdateByScript(ESIndexNames.INDEX_NODES, ESIndexTypes.CREDENTIAL, 
					credId+"", script, params);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
	}
	
	@Override
	public void updateCredentialUsersWithPrivileges(long credId, Session session) {
		try {
			XContentBuilder builder = XContentFactory.jsonBuilder()
		            .startObject();
			List<CredentialUserGroup> credGroups = userGroupManager.getAllCredentialUserGroups(
					credId, session);
			List<CredentialUserGroup> editGroups = credGroups.stream().filter(
					g -> g.getPrivilege() == UserGroupPrivilege.Edit).collect(Collectors.toList());
			List<CredentialUserGroup> viewGroups = credGroups.stream().filter(
					g -> g.getPrivilege() == UserGroupPrivilege.View).collect(Collectors.toList());
			builder.startArray("usersWithEditPrivilege");
			for(CredentialUserGroup g : editGroups) {
				for(UserGroupUser user : g.getUserGroup().getUsers()) {
					builder.startObject();
					builder.field("id", user.getUser().getId());
					builder.endObject();
				}
			}
			builder.endArray();
			builder.startArray("usersWithViewPrivilege");
			for(CredentialUserGroup g : viewGroups) {
				for(UserGroupUser user : g.getUserGroup().getUsers()) {
					builder.startObject();
					builder.field("id", user.getUser().getId());
					builder.endObject();
				}
			}
			builder.endArray();
			builder.endObject();
			
			partialUpdate(ESIndexNames.INDEX_NODES, ESIndexTypes.CREDENTIAL, credId + "", builder);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
	}
	
	@Override
	public void updateVisibleToAll(long credId, boolean value) {
		try {
			XContentBuilder builder = XContentFactory.jsonBuilder()
		            .startObject();
			builder.field("visibleToAll", value);
			builder.endObject();
			
			partialUpdate(ESIndexNames.INDEX_NODES, ESIndexTypes.CREDENTIAL, credId + "", builder);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
	}
}
