package org.prosolo.services.indexing.impl;

import java.io.IOException;
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
import org.prosolo.common.domainmodel.credential.Competence1;
import org.prosolo.common.domainmodel.credential.CompetenceUserGroup;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.common.domainmodel.user.UserGroupUser;
import org.prosolo.services.indexing.AbstractBaseEntityESServiceImpl;
import org.prosolo.services.indexing.CompetenceESService;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.UserGroupManager;
import org.prosolo.services.nodes.observers.learningResources.CompetenceChangeTracker;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("org.prosolo.services.indexing.CompetenceESService")
public class CompetenceESServiceImpl extends AbstractBaseEntityESServiceImpl implements CompetenceESService {
	
	private static Logger logger = Logger.getLogger(CompetenceESServiceImpl.class);
	
	@Inject
	private Competence1Manager compManager;
	@Inject
	private UserGroupManager userGroupManager;
	
	@Override
	@Transactional
	public void saveCompetenceNode(Competence1 comp, Session session) {
	 	try {
			XContentBuilder builder = XContentFactory.jsonBuilder().startObject();
			builder.field("id", comp.getId());
			builder.field("published", comp.isPublished());
			builder.field("title", comp.getTitle());
			builder.field("description", comp.getDescription());
			
			builder.startArray("tags");
			List<Tag> tags = compManager.getCompetenceTags(comp.getId(), session);
			for(Tag tag : tags){
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
	public void updateCompetenceNode(Competence1 comp, CompetenceChangeTracker changeTracker, 
			Session session) {
		if(changeTracker != null &&
				(changeTracker.isStatusChanged() || changeTracker.isTitleChanged() || 
						changeTracker.isDescriptionChanged() || changeTracker.isTagsChanged())) {
			saveCompetenceNode(comp, session);
		}
	}
	
	@Override
	public void addUserToIndex(long compId, long userId, UserGroupPrivilege privilege) {
		String field = privilege == UserGroupPrivilege.Edit ? "usersWithEditPrivilege" : "usersWithViewPrivilege";
		String script = "if (ctx._source[\"" + field + "\"] == null) { " +
				"ctx._source." + field + " = user " +
				"} else { " +
				"ctx._source." + field + " += user " +
				"}";
		updateUsers(compId, userId, script);
	}
	
	@Override
	public void removeUserFromIndex(long compId, long userId, UserGroupPrivilege privilege) {
		String field = privilege == UserGroupPrivilege.Edit ? "usersWithEditPrivilege" : "usersWithViewPrivilege";
		String script = "ctx._source." + field + " -= user";
		updateUsers(compId, userId, script);
	}
	
	private void updateUsers(long compId, long userId, String script) {
		try {
			Map<String, Object> params = new HashMap<>();
			Map<String, Object> param = new HashMap<>();
			param.put("id", userId + "");
			params.put("user", param);
			
			partialUpdateByScript(ESIndexNames.INDEX_NODES, ESIndexTypes.COMPETENCE1, 
					compId+"", script, params);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
	}
	
	@Override
	public void updateVisibleToAll(long compId, boolean value) {
		try {
			XContentBuilder builder = XContentFactory.jsonBuilder()
		            .startObject();
			builder.field("visibleToAll", value);
			builder.endObject();
			
			partialUpdate(ESIndexNames.INDEX_NODES, ESIndexTypes.COMPETENCE1, compId + "", builder);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
	}
	
	@Override
	public void updateCompetenceUsersWithPrivileges(long compId, Session session) {
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
			
			partialUpdate(ESIndexNames.INDEX_NODES, ESIndexTypes.COMPETENCE1, compId + "", builder);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
	}
	
	@Override
	public void updateStatus(long compId, boolean published) {
		try {
			XContentBuilder doc = XContentFactory.jsonBuilder()
			    .startObject()
		        .field("published", published)
		        .endObject();
			partialUpdate(ESIndexNames.INDEX_NODES, ESIndexTypes.COMPETENCE1, compId + "", doc);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
	}

}
