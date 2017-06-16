package org.prosolo.similarity.impl;

import org.prosolo.similarity.ResourceTokenizer;
import org.springframework.stereotype.Service;

@Deprecated
@Service("org.prosolo.similarity.ResourceTokenizer")
public class ResourceTokenizerImpl implements ResourceTokenizer {
	
//	@SuppressWarnings("unused")
//	private static Logger logger = Logger.getLogger(ResourceTokenizerImpl.class);
//	
//	@Autowired private DefaultManager defaultManager;
//	
//	@Override
//	@Transactional(readOnly = true)
//	public String getTokenizedStringForUserLearningGoal(TargetLearningGoal tGoal) {
//		StringBuffer userStringBuffer = new StringBuffer();
//		getTokenizedStringForUserLearningGoal(tGoal, userStringBuffer);
//		return userStringBuffer.toString();
//	}
//	
//	@Override
//	@Transactional(readOnly = false)
//	public String getTokenizedStringForUser(User user) {
//		StringBuffer userStringBuffer = new StringBuffer();
//		user = defaultManager.merge(user);
//		Set<TargetLearningGoal> tLearningGoals = user.getLearningGoals();
//		
//		for (TargetLearningGoal tlg : tLearningGoals) {
//			getTokenizedStringForUserLearningGoal(tlg, userStringBuffer);
//		}
//		
//		return userStringBuffer.toString();
//	}
//	
//	@Override
//	@Transactional(readOnly = true)
//	public void getTokenizedStringForUserLearningGoal(TargetLearningGoal tGoal, StringBuffer userTokensBuffer) {
//		LearningGoal goal = tGoal.getLearningGoal();
//		
//		userTokensBuffer.append(goal.getTitle() + " ");
//		userTokensBuffer.append(goal.getDescription() + " ");
//		Collection<TargetCompetence> tComps = tGoal.getTargetCompetences();
//		
//		for (TargetCompetence tComp : tComps) {
//			getTokenizedStringForTargetCompetence(tComp, userTokensBuffer);
//		}
//		
//		getTokenizedStringForTagsAndHashtags(tGoal, userTokensBuffer);
//		getTokenizedStringForTagsAndHashtags(goal, userTokensBuffer);
//	}
//	
//	@Override
//	@Transactional (readOnly = true)
//	public String getTokenizedStringForCourse(Course course) {
//		course = defaultManager.merge(course);
//		
//		StringBuffer stringBuffer = new StringBuffer();
//		
//		stringBuffer.append(course.getTitle() + " ");
//		stringBuffer.append(course.getDescription() + " ");
//		
//		Collection<CourseCompetence> competences = course.getCompetences();
//		
//		if (competences != null && !competences.isEmpty()) {
//			for (CourseCompetence courseComp : competences) {
//				Competence competence = courseComp.getCompetence();
//				
//				stringBuffer.append(competence.getTitle() + " ");
//				stringBuffer.append(competence.getDescription() + " ");
//				
//				for (Tag ann : competence.getTags()) {
//					getTokenizedStringForAnnotation(ann, stringBuffer);
//				}
//				for (Tag ann : competence.getHashtags()) {
//					getTokenizedStringForAnnotation(ann, stringBuffer);
//				}
//				
//				List<CompetenceActivity> activities = competence.getActivities();
//				
//				if (activities != null && !activities.isEmpty()) {
//					for (CompetenceActivity competenceActivity : activities) {
//						Activity act = competenceActivity.getActivity();
//						
//						stringBuffer.append(act.getTitle() + " ");
//					}
//				}
//			}
//		}
//		
//		for (Tag ann : course.getTags()) {
//			getTokenizedStringForAnnotation(ann, stringBuffer);
//		}
//		for (Tag ann : course.getHashtags()) {
//			getTokenizedStringForAnnotation(ann, stringBuffer);
//		}
//		
//		return stringBuffer.toString();
//	}
//	
//	private void getTokenizedStringForTagsAndHashtags(Node node, StringBuffer userTokensBuffer) {
//		for (Tag ann : node.getTags()) {
//			getTokenizedStringForAnnotation(ann, userTokensBuffer);
//		}
//		for (Tag ann : node.getHashtags()) {
//			getTokenizedStringForAnnotation(ann, userTokensBuffer);
//		}
//	}
//	
//	@Override
//	@Transactional(readOnly = true)
//	public String getTokenizedStringForTargetCompetence(TargetCompetence tCompetence) {
//		StringBuffer targetCompetenceStringBuffer = new StringBuffer();
//		getTokenizedStringForTargetCompetence(tCompetence, targetCompetenceStringBuffer);
//		return targetCompetenceStringBuffer.toString();
//	}
//	
//	private void getTokenizedStringForTargetCompetence(TargetCompetence tCompetence, StringBuffer userTokensBuffer) {
//		userTokensBuffer.append(tCompetence.getTitle() + " ");
//		userTokensBuffer.append(tCompetence.getDescription() + " ");
//		
//		for (TargetActivity tAct : tCompetence.getTargetActivities()) {
//			getTokenizedStringForTargetActivity(tAct, userTokensBuffer);
//		}
//		getTokenizedStringForTagsAndHashtags(tCompetence, userTokensBuffer);
//		getTokenizedStringForTagsAndHashtags(tCompetence.getCompetence(), userTokensBuffer);
//	}
//
//	private void getTokenizedStringForAnnotation(Tag tag, StringBuffer userTokensBuffer) {
//		userTokensBuffer.append(tag.getTitle() + " ");
//	}
//	
//	private void getTokenizedStringForTargetActivity(TargetActivity tActivity, StringBuffer userTokensBuffer) {
//		userTokensBuffer.append(tActivity.getTitle() + " ");
//	}
}
