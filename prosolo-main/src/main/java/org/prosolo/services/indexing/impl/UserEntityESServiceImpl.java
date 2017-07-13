package org.prosolo.services.indexing.impl;

import org.apache.log4j.Logger;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.hibernate.Session;
import org.prosolo.bigdata.common.enums.ESIndexTypes;
import org.prosolo.common.ESIndexNames;
import org.prosolo.common.domainmodel.credential.TargetCompetence1;
import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.util.ElasticsearchUtil;
import org.prosolo.services.indexing.AbstractBaseEntityESServiceImpl;
import org.prosolo.services.indexing.UserEntityESService;
import org.prosolo.services.interaction.FollowResourceManager;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.CredentialInstructorManager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.RoleManager;
import org.prosolo.services.nodes.data.CredentialData;
import org.prosolo.services.nodes.util.RoleUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
public class UserEntityESServiceImpl extends AbstractBaseEntityESServiceImpl implements UserEntityESService{
	
	private static Logger logger = Logger.getLogger(UserEntityESService.class);
	
	@Inject
	private CredentialManager credManager;
	@Inject
	private CredentialInstructorManager credInstructorManager;
	@Inject
	private FollowResourceManager followResourceManager;
	@Inject
	private RoleManager roleManager;
	@Inject private Competence1Manager compManager;

	@Override
	public void saveUserNode(User user, Session session) {
		//if user has admin role, add it to system user index
		if (RoleUtil.hasAdminRole(user)) {
			saveSystemUser(user, session);
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
		delete(user.getId() + "", ESIndexNames.INDEX_USERS +
				ElasticsearchUtil.getOrganizationIndexSuffix(organizationId), ESIndexTypes.ORGANIZATION_USER);
		//if user is also a system user (admin) update assigned flag to false
		if (RoleUtil.hasAdminRole(user)) {
			updateUserAssignedFlag(user.getId(), false);
		}
	}

	private void saveSystemUser(User user, Session session) {
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
			saveSystemUser(user, session);
		}
		//if user has organization set, add it to organization user index
		if (user.getOrganization() != null) {
			updateBasicOrgUserData(user, user.getOrganization().getId(), session);
		}
	}

	private void saveOrganizationUser(User user, long organizationId, Session session) {
		if (user != null) {
			try {
				XContentBuilder builder = getBasicOrgUserDataSet(user);
				List<CredentialData> creds = credManager.getTargetCredentialsProgressAndInstructorInfoForUser(
						user.getId(), session);
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

				List<CredentialData> instructorCreds = credInstructorManager
						.getCredentialIdsAndAssignDateForInstructor(user.getId());
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

				builder.startArray("followers");
				List<User> folowees = followResourceManager.getFollowers(user.getId());

				for (User foloweee : folowees) {
					builder.startObject();
					builder.field("id", foloweee.getId());
					builder.endObject();
				}
				builder.endArray();

				List<TargetCompetence1> comps = compManager.getTargetCompetencesForUser(user.getId(), session);
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

				builder.endObject();
				System.out.println("JSON: " + builder.prettyPrint().string());
				String indexType = ESIndexTypes.ORGANIZATION_USER;
				String fullIndexName = ESIndexNames.INDEX_USERS +
						ElasticsearchUtil.getOrganizationIndexSuffix(organizationId);
				indexNode(builder, String.valueOf(user.getId()), fullIndexName, indexType);
			} catch (IOException e) {
				logger.error(e);
			}
		}
	}
	
	private void updateBasicOrgUserData(User user, long organizationId, Session session) {
		if(user!=null) {
	 		try {
				XContentBuilder builder = getBasicOrgUserDataSet(user);
				builder.endObject();
				partialUpdate(ESIndexNames.INDEX_USERS +
						ElasticsearchUtil.getOrganizationIndexSuffix(organizationId), ESIndexTypes.ORGANIZATION_USER,
						user.getId() + "", builder);
			} catch (IOException e) {
				logger.error(e);
			}
		}
	}
	
	private XContentBuilder getBasicOrgUserDataSet(User user) throws IOException {
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
		
		builder.startArray("roles");
		for(Role role : user.getRoles()) {
			builder.startObject();
			builder.field("id", role.getId());
			builder.startArray("units");

			builder.endArray();
			builder.endObject();
		}
		builder.endArray();
		
		return builder;
	}
	
	@Override
	public void addCredentialToUserIndex(long credId, long userId, long instructorId, int progress,
			String dateEnrolled) {
		try {
			String script = "if (ctx._source[\"credentials\"] == null) { " +
					"ctx._source.credentials = cred " +
					"} else { " +
					"ctx._source.credentials += cred " +
					"}";
			
			Map<String, Object> params = new HashMap<>();
			Map<String, Object> param = new HashMap<>();
			param.put("id", credId);
			param.put("progress", progress);
			param.put("instructorId", instructorId);
			param.put("dateEnrolled", dateEnrolled);
			params.put("cred", param);
			partialUpdateByScript(ESIndexNames.INDEX_USERS, ESIndexTypes.USER, 
					userId+"", script, params);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
	}
	
	@Override
	public void assignInstructorToUserInCredential(long userId, long credId, long instructorId) {
		try {
			String script = "ctx._source.credentials.findAll {it.id == credId } " +
					".each {it.instructorId = instructorId }";
			
			Map<String, Object> params = new HashMap<>();
			params.put("credId", credId);
			params.put("instructorId", instructorId);
			partialUpdateByScript(ESIndexNames.INDEX_USERS, ESIndexTypes.USER, 
					userId+"", script, params);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
	}
	
	@Override
	public void addInstructorToCredential(long credId, long userId, String dateAssigned) {
		try {
			String script = "if (ctx._source[\"credentialsWithInstructorRole\"] == null) { " +
					"ctx._source.credentialsWithInstructorRole = cred " +
					"} else { " +
					"ctx._source.credentialsWithInstructorRole += cred " +
					"}";
			
			Map<String, Object> params = new HashMap<>();
			Map<String, Object> param = new HashMap<>();
			param.put("id", credId);
			param.put("dateAssigned", dateAssigned);
			params.put("cred", param);
			partialUpdateByScript(ESIndexNames.INDEX_USERS, ESIndexTypes.USER, 
					userId+"", script, params);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
	}
	
	@Override
	public void removeInstructorFromCredential(long credId, long userId) {
		try {
			String script = "credToRemove = null; "
					+ "for (cred in ctx._source.credentialsWithInstructorRole) {"
					+ "if (cred['id'] == credId) "
					+ "{ credToRemove = cred; break; } }; "
					+ "if (credToRemove != null) "
					+ "{ ctx._source.credentialsWithInstructorRole.remove(credToRemove); }";
			
			Map<String, Object> params = new HashMap<>();
			params.put("credId", credId);
			partialUpdateByScript(ESIndexNames.INDEX_USERS, ESIndexTypes.USER, 
					userId+"", script, params);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
	}
	
	@Override
	public void changeCredentialProgress(long userId, long credId, int progress) {
		try {
			String script = "ctx._source.credentials.findAll {it.id == credId } " +
					".each {it.progress = progress }";
			
			Map<String, Object> params = new HashMap<>();
			params.put("credId", credId);
			params.put("progress", progress);
			partialUpdateByScript(ESIndexNames.INDEX_USERS, ESIndexTypes.USER, 
					userId+"", script, params);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
	}
	
	@Override
	public void addFollowerIndex(long followedUserId, long followerId) {
		try {
			String script = 
					"if (ctx._source[\"followers\"] == null) { " +
						"ctx._source.followers = follower " +
					"} else { " +
						"ctx._source.followers += follower " +
					"}";
			
			Map<String, Object> params = new HashMap<>();
			Map<String, Object> param = new HashMap<>();
			param.put("id", followerId);
			params.put("follower", param);
			partialUpdateByScript(ESIndexNames.INDEX_USERS, ESIndexTypes.USER, 
					followedUserId+"", script, params);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
	}
	
	@Override
	public void removeFollowerIndex(long followedUserId, long followerId) {
		try {
			String script = "followerToRemove = null; "
					+ "for (user in ctx._source.followers) {"
					+ "if (user['id'] == followerId) "
					+ "{ followerToRemove = user; break; } }; "
					+ "if (followerToRemove != null) "
					+ "{ ctx._source.followers.remove(followerToRemove); }";
			
			Map<String, Object> params = new HashMap<>();
			params.put("followerId", followerId);
			
			partialUpdateByScript(ESIndexNames.INDEX_USERS, ESIndexTypes.USER, 
					followedUserId+"", script, params);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
	}
	
	@Override
	public void addCompetenceToUserIndex(long compId, long userId, String dateEnrolled) {
		try {
			String script = "if (ctx._source[\"competences\"] == null) { " +
					"ctx._source.competences = comp " +
					"} else { " +
					"ctx._source.competences += comp " +
					"}";
			
			Map<String, Object> params = new HashMap<>();
			Map<String, Object> param = new HashMap<>();
			param.put("id", compId);
			param.put("progress", 0);
			param.put("dateEnrolled", dateEnrolled);
			param.put("dateCompleted", null);
			params.put("comp", param);
			partialUpdateByScript(ESIndexNames.INDEX_USERS, ESIndexTypes.USER, 
					userId+"", script, params);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
	}
	
	@Override
	public void updateCompetenceProgress(long userId, long compId, int progress, String completionDate) {
		try {
			String script = "ctx._source.competences.findAll {it.id == compId } " +
					".each {it.progress = progress; it.dateCompleted = date }";
			
			Map<String, Object> params = new HashMap<>();
			params.put("compId", compId);
			params.put("progress", progress);
			params.put("date", completionDate);
			partialUpdateByScript(ESIndexNames.INDEX_USERS, ESIndexTypes.USER, 
					userId+"", script, params);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
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
			logger.error(e);
			e.printStackTrace();
		}
	}

	@Override
	public void addUserToUnitWithRole(long organizationId, long userId, long unitId, long roleId) {
		try {
			String script = "ctx._source.roles.findAll {it.id == roleId } " +
					".each {" +
						"if (it.units == null) { " +
							"it.units = unit " +
						"} else { " +
							"it.units += unit " +
						"}" +
					" }";

			Map<String, Object> params = new HashMap<>();
			params.put("roleId", roleId);
			Map<String, Object> unitParam = new HashMap<>();
			unitParam.put("id", unitId);
			params.put("unit", unitParam);
			partialUpdateByScript(
					ESIndexNames.INDEX_USERS + ElasticsearchUtil.getOrganizationIndexSuffix(organizationId),
					ESIndexTypes.ORGANIZATION_USER,
					userId+"", script, params);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
	}

	@Override
	public void removeUserFromUnitWithRole(long organizationId, long userId, long unitId, long roleId) {
		try {
			String script = "for (role in ctx._source.roles) { "
								+ "if (role.id == roleId) { "
									+ "unitToRemove = null; "
									+ "for (unit in role.units) { "
										+ "if (unit.id == unitId) { "
											+ "unitToRemove = unit; "
											+ "break; "
										+ "} "
									+ "}; "
									+ "if (unitToRemove != null) { "
										+ "role.units.remove(unitToRemove); "
									+ "}; "
									+ "break;"
								+ "}"
					      + "}";

			Map<String, Object> params = new HashMap<>();
			params.put("roleId", roleId);
			params.put("unitId", unitId);
			partialUpdateByScript(
					ESIndexNames.INDEX_USERS + ElasticsearchUtil.getOrganizationIndexSuffix(organizationId),
					ESIndexTypes.ORGANIZATION_USER,
					userId+"", script, params);
		} catch(Exception e) {
			logger.error(e);
			e.printStackTrace();
		}
	}

}