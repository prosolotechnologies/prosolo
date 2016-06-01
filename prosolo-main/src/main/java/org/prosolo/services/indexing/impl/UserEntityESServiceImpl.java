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
import org.prosolo.common.domainmodel.organization.Role;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.indexing.AbstractBaseEntityESServiceImpl;
import org.prosolo.services.indexing.ESIndexNames;
import org.prosolo.services.indexing.UserEntityESService;
import org.prosolo.services.nodes.CredentialInstructorManager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.RoleManager;
import org.prosolo.services.nodes.data.CredentialData;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
	private RoleManager roleManager;
	
	@Override
	@Transactional
	public void saveUserNode(User user, Session session) {
 //	user = (User) session.merge(user);
		if(user!=null)
	 		try {
			XContentBuilder builder = XContentFactory.jsonBuilder().startObject();
			builder.field("id", user.getId());
		//	builder.field("url", user.getUri());
			builder.field("name", user.getName());
			builder.field("lastname", user.getLastname());
			builder.startObject("location");
			builder.startObject("pin");
			double latitude = (user.getLatitude() != null && user.getLatitude() != 0) ? user.getLatitude() : 0;
			double longitude = (user.getLongitude() != null && user.getLongitude() != 0) ? user.getLongitude() : 0;
 			builder.field("lat", latitude).field("lon", longitude);
			builder.endObject();
			builder.endObject();
			builder.field("system", user.isSystem());
			
			//builder.startArray("learninggoals");
//			List<TargetLearningGoal> targetLearningGoals = learningGoalManager.getUserTargetGoals(user, session);
//			//Set<TargetLearningGoal> targetLearningGoals=user.getLearningGoals();
//			for(TargetLearningGoal tGoal: targetLearningGoals){
//				LearningGoal lGoal=tGoal.getLearningGoal();
//				builder.startObject();
// 				builder.field("title", lGoal.getTitle());
// 				builder.field("description", lGoal.getDescription());
// 				builder.endObject();
//			}
//			Set<LearningGoal> lGoals = user.getLearningGoals();
//			
//			for (LearningGoal lGoal : lGoals) {
//				builder.startObject();
//				builder.field("title", lGoal.getTitle());
//				builder.field("description", lGoal.getDescription());
//				builder.endObject();
//			}
			//builder.endArray();
			
			builder.field("avatar", user.getAvatarUrl());
			builder.field("position", user.getPosition());
			
			builder.startArray("roles");
			List<Role> roles = roleManager.getUserRoles(user.getEmail());
			for(Role role : roles) {
				builder.startObject();
				builder.field("id", role.getId());
				builder.endObject();
			}
			builder.endArray();
			List<CredentialData> creds = credManager.getTargetCredentialsProgressAndInstructorInfoForUser(
					user.getId(), session);
			builder.startArray("credentials");
			for(CredentialData cd : creds) {
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
				String dateString = null;
				if(date != null) {
					DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
					dateString = df.format(date);
				}
				builder.field("dateEnrolled", dateString);
				
				builder.endObject();
			}
			builder.endArray();
			
			List<CredentialData> instructorCreds = credInstructorManager
					.getCredentialIdsAndAssignDateForInstructor(user.getId());
			builder.startArray("credentialsWithInstructorRole");
			for(CredentialData cd : instructorCreds) {
				builder.startObject();
				builder.field("id", cd.getId());
				Date date = cd.getDate();
				String dateString = null;
				if(date != null) {
					DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
					dateString = df.format(date);
				}
				builder.field("dateAssigned", dateString);
				builder.endObject();
			}
			builder.endArray();
			
			builder.endObject();
			System.out.println("JSON: " + builder.prettyPrint().string());
			String indexType = getIndexTypeForNode(user);
			indexNode(builder, String.valueOf(user.getId()), ESIndexNames.INDEX_USERS, indexType);
		} catch (IOException e) {
			logger.error(e);
		}
	}
	
	@Override
	public void addCredentialToUserIndex(long credId, long userId, long instructorId, 
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
			param.put("progress", 0);
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

}
