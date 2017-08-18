package org.prosolo.services.indexing.impl;

import org.apache.log4j.Logger;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.hibernate.Session;
import org.prosolo.bigdata.common.enums.ESIndexTypes;
import org.prosolo.common.ESIndexNames;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.credential.*;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.common.domainmodel.user.UserGroupUser;
import org.prosolo.common.util.ElasticsearchUtil;
import org.prosolo.services.indexing.AbstractBaseEntityESServiceImpl;
import org.prosolo.services.indexing.CredentialESService;
import org.prosolo.services.nodes.CredentialInstructorManager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.UnitManager;
import org.prosolo.services.nodes.UserGroupManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service("org.prosolo.services.indexing.CredentialESService")
public class CredentialESServiceImpl extends AbstractBaseEntityESServiceImpl implements CredentialESService {
	
	private static Logger logger = Logger.getLogger(CredentialESServiceImpl.class);
	
	@Inject
	private CredentialManager credentialManager;
	@Inject
	private UserGroupManager userGroupManager;
	@Inject
	private CredentialInstructorManager credInstructorManager;
	@Inject
	private UnitManager unitManager;
	
	@Override
	@Transactional
	public void saveCredentialNode(Credential1 cred, Session session) {
	 	try {
	 		if (cred.getOrganization() != null) {
				XContentBuilder builder = XContentFactory.jsonBuilder().startObject();
				builder.field("id", cred.getId());

				builder.startArray("units");
				//retrieve units for original credential
				long credId = cred.getType() == CredentialType.Original ? cred.getId() : cred.getDeliveryOf().getId();
				List<Long> units = unitManager.getAllUnitIdsCredentialIsConnectedTo(credId, session);
				for (long id : units) {
					builder.startObject();
					builder.field("id", id);
					builder.endObject();
				}
				builder.endArray();

				builder.field("archived", cred.isArchived());
				builder.field("title", cred.getTitle());
				builder.field("description", cred.getDescription());
				Date date = cred.getDateCreated();
				if (date != null) {
					builder.field("dateCreated", ElasticsearchUtil.getDateStringRepresentation(date));
				}
				if (cred.getDeliveryStart() != null) {
					builder.field("deliveryStart", ElasticsearchUtil.getDateStringRepresentation(
							cred.getDeliveryStart()));
				}
				if (cred.getDeliveryEnd() != null) {
					builder.field("deliveryEnd", ElasticsearchUtil.getDateStringRepresentation(
							cred.getDeliveryEnd()));
				}

				builder.startArray("tags");
				List<Tag> tags = credentialManager.getCredentialTags(cred.getId(), session);
				for (Tag tag : tags) {
					builder.startObject();
					builder.field("title", tag.getTitle());
					builder.endObject();
				}
				builder.endArray();

				builder.startArray("hashtags");
				List<Tag> hashtags = credentialManager.getCredentialHashtags(cred.getId(), session);
				for (Tag hashtag : hashtags) {
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
				for (CredentialBookmark cb : bookmarks) {
					builder.startObject();
					builder.field("id", cb.getUser().getId());
					builder.endObject();
				}
				builder.endArray();
				List<Long> instructorsUserIds = credInstructorManager
						.getCredentialInstructorsUserIds(cred.getId());
				builder.startArray("instructors");
				for (Long id : instructorsUserIds) {
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
						g -> g.getPrivilege() == UserGroupPrivilege.Learn).collect(Collectors.toList());
				builder.startArray("usersWithEditPrivilege");
				for (CredentialUserGroup g : editGroups) {
					for (UserGroupUser user : g.getUserGroup().getUsers()) {
						builder.startObject();
						builder.field("id", user.getUser().getId());
						builder.endObject();
					}
				}
				builder.endArray();
				builder.startArray("usersWithViewPrivilege");
				for (CredentialUserGroup g : viewGroups) {
					for (UserGroupUser user : g.getUserGroup().getUsers()) {
						builder.startObject();
						builder.field("id", user.getUser().getId());
						builder.endObject();
					}
				}
				builder.endArray();
				List<TargetCredential1> targetCreds = credentialManager.getTargetCredentialsForCredential(
						cred.getId(), false);
				builder.startArray("students");
				for (TargetCredential1 tc : targetCreds) {
					builder.startObject();
					builder.field("id", tc.getUser().getId());
					builder.endObject();
				}
				builder.endArray();
				builder.endObject();
				System.out.println("JSON: " + builder.prettyPrint().string());
				String indexType = ESIndexTypes.CREDENTIAL;
				indexNode(
						builder, String.valueOf(cred.getId()),
						ESIndexNames.INDEX_NODES + ElasticsearchUtil.getOrganizationIndexSuffix(
								cred.getOrganization().getId()), indexType);
			}
		} catch (IOException e) {
			logger.error(e);
			e.printStackTrace();
		}
	}
	
	@Override
	@Transactional
	public void updateCredentialNode(Credential1 cred, Session session) {
		saveCredentialNode(cred, session);
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
	public void addBookmarkToCredentialIndex(long organizationId, long credId, long userId) {
		String script = "if (ctx._source[\"bookmarkedBy\"] == null) { " +
				"ctx._source.bookmarkedBy = bookmark " +
				"} else { " +
				"ctx._source.bookmarkedBy += bookmark " +
				"}";
		updateCredentialBookmarks(organizationId, credId, userId, script);
	}
	
	@Override
	public void removeBookmarkFromCredentialIndex(long organizationId, long credId, long userId) {
		String script = "ctx._source.bookmarkedBy -= bookmark";
		updateCredentialBookmarks(organizationId, credId, userId, script);
	}
	
	@Override
	public void updateCredentialBookmarks(long organizationId, long credId, Session session) {
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
			
			partialUpdate(
					ESIndexNames.INDEX_NODES + ElasticsearchUtil.getOrganizationIndexSuffix(organizationId),
					ESIndexTypes.CREDENTIAL, credId + "", builder);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
	}
	
	private void updateCredentialBookmarks(long organizationId, long credId, long userId, String script) {
		try {
			Map<String, Object> params = new HashMap<>();
			Map<String, Object> param = new HashMap<>();
			param.put("id", userId);
			params.put("bookmark", param);
			
			partialUpdateByScript(
					ESIndexNames.INDEX_NODES + ElasticsearchUtil.getOrganizationIndexSuffix(organizationId),
					ESIndexTypes.CREDENTIAL,credId+"", script, params);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
	}
	
	@Override
	public void addUserToCredentialIndex(long organizationId, long credId, long userId,
										 UserGroupPrivilege privilege) {
		//temporarely while collection of users with instruct privilege is not introduced
		if (privilege != UserGroupPrivilege.Instruct) {
			String field = privilege == UserGroupPrivilege.Edit ? "usersWithEditPrivilege" : "usersWithViewPrivilege";
			String script = "if (ctx._source[\"" + field + "\"] == null) { " +
					"ctx._source." + field + " = user " +
					"} else { " +
					"ctx._source." + field + " += user " +
					"}";
			updateCredentialUsers(organizationId, credId, userId, script);
		}
	}
	
	@Override
	public void removeUserFromCredentialIndex(long organizationId, long credId, long userId,
											  UserGroupPrivilege privilege) {
		//temporarely while collection of users with instruct privilege is not introduced
		if (privilege != UserGroupPrivilege.Instruct) {
			String field = privilege == UserGroupPrivilege.Edit 
					? "usersWithEditPrivilege" 
					: "usersWithViewPrivilege";
			String script = "ctx._source." + field + " -= user";
			updateCredentialUsers(organizationId, credId, userId, script);
		}
	}
	
	private void updateCredentialUsers(long organizationId, long credId, long userId, String script) {
		try {
			Map<String, Object> params = new HashMap<>();
			Map<String, Object> param = new HashMap<>();
			param.put("id", userId);
			params.put("user", param);
			
			partialUpdateByScript(
					ESIndexNames.INDEX_NODES + ElasticsearchUtil.getOrganizationIndexSuffix(organizationId),
					ESIndexTypes.CREDENTIAL,credId+"", script, params);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
	}
	
	@Override
	public void addStudentToCredentialIndex(long organizationId, long credId, long userId) {
		String script = "if (ctx._source[\"students\"] == null) { " +
				"ctx._source.students = student " +
				"} else { " +
				"ctx._source.students += student " +
				"}";
		updateCredentialStudents(organizationId, credId, userId, script);
	}
	
	@Override
	public void removeStudentFromCredentialIndex(long organizationId, long credId, long userId) {
		String script = "ctx._source.students -= student";
		updateCredentialStudents(organizationId, credId, userId, script);
	}
	
	private void updateCredentialStudents(long organizationId, long credId, long userId,
										  String script) {
		try {
			Map<String, Object> params = new HashMap<>();
			Map<String, Object> param = new HashMap<>();
			param.put("id", userId);
			params.put("student", param);
			
			partialUpdateByScript(
					ESIndexNames.INDEX_NODES + ElasticsearchUtil.getOrganizationIndexSuffix(organizationId),
					ESIndexTypes.CREDENTIAL,
					credId+"", script, params);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
	}
	
	@Override
	public void updateCredentialUsersWithPrivileges(long organizationId, long credId,
													Session session) {
		try {
			XContentBuilder builder = XContentFactory.jsonBuilder()
		            .startObject();
			List<CredentialUserGroup> credGroups = userGroupManager.getAllCredentialUserGroups(
					credId, session);
			List<CredentialUserGroup> editGroups = credGroups.stream().filter(
					g -> g.getPrivilege() == UserGroupPrivilege.Edit).collect(Collectors.toList());
			List<CredentialUserGroup> viewGroups = credGroups.stream().filter(
					g -> g.getPrivilege() == UserGroupPrivilege.Learn).collect(Collectors.toList());
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
			
			partialUpdate(
					ESIndexNames.INDEX_NODES + ElasticsearchUtil.getOrganizationIndexSuffix(organizationId),
					ESIndexTypes.CREDENTIAL, credId + "", builder);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
	}
	
	@Override
	public void updateVisibleToAll(long organizationId, long credId, boolean value) {
		try {
			XContentBuilder builder = XContentFactory.jsonBuilder()
		            .startObject();
			builder.field("visibleToAll", value);
			builder.endObject();
			
			partialUpdate(
					ESIndexNames.INDEX_NODES + ElasticsearchUtil.getOrganizationIndexSuffix(organizationId),
					ESIndexTypes.CREDENTIAL, credId + "", builder);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
	}
	
	@Override
	public void addInstructorToCredentialIndex(long organizationId, long credId, long userId) {
		String script = "if (ctx._source[\"instructors\"] == null) { " +
				"ctx._source.instructors = instructor " +
				"} else { " +
				"ctx._source.instructors += instructor " +
				"}";
		updateCredentialInstructors(organizationId, credId, userId, script);
	}
	
	@Override
	public void removeInstructorFromCredentialIndex(long organizationId, long credId, long userId) {
		String script = "ctx._source.instructors -= instructor";
		updateCredentialInstructors(organizationId, credId, userId, script);
	}
	
	private void updateCredentialInstructors(long organizationId, long credId, long userId,
											 String script) {
		try {
			Map<String, Object> params = new HashMap<>();
			Map<String, Object> param = new HashMap<>();
			param.put("id", userId);
			params.put("instructor", param);
			
			partialUpdateByScript(
					ESIndexNames.INDEX_NODES + ElasticsearchUtil.getOrganizationIndexSuffix(organizationId),
					ESIndexTypes.CREDENTIAL,
					credId+"", script, params);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
	}
	
	@Override
	public void archiveCredential(long organizationId, long credId) {
		try {
			XContentBuilder doc = XContentFactory.jsonBuilder()
			    .startObject()
		        .field("archived", true)
		        .endObject();
			partialUpdate(
					ESIndexNames.INDEX_NODES + ElasticsearchUtil.getOrganizationIndexSuffix(organizationId),
					ESIndexTypes.CREDENTIAL, credId + "", doc);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
	}
	
	@Override
	public void restoreCredential(long organizationId, long credId) {
		try {
			XContentBuilder doc = XContentFactory.jsonBuilder()
			    .startObject()
		        .field("archived", false)
		        .endObject();
			partialUpdate(
					ESIndexNames.INDEX_NODES + ElasticsearchUtil.getOrganizationIndexSuffix(organizationId),
					ESIndexTypes.CREDENTIAL, credId + "", doc);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
	}

	@Override
	public void updateCredentialOwner(long organizationId, long credId, long newOwnerId) {
		try {
			XContentBuilder builder = XContentFactory.jsonBuilder()
					.startObject();
			builder.field("creatorId", newOwnerId);
			builder.endObject();

			partialUpdate(
					ESIndexNames.INDEX_NODES + ElasticsearchUtil.getOrganizationIndexSuffix(organizationId),
					ESIndexTypes.CREDENTIAL, credId + "", builder);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
	}

	@Override
	public void addUnitToCredentialIndex(long organizationId, long credId, long unitId) {
		String script = "if (ctx._source[\"units\"] == null) { " +
				"ctx._source.units = unit " +
				"} else { " +
				"ctx._source.units += unit " +
				"}";
		updateCredentialUnits(organizationId, credId, unitId, script);
	}

	@Override
	public void removeUnitFromCredentialIndex(long organizationId, long credId, long unitId) {
		String script = "ctx._source.units -= unit";
		updateCredentialUnits(organizationId, credId, unitId, script);
	}

	private void updateCredentialUnits(long organizationId, long credId, long unitId, String script) {
		try {
			Map<String, Object> params = new HashMap<>();
			Map<String, Object> param = new HashMap<>();
			param.put("id", unitId);
			params.put("unit", param);

			partialUpdateByScript(
					ESIndexNames.INDEX_NODES + ElasticsearchUtil.getOrganizationIndexSuffix(organizationId),
					ESIndexTypes.CREDENTIAL,credId+"", script, params);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
	}
}
