package org.prosolo.services.indexing.impl;

import org.apache.log4j.Logger;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.hibernate.Session;
import org.prosolo.bigdata.common.enums.ESIndexTypes;
import org.prosolo.common.ESIndexNames;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.common.domainmodel.credential.CompetenceBookmark;
import org.prosolo.common.domainmodel.credential.CompetenceUserGroup;
import org.prosolo.common.domainmodel.credential.TargetCompetence1;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.common.domainmodel.user.UserGroupUser;
import org.prosolo.common.util.ElasticsearchUtil;
import org.prosolo.services.indexing.AbstractBaseEntityESServiceImpl;
import org.prosolo.services.indexing.CompetenceESService;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.DefaultManager;
import org.prosolo.services.nodes.UnitManager;
import org.prosolo.services.nodes.UserGroupManager;
import org.prosolo.services.nodes.observers.learningResources.CompetenceChangeTracker;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service("org.prosolo.services.indexing.CompetenceESService")
public class CompetenceESServiceImpl extends AbstractBaseEntityESServiceImpl implements CompetenceESService {
	
	private static Logger logger = Logger.getLogger(CompetenceESServiceImpl.class);
	
	@Inject private Competence1Manager compManager;
	@Inject private DefaultManager defaultManager;
	@Inject private UserGroupManager userGroupManager;
	@Inject private UnitManager unitManager;
	
	@Override
	@Transactional
	public void saveCompetenceNode(Competence1 competence, Session session) {
	 	try {
	 		if (competence.getOrganization() != null) {
				Competence1 comp = defaultManager.loadResource(Competence1.class, competence.getId());
				XContentBuilder builder = XContentFactory.jsonBuilder().startObject();
				builder.field("id", comp.getId());

				builder.startArray("units");
				List<Long> units = unitManager.getAllUnitIdsCompetenceIsConnectedTo(competence.getId(),
						session);
				for (long id : units) {
					builder.startObject();
					builder.field("id", id);
					builder.endObject();
				}
				builder.endArray();

				builder.field("published", comp.isPublished());
				builder.field("archived", comp.isArchived());
				builder.field("title", comp.getTitle());
				builder.field("description", comp.getDescription());
				Date date = comp.getDateCreated();
				if (date != null) {
					builder.field("dateCreated", ElasticsearchUtil.getDateStringRepresentation(date));
				}
				Date datePublished = comp.getDatePublished();
				if (datePublished != null) {
					builder.field("datePublished", ElasticsearchUtil.getDateStringRepresentation(datePublished));
				}
				builder.startArray("tags");
				List<Tag> tags = compManager.getCompetenceTags(comp.getId(), session);
				for (Tag tag : tags) {
					builder.startObject();
					builder.field("title", tag.getTitle());
					builder.endObject();
				}
				builder.endArray();
				builder.field("type", comp.getType());
				builder.field("creatorId", comp.getCreatedBy().getId());
				builder.field("visibleToAll", comp.isVisibleToAll());

				List<CompetenceUserGroup> compGroups = userGroupManager.getAllCompetenceUserGroups(comp.getId());
				List<CompetenceUserGroup> editGroups = compGroups.stream().filter(
						g -> g.getPrivilege() == UserGroupPrivilege.Edit).collect(Collectors.toList());
				List<CompetenceUserGroup> viewGroups = compGroups.stream().filter(
						g -> g.getPrivilege() == UserGroupPrivilege.Learn).collect(Collectors.toList());
				builder.startArray("usersWithEditPrivilege");
				for (CompetenceUserGroup g : editGroups) {
					for (UserGroupUser user : g.getUserGroup().getUsers()) {
						builder.startObject();
						builder.field("id", user.getUser().getId());
						builder.endObject();
					}
				}
				builder.endArray();
				builder.startArray("usersWithViewPrivilege");
				for (CompetenceUserGroup g : viewGroups) {
					for (UserGroupUser user : g.getUserGroup().getUsers()) {
						builder.startObject();
						builder.field("id", user.getUser().getId());
						builder.endObject();
					}
				}
				builder.endArray();

				builder.startArray("bookmarkedBy");
				List<CompetenceBookmark> bookmarks = compManager.getBookmarkedByIds(
						comp.getId(), session);
				for (CompetenceBookmark cb : bookmarks) {
					builder.startObject();
					builder.field("id", cb.getUser().getId());
					builder.endObject();
				}
				builder.endArray();

				List<TargetCompetence1> targetComps = compManager.getTargetCompetencesForCompetence(
						comp.getId(), false);
				builder.startArray("students");
				for (TargetCompetence1 tc : targetComps) {
					builder.startObject();
					builder.field("id", tc.getUser().getId());
					builder.endObject();
				}
				builder.endArray();

				builder.endObject();
				System.out.println("JSON: " + builder.prettyPrint().string());
				indexNode(builder, String.valueOf(comp.getId()), ESIndexNames.INDEX_NODES
						+ ElasticsearchUtil.getOrganizationIndexSuffix(competence.getOrganization().getId()),
						ESIndexTypes.COMPETENCE);
			}
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
	}
	
	@Override
	@Transactional
	public void updateCompetenceNode(Competence1 comp, CompetenceChangeTracker changeTracker, 
			Session session) {
		if(changeTracker != null &&
				(changeTracker.isStatusChanged() || changeTracker.isTitleChanged() || 
						changeTracker.isDescriptionChanged() || changeTracker.isTagsChanged())) {
			saveCompetenceNode(comp, session);
		}
	}
	
	@Override
	public void addUserToIndex(long organizationId, long compId, long userId, UserGroupPrivilege privilege) {
		//temporarely while collection of users with instruct privilege is not introduced
		if (privilege != UserGroupPrivilege.Instruct) {
			String field = privilege == UserGroupPrivilege.Edit ? "usersWithEditPrivilege" : "usersWithViewPrivilege";
			String script = "if (ctx._source[\"" + field + "\"] == null) { " +
					"ctx._source." + field + " = user " +
					"} else { " +
					"ctx._source." + field + " += user " +
					"}";
			updateUsers(organizationId, compId, userId, script);
		}
	}
	
	@Override
	public void removeUserFromIndex(long organizationId, long compId, long userId, UserGroupPrivilege privilege) {
		//temporarely while collection of users with instruct privilege is not introduced
		if (privilege != UserGroupPrivilege.Instruct) {
			String field = privilege == UserGroupPrivilege.Edit ? "usersWithEditPrivilege" : "usersWithViewPrivilege";
			String script = "ctx._source." + field + " -= user";
			updateUsers(organizationId, compId, userId, script);
		}
	}
	
	private void updateUsers(long organizationId, long compId, long userId, String script) {
		try {
			Map<String, Object> params = new HashMap<>();
			Map<String, Object> param = new HashMap<>();
			param.put("id", userId);
			params.put("user", param);
			
			partialUpdateByScript(ESIndexNames.INDEX_NODES + ElasticsearchUtil.getOrganizationIndexSuffix(organizationId),
					ESIndexTypes.COMPETENCE,compId+"", script, params);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
	}
	
	@Override
	public void updateVisibleToAll(long organizationId, long compId, boolean value) {
		try {
			XContentBuilder builder = XContentFactory.jsonBuilder()
		            .startObject();
			builder.field("visibleToAll", value);
			builder.endObject();
			
			partialUpdate(ESIndexNames.INDEX_NODES + ElasticsearchUtil.getOrganizationIndexSuffix(organizationId),
					ESIndexTypes.COMPETENCE, compId + "", builder);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
	}
	
	@Override
	public void updateCompetenceUsersWithPrivileges(long organizationId, long compId, Session session) {
		try {
			XContentBuilder builder = XContentFactory.jsonBuilder()
		            .startObject();
			List<CompetenceUserGroup> compGroups = userGroupManager.getAllCompetenceUserGroups(
					compId, session);
			List<CompetenceUserGroup> editGroups = compGroups.stream().filter(
					g -> g.getPrivilege() == UserGroupPrivilege.Edit).collect(Collectors.toList());
			List<CompetenceUserGroup> viewGroups = compGroups.stream().filter(
					g -> g.getPrivilege() == UserGroupPrivilege.Learn).collect(Collectors.toList());
			builder.startArray("usersWithEditPrivilege");
			for(CompetenceUserGroup g : editGroups) {
				for(UserGroupUser user : g.getUserGroup().getUsers()) {
					builder.startObject();
					builder.field("id", user.getUser().getId());
					builder.endObject();
				}
			}
			builder.endArray();
			builder.startArray("usersWithViewPrivilege");
			for(CompetenceUserGroup g : viewGroups) {
				for(UserGroupUser user : g.getUserGroup().getUsers()) {
					builder.startObject();
					builder.field("id", user.getUser().getId());
					builder.endObject();
				}
			}
			builder.endArray();
			builder.endObject();
			
			partialUpdate(ESIndexNames.INDEX_NODES + ElasticsearchUtil.getOrganizationIndexSuffix(organizationId),
					ESIndexTypes.COMPETENCE, compId + "", builder);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
	}
	
	@Override
	public void updateStatus(long organizationId, long compId, boolean published, Date datePublished) {
		try {
			XContentBuilder doc = XContentFactory.jsonBuilder().startObject();
			doc.field("published", published);
			if (published && datePublished != null) {
				doc.field("datePublished", ElasticsearchUtil.getDateStringRepresentation(datePublished));
			}
			doc.endObject();
			partialUpdate(ESIndexNames.INDEX_NODES + ElasticsearchUtil.getOrganizationIndexSuffix(organizationId),
					ESIndexTypes.COMPETENCE, compId + "", doc);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
	}
	
	@Override
	public void addBookmarkToCompetenceIndex(long organizationId, long compId, long userId) {
		String script = "if (ctx._source[\"bookmarkedBy\"] == null) { " +
				"ctx._source.bookmarkedBy = bookmark " +
				"} else { " +
				"ctx._source.bookmarkedBy += bookmark " +
				"}";
		updateCompetenceBookmarks(organizationId, compId, userId, script);
	}
	
	@Override
	public void removeBookmarkFromCompetenceIndex(long organizationId, long compId, long userId) {
		String script = "ctx._source.bookmarkedBy -= bookmark";
		updateCompetenceBookmarks(organizationId, compId, userId, script);
	}
	
	private void updateCompetenceBookmarks(long organizationId, long compId, long userId, String script) {
		try {
			Map<String, Object> params = new HashMap<>();
			Map<String, Object> param = new HashMap<>();
			param.put("id", userId);
			params.put("bookmark", param);
			
			partialUpdateByScript(ESIndexNames.INDEX_NODES + ElasticsearchUtil.getOrganizationIndexSuffix(organizationId),
					ESIndexTypes.COMPETENCE,compId+"", script, params);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
	}
	
	@Override
	public void addStudentToCompetenceIndex(long organizationId, long compId, long userId) {
		String script = "if (ctx._source[\"students\"] == null) { " +
				"ctx._source.students = student " +
				"} else { " +
				"ctx._source.students += student " +
				"}";
		updateCompetenceStudents(organizationId, compId, userId, script);
	}
	
	@Override
	public void removeStudentFromCompetenceIndex(long organizationId, long compId, long userId) {
		String script = "ctx._source.students -= student";
		updateCompetenceStudents(organizationId, compId, userId, script);
	}
	
	private void updateCompetenceStudents(long organizationId, long compId, long userId, String script) {
		try {
			Map<String, Object> params = new HashMap<>();
			Map<String, Object> param = new HashMap<>();
			param.put("id", userId);
			params.put("student", param);
			
			partialUpdateByScript(ESIndexNames.INDEX_NODES + ElasticsearchUtil.getOrganizationIndexSuffix(organizationId),
					ESIndexTypes.COMPETENCE,compId+"", script, params);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
	}
	
	@Override
	public void archiveCompetence(long organizationId, long compId) {
		try {
			XContentBuilder doc = XContentFactory.jsonBuilder()
			    .startObject()
		        .field("archived", true)
		        .endObject();
			partialUpdate(ESIndexNames.INDEX_NODES + ElasticsearchUtil.getOrganizationIndexSuffix(organizationId),
					ESIndexTypes.COMPETENCE, compId + "", doc);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
	}
	
	@Override
	public void restoreCompetence(long organizationId, long compId) {
		try {
			XContentBuilder doc = XContentFactory.jsonBuilder()
			    .startObject()
		        .field("archived", false)
		        .endObject();
			partialUpdate(ESIndexNames.INDEX_NODES + ElasticsearchUtil.getOrganizationIndexSuffix(organizationId),
					ESIndexTypes.COMPETENCE, compId + "", doc);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
	}

	@Override
	public void updateCompetenceOwner(long organizationId, long compId, long newOwnerId) {
		try {
			XContentBuilder builder = XContentFactory.jsonBuilder()
					.startObject();
			builder.field("creatorId", newOwnerId);
			builder.endObject();

			partialUpdate(ESIndexNames.INDEX_NODES + ElasticsearchUtil.getOrganizationIndexSuffix(organizationId),
					ESIndexTypes.COMPETENCE, compId + "", builder);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
	}

	@Override
	public void addUnitToCompetenceIndex(long organizationId, long compId, long unitId) {
		String script = "if (ctx._source[\"units\"] == null) { " +
				"ctx._source.units = unit " +
				"} else { " +
				"ctx._source.units += unit " +
				"}";
		updateCompetenceUnits(organizationId, compId, unitId, script);
	}

	@Override
	public void removeUnitFromCompetenceIndex(long organizationId, long compId, long unitId) {
		String script = "ctx._source.units -= unit";
		updateCompetenceUnits(organizationId, compId, unitId, script);
	}

	private void updateCompetenceUnits(long organizationId, long compId, long unitId, String script) {
		try {
			Map<String, Object> params = new HashMap<>();
			Map<String, Object> param = new HashMap<>();
			param.put("id", unitId);
			params.put("unit", param);

			partialUpdateByScript(
					ESIndexNames.INDEX_NODES + ElasticsearchUtil.getOrganizationIndexSuffix(organizationId),
					ESIndexTypes.COMPETENCE,compId + "", script, params);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
	}

}
