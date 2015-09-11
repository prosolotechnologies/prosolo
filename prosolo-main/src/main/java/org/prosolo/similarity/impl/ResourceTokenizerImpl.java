package org.prosolo.similarity.impl;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.activities.Activity;
import org.prosolo.common.domainmodel.activities.CompetenceActivity;
import org.prosolo.common.domainmodel.activities.TargetActivity;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.competences.Competence;
import org.prosolo.common.domainmodel.competences.TargetCompetence;
import org.prosolo.common.domainmodel.course.Course;
import org.prosolo.common.domainmodel.course.CourseCompetence;
import org.prosolo.common.domainmodel.general.Node;
import org.prosolo.common.domainmodel.user.LearningGoal;
import org.prosolo.common.domainmodel.user.TargetLearningGoal;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.nodes.DefaultManager;
import org.prosolo.similarity.ResourceTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("org.prosolo.similarity.ResourceTokenizer")
public class ResourceTokenizerImpl implements ResourceTokenizer {
	
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(ResourceTokenizerImpl.class);
	
	@Autowired private DefaultManager defaultManager;
	
	@Override
	@Transactional(readOnly = true)
	public String getTokenizedStringForUserLearningGoal(User user, TargetLearningGoal tGoal) {
		StringBuffer userStringBuffer = new StringBuffer();
		getTokenizedStringForUserLearningGoal(user, tGoal, userStringBuffer);
		return userStringBuffer.toString();
	}
	
	@Override
	@Transactional(readOnly = true)
	public String getTokenizedStringForUser(User user) {
		StringBuffer userStringBuffer = new StringBuffer();
		user = defaultManager.merge(user);
		Set<TargetLearningGoal> tLearningGoals = user.getLearningGoals();
		
		for (TargetLearningGoal tlg : tLearningGoals) {
			getTokenizedStringForUserLearningGoal(user, tlg, userStringBuffer);
		}
		
		return userStringBuffer.toString();
	}
	
	@Override
	@Transactional(readOnly = true)
	public void getTokenizedStringForUserLearningGoal(User user, TargetLearningGoal tGoal, StringBuffer userTokensBuffer) {
		LearningGoal goal = tGoal.getLearningGoal();
		
		userTokensBuffer.append(goal.getTitle() + " ");
		userTokensBuffer.append(goal.getDescription() + " ");
		Collection<TargetCompetence> tComps = tGoal.getTargetCompetences();
		
		for (TargetCompetence tComp : tComps) {
			getTokenizedStringForTargetCompetence(tComp, userTokensBuffer);
		}
		
		getTokenizedStringForTagsAndHashtags(tGoal, userTokensBuffer);
		getTokenizedStringForTagsAndHashtags(goal, userTokensBuffer);
	}
	
	@Override
	@Transactional (readOnly = true)
	public String getTokenizedStringForCourse(Course course) {
		course = defaultManager.merge(course);
		
		StringBuffer stringBuffer = new StringBuffer();
		
		stringBuffer.append(course.getTitle() + " ");
		stringBuffer.append(course.getDescription() + " ");
		
		Collection<CourseCompetence> competences = course.getCompetences();
		
		if (competences != null && !competences.isEmpty()) {
			for (CourseCompetence courseComp : competences) {
				Competence competence = courseComp.getCompetence();
				
				stringBuffer.append(competence.getTitle() + " ");
				stringBuffer.append(competence.getDescription() + " ");
				
				for (Tag ann : competence.getTags()) {
					getTokenizedStringForAnnotation(ann, stringBuffer);
				}
				for (Tag ann : competence.getHashtags()) {
					getTokenizedStringForAnnotation(ann, stringBuffer);
				}
				
				List<CompetenceActivity> activities = competence.getActivities();
				
				if (activities != null && !activities.isEmpty()) {
					for (CompetenceActivity competenceActivity : activities) {
						Activity act = competenceActivity.getActivity();
						
						stringBuffer.append(act.getTitle() + " ");
					}
				}
			}
		}
		
		for (Tag ann : course.getTags()) {
			getTokenizedStringForAnnotation(ann, stringBuffer);
		}
		for (Tag ann : course.getHashtags()) {
			getTokenizedStringForAnnotation(ann, stringBuffer);
		}
		
		return stringBuffer.toString();
	}
	
	private void getTokenizedStringForTagsAndHashtags(Node node, StringBuffer userTokensBuffer) {
		for (Tag ann : node.getTags()) {
			getTokenizedStringForAnnotation(ann, userTokensBuffer);
		}
		for (Tag ann : node.getHashtags()) {
			getTokenizedStringForAnnotation(ann, userTokensBuffer);
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public String getTokenizedStringForTargetCompetence(TargetCompetence tCompetence) {
		StringBuffer targetCompetenceStringBuffer = new StringBuffer();
		getTokenizedStringForTargetCompetence(tCompetence, targetCompetenceStringBuffer);
		return targetCompetenceStringBuffer.toString();
	}
	
	private void getTokenizedStringForTargetCompetence(TargetCompetence tCompetence, StringBuffer userTokensBuffer) {
		userTokensBuffer.append(tCompetence.getTitle() + " ");
		userTokensBuffer.append(tCompetence.getDescription() + " ");
		
		for (TargetActivity tAct : tCompetence.getTargetActivities()) {
			getTokenizedStringForTargetActivity(tAct, userTokensBuffer);
		}
		getTokenizedStringForTagsAndHashtags(tCompetence, userTokensBuffer);
		getTokenizedStringForTagsAndHashtags(tCompetence.getCompetence(), userTokensBuffer);
	}

	private void getTokenizedStringForAnnotation(Tag tag, StringBuffer userTokensBuffer) {
		userTokensBuffer.append(tag.getTitle() + " ");
	}
	
	private void getTokenizedStringForTargetActivity(TargetActivity tActivity, StringBuffer userTokensBuffer) {
		userTokensBuffer.append(tActivity.getTitle() + " ");
	}
}
