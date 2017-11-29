package org.prosolo.services.indexing.impl;

import org.apache.log4j.Logger;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.hibernate.Session;
import org.prosolo.bigdata.common.enums.ESIndexTypes;
import org.prosolo.common.ESIndexNames;
import org.prosolo.common.domainmodel.credential.TargetCompetence1;
import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.common.domainmodel.organization.Unit;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.elasticsearch.impl.AbstractESIndexerImpl;
import org.prosolo.common.util.ElasticsearchUtil;
import org.prosolo.services.indexing.UserEntityESService;
import org.prosolo.services.interaction.FollowResourceManager;
import org.prosolo.services.nodes.*;
import org.prosolo.services.nodes.data.CredentialData;
import org.prosolo.services.nodes.util.RoleUtil;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Zoran Jeremic 2013-06-29
 *
 */
@Service("org.prosolo.services.indexing.UserEntityESService")
public class UserEntityESServiceImpl extends AbstractESIndexerImpl implements UserEntityESService{
	
	private static Logger logger = Logger.getLogger(UserEntityESService.class);
	
	@Inject
	private CredentialManager credManager;
	@Inject
	private CredentialInstructorManager credInstructorManager;
	@Inject
	private FollowResourceManager followResourceManager;
	@Inject private Competence1Manager compManager;
	@Inject private UnitManager unitManager;
	@Inject private UserGroupManager userGroupManager;

	@Override
	public void saveUserNode(User user, Session session) {
		//if user has admin role, add it to system user index
		if (RoleUtil.hasAdminRole(user)) {
			saveSystemUser(user);
		}
		//if user has organization set, add it to organization user index
		if (user.getOrganization() != null) {
			saveOrganizationUser(user, user.getOrganization().getId(), session);
		}
	}

	@Override
	public void addUserToOrganization(User user, long organizationId, Session session) {
		saveOrganizationUser(user, organizationId, session);
		//if user is also a system user (admin) update assigned flag to true
		if (RoleUtil.hasAdminRole(user)) {
			updateUserAssignedFlag(user.getId(), true);
		}
	}

	@Override
	public void removeUserFromOrganization(User user, long organizationId) {
		try {
			delete(user.getId() + "", ElasticsearchUtil.getOrganizationIndexName(ESIndexNames.INDEX_USERS, organizationId), ESIndexTypes.ORGANIZATION_USER);
			//if user is also a system user (admin) update assigned flag to false
			if (RoleUtil.hasAdminRole(user)) {
				updateUserAssignedFlag(user.getId(), false);
			}
		} catch (Exception e) {
			logger.error("Error", e);
		}
	}

	private void saveSystemUser(User user) {
		if (user != null) {
			try {
				XContentBuilder builder = XContentFactory.jsonBuilder().startObject();

				builder.field("id", user.getId());
				//	builder.field("url", user.getUri());
				builder.field("name", user.getName());
				builder.field("lastname", user.getLastname());

				builder.field("avatar", user.getAvatarUrl());
				builder.field("position", user.getPosition());

				builder.field("assigned", user.getOrganization() != null);

				builder.startArray("roles");
				user.getRoles().stream().filter(role -> RoleUtil.isAdminRole(role)).forEach( role -> {
					try {
						builder.startObject();
						builder.field("id", role.getId());
						builder.endObject();
					} catch (IOException e) {
						logger.error("error", e);
					}
				});

				builder.endArray();

				builder.endObject();
				logger.info("JSON: " + builder.prettyPrint().string());
				String indexType = ESIndexTypes.USER;
				indexNode(builder, String.valueOf(user.getId()), ESIndexNames.INDEX_USERS, indexType);
			} catch (IOException e) {
				logger.error(e);
			}
		}
	}

	@Override
	public void updateBasicUserData(User user, Session session) {
		//if user has admin role, add it to system user index
		if (RoleUtil.hasAdminRole(user)) {
			saveSystemUser(user);
		}
		//if user has organization set, add it to organization user index
		if (user.getOrganization() != null) {
			updateBasicOrgUserData(user, user.getOrganization().getId(), session);
		}
	}

	private void saveOrganizationUser(User user, long organizationId, Session session) {
		if (user != null) {
			try {
				XContentBuilder builder = getBasicOrgUserDataSet(user, session);

				addCredentials(builder, user.getId(), session);
				addCredentialsWithInstructorRole(builder, user.getId());
				addFollowers(builder, user.getId());
				addCompetences(builder, user.getId(), session);
				addGroups(builder, user.getId());

				builder.endObject();
				System.out.println("JSON: " + builder.prettyPrint().string());
				String indexType = ESIndexTypes.ORGANIZATION_USER;
				String fullIndexName = ElasticsearchUtil.getOrganizationIndexName(ESIndexNames.INDEX_USERS, organizationId);
				indexNode(builder, String.valueOf(user.getId()), fullIndexName, indexType);
			} catch (Exception e) {
				logger.error(e);
			}
		}
	}
	
	private void updateBasicOrgUserData(User user, long organizationId, Session session) {
		if(user!=null) {
	 		try {
				XContentBuilder builder = getBasicOrgUserDataSet(user, session);
				builder.endObject();
				partialUpdate(ElasticsearchUtil.getOrganizationIndexName(ESIndexNames.INDEX_USERS, organizationId), ESIndexTypes.ORGANIZATION_USER,
						user.getId() + "", builder);
			} catch (IOException e) {
				logger.error(e);
			}
		}
	}
	
	private XContentBuilder getBasicOrgUserDataSet(User user, Session session) throws IOException {
		XContentBuilder builder = XContentFactory.jsonBuilder().startObject();
		builder.field("id", user.getId());
	//	builder.field("url", user.getUri());
		builder.field("name", user.getName());
		builder.field("lastname", user.getLastname());
		builder.startObject("location");
		//builder.startObject("pin");
		double latitude = (user.getLatitude() != null && user.getLatitude() != 0) ? user.getLatitude() : 0;
		double longitude = (user.getLongitude() != null && user.getLongitude() != 0) ? user.getLongitude() : 0;
			builder.field("lat", latitude).field("lon", longitude);
		//builder.endObject();
		builder.endObject();
		builder.field("system", user.isSystem());
		builder.field("avatar", user.getAvatarUrl());
		builder.field("position", user.getPosition());
		
		addRoles(builder, user, session);
		
		return builder;
	}

	private void addCredentials(XContentBuilder builder, long userId, Session session) throws IOException {
		List<CredentialData> creds = credManager.getTargetCredentialsProgressAndInstructorInfoForUser(
				userId, session);
		builder.startArray("credentials");

		for (CredentialData cd : creds) {
			builder.startObject();
			long credId = cd.getId();
			builder.field("id", credId);
			int credProgress = cd.getProgress();
			builder.field("progress", credProgress);
			//change when user profile types are implemented
			//				builder.startObject("profile");
			//				builder.field("profileType", "A");
			//				builder.field("profileTitle", "PROFILE 1");
			//				builder.endObject();
			long instructorId = cd.getInstructorId();
			builder.field("instructorId", instructorId);

			Date date = cd.getDate();
			if (date != null) {
				builder.field("dateEnrolled", ElasticsearchUtil.getDateStringRepresentation(date));
			}

			builder.endObject();
		}
		builder.endArray();
	}

	private void addCompetences(XContentBuilder builder, long userId, Session session) throws IOException {
		List<TargetCompetence1> comps = compManager.getTargetCompetencesForUser(userId, session);
		builder.startArray("competences");
		for (TargetCompetence1 tc : comps) {
			builder.startObject();
			builder.field("id", tc.getCompetence().getId());
			builder.field("progress", tc.getProgress());

			Date dateEnrolled = tc.getDateCreated();
			if (dateEnrolled != null) {
				builder.field("dateEnrolled", ElasticsearchUtil.getDateStringRepresentation(dateEnrolled));
			}
			if (tc.getDateCompleted() != null) {
				builder.field("dateCompleted", ElasticsearchUtil.getDateStringRepresentation(
						tc.getDateCompleted()));
			}
			builder.endObject();
		}
		builder.endArray();
	}

	private void addCredentialsWithInstructorRole(XContentBuilder builder, long userId) throws IOException {
		List<CredentialData> instructorCreds = credInstructorManager
				.getCredentialIdsAndAssignDateForInstructor(userId);
		builder.startArray("credentialsWithInstructorRole");
		for (CredentialData cd : instructorCreds) {
			builder.startObject();
			builder.field("id", cd.getId());
			Date date = cd.getDate();
			if (date != null) {
				builder.field("dateAssigned", ElasticsearchUtil.getDateStringRepresentation(date));
			}

			builder.endObject();
		}
		builder.endArray();
	}

	private void addFollowers(XContentBuilder builder, long userId) throws IOException {
		builder.startArray("followers");
		List<User> followers = followResourceManager.getFollowers(userId);
		for (User follower : followers) {
			builder.startObject();
			builder.field("id", follower.getId());
			builder.endObject();
		}
		builder.endArray();
	}

	private void addRoles(XContentBuilder builder, User user, Session session) throws IOException {
		builder.startArray("roles");
		for (Role role : user.getRoles()) {
			builder.startObject();
			builder.field("id", role.getId());
			builder.startArray("units");
			List<Unit> units = unitManager.getAllUnitsWithUserInARole(user.getId(), role.getId(), session);
			for (Unit unit : units) {
				builder.startObject();
				builder.field("id", unit.getId());
				builder.endObject();
			}
			builder.endArray();
			builder.endObject();
		}
		builder.endArray();
	}

	private void addGroups(XContentBuilder builder, long userId) throws IOException {
		builder.startArray("groups");
		List<Long> groups = userGroupManager.getUserGroupIds(userId, false);
		for (long id : groups) {
			builder.startObject();
			builder.field("id", id);
			builder.endObject();
		}
		builder.endArray();
	}

	@Override
	public void updateCredentials(long orgId, long userId, Session session) {
		try {
			XContentBuilder builder = XContentFactory.jsonBuilder()
					.startObject();
			addCredentials(builder, userId, session);
			builder.endObject();

			partialUpdate(ElasticsearchUtil.getOrganizationIndexName(ESIndexNames.INDEX_USERS, orgId), ESIndexTypes.ORGANIZATION_USER,
					userId + "", builder);
		} catch (Exception e) {
			logger.error("Error", e);
		}
	}
	
	@Override
	public void assignInstructorToUserInCredential(long orgId, long userId, long credId, long instructorId) {
		try {
			String script = "ctx._source.credentials.findAll {it.id == credId } " +
					".each {it.instructorId = instructorId }";
			
			Map<String, Object> params = new HashMap<>();
			params.put("credId", credId);
			params.put("instructorId", instructorId);
			partialUpdateByScript(ElasticsearchUtil.getOrganizationIndexName(ESIndexNames.INDEX_USERS, orgId),
					ESIndexTypes.ORGANIZATION_USER,userId+"", script, params);
		} catch(Exception e) {
			logger.error("Error", e);
		}
	}

	@Override
	public void updateCredentialsWithInstructorRole(long orgId, long userId) {
		try {
			XContentBuilder builder = XContentFactory.jsonBuilder()
					.startObject();
			addCredentialsWithInstructorRole(builder, userId);
			builder.endObject();

			partialUpdate(ElasticsearchUtil.getOrganizationIndexName(ESIndexNames.INDEX_USERS, orgId), ESIndexTypes.ORGANIZATION_USER,
					userId + "", builder);
		} catch (Exception e) {
			logger.error("Error", e);
		}
	}
	
	@Override
	public void changeCredentialProgress(long orgId, long userId, long credId, int progress) {
		try {
			String script = "ctx._source.credentials.findAll {it.id == credId } " +
					".each {it.progress = progress }";
			
			Map<String, Object> params = new HashMap<>();
			params.put("credId", credId);
			params.put("progress", progress);
			partialUpdateByScript(ElasticsearchUtil.getOrganizationIndexName(ESIndexNames.INDEX_USERS, orgId),
					ESIndexTypes.ORGANIZATION_USER,userId+"", script, params);
		} catch (Exception e) {
			logger.error("Error", e);
		}
	}

	@Override
	public void updateFollowers(long orgId, long userId) {
		try {
			XContentBuilder builder = XContentFactory.jsonBuilder()
					.startObject();
			addFollowers(builder, userId);
			builder.endObject();

			partialUpdate(ElasticsearchUtil.getOrganizationIndexName(ESIndexNames.INDEX_USERS, orgId), ESIndexTypes.ORGANIZATION_USER,
					userId + "", builder);
		} catch (Exception e) {
			logger.error("Error", e);
		}
	}

	@Override
	public void updateCompetences(long orgId, long userId, Session session) {
		try {
			XContentBuilder builder = XContentFactory.jsonBuilder()
					.startObject();
			addCompetences(builder, userId, session);
			builder.endObject();

			partialUpdate(ElasticsearchUtil.getOrganizationIndexName(ESIndexNames.INDEX_USERS, orgId), ESIndexTypes.ORGANIZATION_USER,
					userId + "", builder);
		} catch (Exception e) {
			logger.error("Error", e);
		}
	}
	
	@Override
	public void updateCompetenceProgress(long orgId, long userId, TargetCompetence1 tComp) {
		try {
			String script = "ctx._source.competences.findAll {it.id == compId } " +
					".each {it.progress = progress; it.dateCompleted = date }";
			
			Map<String, Object> params = new HashMap<>();
			params.put("compId", tComp.getCompetence().getId());
			params.put("progress", tComp.getProgress());
			String dateCompleted = null;
			if (tComp.getDateCompleted() != null) {
				dateCompleted = ElasticsearchUtil.getDateStringRepresentation(tComp.getDateCompleted());
			}
			params.put("date", dateCompleted);
			partialUpdateByScript(ElasticsearchUtil.getOrganizationIndexName(ESIndexNames.INDEX_USERS, orgId),
					ESIndexTypes.ORGANIZATION_USER,userId+"", script, params);
		} catch (Exception e) {
			logger.error("Error", e);
		}
	}

	private void updateUserAssignedFlag(long userId, boolean assigned) {
		try {
			XContentBuilder builder = XContentFactory.jsonBuilder()
					.startObject();
			builder.field("assigned", assigned);
			builder.endObject();

			partialUpdate(ESIndexNames.INDEX_USERS, ESIndexTypes.USER, userId + "", builder);
		} catch(Exception e) {
			logger.error("Error", e);
		}
	}

	@Override
	public void updateRoles(long userId, Session session) {
		try {
			XContentBuilder builder = XContentFactory.jsonBuilder()
					.startObject();
			User user = (User) session.load(User.class, userId);
			addRoles(builder, user, session);
			builder.endObject();

			partialUpdate(ElasticsearchUtil.getOrganizationIndexName(ESIndexNames.INDEX_USERS, user.getOrganization().getId()), ESIndexTypes.ORGANIZATION_USER,
					userId + "", builder);
		} catch (Exception e) {
			logger.error("Error", e);
		}
	}

	@Override
	public void updateGroups(long orgId, long userId) {
		try {
			XContentBuilder builder = XContentFactory.jsonBuilder()
					.startObject();
			addGroups(builder, userId);
			builder.endObject();

			partialUpdate(ElasticsearchUtil.getOrganizationIndexName(ESIndexNames.INDEX_USERS, orgId), ESIndexTypes.ORGANIZATION_USER,
					userId + "", builder);
		} catch (Exception e) {
			logger.error("Error", e);
		}
	}

	@Override
	public void removeUserFromIndex(User user) {
		try {
			if (user.getOrganization() != null) {
				delete(user.getId() + "", ElasticsearchUtil.getOrganizationIndexName(ESIndexNames.INDEX_USERS, user.getOrganization().getId()),
						ESIndexTypes.ORGANIZATION_USER);
			}
			if (RoleUtil.hasAdminRole(user)) {
				delete(user.getId() + "", ESIndexNames.INDEX_USERS, ESIndexTypes.USER);
			}
		} catch (Exception e) {
			logger.error("Error", e);
		}
	}

}