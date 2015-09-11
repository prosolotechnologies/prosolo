package org.prosolo.services.indexing.impl;

import java.io.IOException;
import java.util.Set;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Zoran Jeremic 2013-06-29
 *
 */
@Service("org.prosolo.services.indexing.UserEntityESService")
public class UserEntityESServiceImpl extends AbstractBaseEntityESServiceImpl implements UserEntityESService{
	
	private static Logger logger = Logger.getLogger(UserEntityESService.class);
	
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
			builder.startArray("learninggoals");
			
			// TODO: Zoran - here you need to iterate through target learnign goals
			Set<TargetLearningGoal> targetLearningGoals=user.getLearningGoals();
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
			builder.endObject();
			String indexType = getIndexTypeForNode(user);
			indexNode(builder, String.valueOf(user.getId()), ESIndexNames.INDEX_USERS,indexType);
		} catch (IOException e) {
			logger.error(e);
		}
	}
}
