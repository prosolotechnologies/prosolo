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
import org.prosolo.common.elasticsearch.impl.AbstractESIndexerImpl;
import org.prosolo.common.util.ElasticsearchUtil;
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
import java.util.List;
import java.util.stream.Collectors;

@Service("org.prosolo.services.indexing.CredentialESService")
public class CredentialESServiceImpl extends AbstractESIndexerImpl implements CredentialESService {
	
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

				//retrieve units for original credential
				long credId = cred.getType() == CredentialType.Original ? cred.getId() : cred.getDeliveryOf().getId();
				List<Long> units = unitManager.getAllUnitIdsCredentialIsConnectedTo(credId, session);
				addUnits(builder, units);

				builder.field("archived", cred.isArchived());
				builder.field("title", cred.getTitle());
				builder.field("description", cred.getDescription());
				Date date = cred.getDateCreated();
				if (date != null) {
					builder.field("dateCreated", ElasticsearchUtil.getDateStringRepresentation(date));
				}
				String deliveryStart = null;
				if (cred.getDeliveryStart() != null) {
					deliveryStart = ElasticsearchUtil.getDateStringRepresentation(cred.getDeliveryStart());
				}
				builder.field("deliveryStart", deliveryStart);
				String deliveryEnd = null;
				if (cred.getDeliveryEnd() != null) {
					deliveryEnd = ElasticsearchUtil.getDateStringRepresentation(cred.getDeliveryEnd());
				}
				builder.field("deliveryEnd", deliveryEnd);

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

				addBookmarks(builder, cred.getId(), session);
				addInstructors(builder, cred.getId());
				addUsersWithPrivileges(builder, cred.getId(), session);
				addStudents(builder, cred.getId());

				builder.endObject();
				System.out.println("JSON: " + builder.prettyPrint().string());
				indexNode(
						builder, String.valueOf(cred.getId()),
						ElasticsearchUtil.getOrganizationIndexName(ESIndexNames.INDEX_NODES, cred.getOrganization().getId()),
						ESIndexTypes.CREDENTIAL);
			}
		} catch (IOException e) {
			logger.error("Error", e);
		}
	}

	private void addUnits(XContentBuilder builder, List<Long> units) throws IOException {
		builder.startArray("units");
		for (long id : units) {
			builder.startObject();
			builder.field("id", id);
			builder.endObject();
		}
		builder.endArray();
	}

	private void addBookmarks(XContentBuilder builder, long credId, Session session) throws IOException {
		builder.startArray("bookmarkedBy");
		List<CredentialBookmark> bookmarks = credentialManager.getBookmarkedByIds(
				credId, session);
		for (CredentialBookmark cb : bookmarks) {
			builder.startObject();
			builder.field("id", cb.getUser().getId());
			builder.endObject();
		}
		builder.endArray();
	}

	private void addInstructors(XContentBuilder builder, long credId) throws IOException {
		List<Long> instructorsUserIds = credInstructorManager.getCredentialInstructorsUserIds(credId);
		builder.startArray("instructors");
		for (Long id : instructorsUserIds) {
			builder.startObject();
			builder.field("id", id);
			builder.endObject();
		}
		builder.endArray();
	}

	private void addUsersWithPrivileges(XContentBuilder builder, long credId, Session session) throws IOException {
		List<CredentialUserGroup> credGroups = userGroupManager.getAllCredentialUserGroups(
				credId, session);
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
	}

	private void addStudents(XContentBuilder builder, long credId) throws IOException {
		List<TargetCredential1> targetCreds = credentialManager.getTargetCredentialsForCredential(
				credId, false);
		builder.startArray("students");
		for (TargetCredential1 tc : targetCreds) {
			builder.startObject();
			builder.field("id", tc.getUser().getId());
			builder.endObject();
		}
		builder.endArray();
	}
	
	@Override
	@Transactional
	public void updateCredentialNode(Credential1 cred, Session session) {
		saveCredentialNode(cred, session);
	}


//	@Override
//	public void addBookmarkToCredentialIndex(long organizationId, long credId, long userId) {
//		String script = "if (ctx._source[\"bookmarkedBy\"] == null) { " +
//				"ctx._source.bookmarkedBy = bookmark " +
//				"} else { " +
//				"ctx._source.bookmarkedBy += bookmark " +
//				"}";
//		updateCredentialBookmarks(organizationId, credId, userId, script);
//	}
//
//	@Override
//	public void removeBookmarkFromCredentialIndex(long organizationId, long credId, long userId) {
//		String script = "ctx._source.bookmarkedBy -= bookmark";
//		updateCredentialBookmarks(organizationId, credId, userId, script);
//	}
//
//	private void updateCredentialBookmarks(long organizationId, long credId, long userId, String script) {
//		try {
//			Map<String, Object> params = new HashMap<>();
//			Map<String, Object> param = new HashMap<>();
//			param.put("id", userId);
//			params.put("bookmark", param);
//
//			partialUpdateByScript(
//					ESIndexNames.INDEX_NODES + ElasticsearchUtil.getOrganizationIndexSuffix(organizationId),
//					ESIndexTypes.CREDENTIAL,credId+"", script, params);
//		} catch(Exception e) {
//			logger.error(e);
//			e.printStackTrace();
//		}
//	}
	
	@Override
	public void updateCredentialBookmarks(long organizationId, long credId, Session session) {
		try {
			XContentBuilder builder = XContentFactory.jsonBuilder()
		            .startObject();
			addBookmarks(builder, credId, session);
			builder.endObject();
			
			partialUpdate(
					ElasticsearchUtil.getOrganizationIndexName(ESIndexNames.INDEX_NODES, organizationId),
					ESIndexTypes.CREDENTIAL, credId + "", builder);
		} catch(Exception e) {
			logger.error("Error", e);
		}
	}

	@Override
	public void updateStudents(long organizationId, long credId) {
		try {
			XContentBuilder builder = XContentFactory.jsonBuilder()
					.startObject();
			addStudents(builder, credId);
			builder.endObject();

			partialUpdate(
					ElasticsearchUtil.getOrganizationIndexName(ESIndexNames.INDEX_NODES, organizationId),
					ESIndexTypes.CREDENTIAL, credId + "", builder);
		} catch(Exception e) {
			logger.error("Error", e);
		}
	}
	
	@Override
	public void updateCredentialUsersWithPrivileges(long organizationId, long credId,
													Session session) {
		try {
			XContentBuilder builder = XContentFactory.jsonBuilder()
		            .startObject();
			addUsersWithPrivileges(builder, credId, session);
			builder.endObject();
			
			partialUpdate(
					ElasticsearchUtil.getOrganizationIndexName(ESIndexNames.INDEX_NODES, organizationId),
					ESIndexTypes.CREDENTIAL, credId + "", builder);
		} catch(Exception e) {
			logger.error("Error", e);
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
					ElasticsearchUtil.getOrganizationIndexName(ESIndexNames.INDEX_NODES, organizationId),
					ESIndexTypes.CREDENTIAL, credId + "", builder);
		} catch(Exception e) {
			logger.error("Error", e);
		}
	}

	@Override
	public void updateInstructors(long organizationId, long credId, Session session) {
		try {
			XContentBuilder builder = XContentFactory.jsonBuilder()
					.startObject();
			addInstructors(builder, credId);
			builder.endObject();

			partialUpdate(
					ElasticsearchUtil.getOrganizationIndexName(ESIndexNames.INDEX_NODES, organizationId),
					ESIndexTypes.CREDENTIAL, credId + "", builder);
		} catch(Exception e) {
			logger.error("Error", e);
		}
	}

	@Override
	public void updateArchived(long organizationId, long credId, boolean archived) {
		try {
			XContentBuilder doc = XContentFactory.jsonBuilder()
					.startObject()
					.field("archived", archived)
					.endObject();
			partialUpdate(ElasticsearchUtil.getOrganizationIndexName(ESIndexNames.INDEX_NODES, organizationId),
					ESIndexTypes.CREDENTIAL, credId + "", doc);
		} catch(Exception e) {
			logger.error("Error", e);
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
					ElasticsearchUtil.getOrganizationIndexName(ESIndexNames.INDEX_NODES, organizationId),
					ESIndexTypes.CREDENTIAL, credId + "", builder);
		} catch(Exception e) {
			logger.error("Error", e);
		}
	}

	@Override
	public void updateUnitsForOriginalCredentialAndItsDeliveries(long organizationId, long credentialId, Session session) {
		try {
			//retrieve units for original credential
			List<Long> units = unitManager.getAllUnitIdsCredentialIsConnectedTo(credentialId, session);

			updateUnits(organizationId, credentialId, units);
			List<Long> deliveries = credentialManager.getDeliveryIdsForCredential(credentialId);
			//update units for all credential deliveries
			for (long id : deliveries) {
				updateUnits(organizationId, id, units);
			}
		} catch(Exception e) {
			logger.error("Error", e);
		}
	}

	private void updateUnits(long organizationId, long credId, List<Long> units) throws IOException {
		XContentBuilder builder = XContentFactory.jsonBuilder()
				.startObject();
		addUnits(builder, units);
		builder.endObject();

		partialUpdate(
				ElasticsearchUtil.getOrganizationIndexName(ESIndexNames.INDEX_NODES, organizationId),
				ESIndexTypes.CREDENTIAL, credId + "", builder);
	}

	@Override
	public void updateDeliveryTimes(long organizationId, Credential1 delivery) {
		try {
			XContentBuilder builder = XContentFactory.jsonBuilder()
					.startObject();
			String deliveryStartStr = null;
			if (delivery.getDeliveryStart() != null) {
				deliveryStartStr = ElasticsearchUtil.getDateStringRepresentation(delivery.getDeliveryStart());
			}
			builder.field("deliveryStart", deliveryStartStr);
			String deliveryEndStr = null;
			if (delivery.getDeliveryEnd() != null) {
				deliveryEndStr = ElasticsearchUtil.getDateStringRepresentation(delivery.getDeliveryEnd());
			}
			builder.field("deliveryEnd", deliveryEndStr);
			builder.endObject();

			partialUpdate(
					ElasticsearchUtil.getOrganizationIndexName(ESIndexNames.INDEX_NODES, organizationId),
					ESIndexTypes.CREDENTIAL, delivery.getId() + "", builder);
		} catch(Exception e) {
			logger.error("Error", e);
		}
	}
}
