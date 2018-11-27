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
import org.prosolo.services.indexing.CompetenceESService;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.UnitManager;
import org.prosolo.services.user.UserGroupManager;
import org.prosolo.services.nodes.observers.learningResources.CompetenceChangeTracker;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service("org.prosolo.services.indexing.CompetenceESService")
public class CompetenceESServiceImpl extends AbstractBaseEntityESServiceImpl implements CompetenceESService {
	
	private static Logger logger = Logger.getLogger(CompetenceESServiceImpl.class);
	
	@Inject private Competence1Manager compManager;
	@Inject private UserGroupManager userGroupManager;
	@Inject private UnitManager unitManager;
	
	@Override
	@Transactional
	public void saveCompetenceNode(Competence1 competence, Session session) {
	 	try {
	 		if (competence.getOrganization() != null) {
				XContentBuilder builder = XContentFactory.jsonBuilder().startObject();
				builder.field("id", competence.getId());

				addUnits(builder, competence.getId(), session);

				builder.field("published", competence.isPublished());
				builder.field("archived", competence.isArchived());
				builder.field("title", competence.getTitle());
				builder.field("description", competence.getDescription());
				Date date = competence.getDateCreated();
				if (date != null) {
					builder.field("dateCreated", ElasticsearchUtil.getDateStringRepresentation(date));
				}
				Date datePublished = competence.getDatePublished();
				if (datePublished != null) {
					builder.field("datePublished", ElasticsearchUtil.getDateStringRepresentation(datePublished));
				}
				builder.startArray("tags");
				List<Tag> tags = compManager.getCompetenceTags(competence.getId(), session);
				for (Tag tag : tags) {
					builder.startObject();
					builder.field("title", tag.getTitle());
					builder.endObject();
				}
				builder.endArray();
				builder.field("type", competence.getType());
				builder.field("creatorId", competence.getCreatedBy().getId());
				builder.field("visibleToAll", competence.isVisibleToAll());

				setLearningStageInfo(builder, competence);

				addBookmarks(builder, competence.getId(), session);
				addUsersWithPrivileges(builder, competence.getId(), session);
				addStudents(builder, competence.getId());

				builder.endObject();
				System.out.println("JSON: " + builder.prettyPrint().string());
				indexNode(builder, String.valueOf(competence.getId()),
						ElasticsearchUtil.getOrganizationIndexName(ESIndexNames.INDEX_NODES, competence.getOrganization().getId()),
						ESIndexTypes.COMPETENCE);
			}
		} catch (Exception e) {
			logger.error("Error", e);
		}
	}

	private void setLearningStageInfo(XContentBuilder builder, Competence1 competence) throws IOException {
		builder.field("learningStageId", competence.getLearningStage() != null ? competence.getLearningStage().getId() : 0);
		builder.field("firstStageCompetenceId",
				competence.getFirstLearningStageCompetence() != null
						? competence.getFirstLearningStageCompetence().getId()
						: 0);
	}

	private void addUnits(XContentBuilder builder, long compId, Session session) throws IOException {
		List<Long> units = unitManager.getAllUnitIdsCompetenceIsConnectedTo(compId, session);
		builder.startArray("units");
		for (long id : units) {
			builder.startObject();
			builder.field("id", id);
			builder.endObject();
		}
		builder.endArray();
	}

	private void addBookmarks(XContentBuilder builder, long compId, Session session) throws IOException {
		builder.startArray("bookmarkedBy");
		List<CompetenceBookmark> bookmarks = compManager.getBookmarkedByIds(compId, session);
		for (CompetenceBookmark cb : bookmarks) {
			builder.startObject();
			builder.field("id", cb.getUser().getId());
			builder.endObject();
		}
		builder.endArray();
	}

	private void addUsersWithPrivileges(XContentBuilder builder, long compId, Session session) throws IOException {
		List<CompetenceUserGroup> compGroups = userGroupManager.getAllCompetenceUserGroups(compId, session);
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
	}

	private void addStudents(XContentBuilder builder, long compId) throws IOException {
		List<TargetCompetence1> targetComps = compManager.getTargetCompetencesForCompetence(
				compId, false);
		builder.startArray("students");
		for (TargetCompetence1 tc : targetComps) {
			builder.startObject();
			builder.field("id", tc.getUser().getId());
			builder.endObject();
		}
		builder.endArray();
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
	public void updateVisibleToAll(long organizationId, long compId, boolean value) {
		try {
			XContentBuilder builder = XContentFactory.jsonBuilder()
		            .startObject();
			builder.field("visibleToAll", value);
			builder.endObject();
			
			partialUpdate(ElasticsearchUtil.getOrganizationIndexName(ESIndexNames.INDEX_NODES, organizationId),
					ESIndexTypes.COMPETENCE, compId + "", builder);
		} catch(Exception e) {
			logger.error("Error", e);
		}
	}
	
	@Override
	public void updateCompetenceUsersWithPrivileges(long organizationId, long compId, Session session) {
		try {
			XContentBuilder builder = XContentFactory.jsonBuilder()
		            .startObject();
			addUsersWithPrivileges(builder, compId, session);
			builder.endObject();
			
			partialUpdate(ElasticsearchUtil.getOrganizationIndexName(ESIndexNames.INDEX_NODES, organizationId),
					ESIndexTypes.COMPETENCE, compId + "", builder);
		} catch(Exception e) {
			logger.error("Error", e);
		}
	}
	
	@Override
	public void updateStatus(long organizationId, Competence1 comp) {
		try {
			XContentBuilder doc = XContentFactory.jsonBuilder().startObject();
			doc.field("published", comp.isPublished());
			if (comp.isPublished() && comp.getDatePublished() != null) {
				doc.field("datePublished", ElasticsearchUtil.getDateStringRepresentation(comp.getDatePublished()));
			}
			doc.endObject();
			partialUpdate(ElasticsearchUtil.getOrganizationIndexName(ESIndexNames.INDEX_NODES, organizationId),
					ESIndexTypes.COMPETENCE, comp.getId() + "", doc);
		} catch(Exception e) {
			logger.error("Error", e);
		}
	}

	@Override
	public void updateCompetenceBookmarks(long organizationId, long compId, Session session) {
		try {
			XContentBuilder builder = XContentFactory.jsonBuilder()
					.startObject();
			addBookmarks(builder, compId, session);
			builder.endObject();

			partialUpdate(
					ElasticsearchUtil.getOrganizationIndexName(ESIndexNames.INDEX_NODES, organizationId),
					ESIndexTypes.COMPETENCE, compId + "", builder);
		} catch(Exception e) {
			logger.error("Error", e);
		}
	}

	@Override
	public void updateStudents(long organizationId, long compId) {
		try {
			XContentBuilder builder = XContentFactory.jsonBuilder()
					.startObject();
			addStudents(builder, compId);
			builder.endObject();

			partialUpdate(
					ElasticsearchUtil.getOrganizationIndexName(ESIndexNames.INDEX_NODES, organizationId),
					ESIndexTypes.COMPETENCE, compId + "", builder);
		} catch(Exception e) {
			logger.error("Error", e);
		}
	}

	@Override
	public void updateUnits(long organizationId, long compId, Session session) {
		try {
			XContentBuilder builder = XContentFactory.jsonBuilder()
					.startObject();
			addUnits(builder, compId, session);
			builder.endObject();

			partialUpdate(
					ElasticsearchUtil.getOrganizationIndexName(ESIndexNames.INDEX_NODES, organizationId),
					ESIndexTypes.COMPETENCE, compId + "", builder);
		} catch (Exception e) {
			logger.error("Error", e);
		}
	}
	
	@Override
	public void updateArchived(long organizationId, long compId, boolean archived) {
		try {
			XContentBuilder doc = XContentFactory.jsonBuilder()
			    .startObject()
		        .field("archived", archived)
		        .endObject();
			partialUpdate(ElasticsearchUtil.getOrganizationIndexName(ESIndexNames.INDEX_NODES, organizationId),
					ESIndexTypes.COMPETENCE, compId + "", doc);
		} catch(Exception e) {
			logger.error("Error", e);
		}
	}

	@Override
	public void updateCompetenceOwner(long organizationId, long compId, long newOwnerId) {
		try {
			XContentBuilder builder = XContentFactory.jsonBuilder()
					.startObject();
			builder.field("creatorId", newOwnerId);
			builder.endObject();

			partialUpdate(ElasticsearchUtil.getOrganizationIndexName(ESIndexNames.INDEX_NODES, organizationId),
					ESIndexTypes.COMPETENCE, compId + "", builder);
		} catch(Exception e) {
			logger.error("Error", e);
		}
	}

	@Override
	public void updateLearningStageInfo(Competence1 comp) {
		try {
			XContentBuilder builder = XContentFactory.jsonBuilder()
					.startObject();
			setLearningStageInfo(builder, comp);
			builder.endObject();

			partialUpdate(
					ElasticsearchUtil.getOrganizationIndexName(ESIndexNames.INDEX_NODES, comp.getOrganization().getId()),
					ESIndexTypes.COMPETENCE, comp.getId() + "", builder);
		} catch (Exception e) {
			logger.error("Error", e);
		}
	}

}
