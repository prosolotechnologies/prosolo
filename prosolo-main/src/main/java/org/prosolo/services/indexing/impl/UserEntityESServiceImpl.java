package org.prosolo.services.indexing.impl;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.user.LearningGoal;
import org.prosolo.common.domainmodel.user.TargetLearningGoal;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.indexing.AbstractBaseEntityESServiceImpl;
import org.prosolo.services.indexing.ESIndexNames;
import org.prosolo.services.indexing.UserEntityESService;
import org.prosolo.services.nodes.CourseManager;
import org.prosolo.services.nodes.LearningGoalManager;
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
	private CourseManager courseManager;
	@Inject
	private LearningGoalManager learningGoalManager;
	
	@Override
	@Transactional
	public void saveUserNode(User user, Session session) {
 //	user = (User) session.merge(user);
		if(user!=null)
	 		try {
	 		List<Map<String, Object>> courseInfo = courseManager
	 				.getUserCoursesWithProgressAndInstructorInfo(user.getId(), session);
	 		
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
			builder.startArray("learninggoals");
			
			List<TargetLearningGoal> targetLearningGoals = learningGoalManager.getUserTargetGoals(user, session);
			//Set<TargetLearningGoal> targetLearningGoals=user.getLearningGoals();
			for(TargetLearningGoal tGoal: targetLearningGoals){
				LearningGoal lGoal=tGoal.getLearningGoal();
				builder.startObject();
 				builder.field("title", lGoal.getTitle());
 				builder.field("description", lGoal.getDescription());
 				builder.endObject();
			}
//			Set<LearningGoal> lGoals = user.getLearningGoals();
//			
//			for (LearningGoal lGoal : lGoals) {
//				builder.startObject();
//				builder.field("title", lGoal.getTitle());
//				builder.field("description", lGoal.getDescription());
//				builder.endObject();
//			}
			builder.endArray();
			
			builder.field("avatar", user.getAvatarUrl());
			builder.field("position", user.getPosition());
			
			builder.startArray("courses");
			for(Map<String, Object> resMap : courseInfo) {
				builder.startObject();
				long courseId = (long) resMap.get("course");
				builder.field("id", courseId);
				int courseProgress = (int) resMap.get("courseProgress");
				builder.field("progress", courseProgress);
				//change when user profile types are implemented
				builder.startObject("profile");
				builder.field("profileType", "A");
				builder.field("profileTitle", "PROFILE 1");
				builder.endObject();
				Long instructorId = (Long) resMap.get("instructorId");
				instructorId = instructorId != null ? instructorId : 0;
				builder.field("instructorId", instructorId);
				
				builder.endObject();
			}
			builder.endArray();
			
			List<Long> courseIds = courseManager.getCourseIdsForInstructor(user.getId());
			builder.startArray("coursesWithInstructorRole");
			for(long id : courseIds) {
				builder.startObject();
				builder.field("id", id);
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
}
