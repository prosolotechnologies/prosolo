package org.prosolo.bigdata.feeds.impl;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.transaction.Transactional;

import org.hibernate.Session;
import org.prosolo.bigdata.dal.persistence.DiggestGeneratorDAO;
import org.prosolo.bigdata.dal.persistence.impl.DiggestGeneratorDAOImpl;
import org.prosolo.bigdata.feeds.ResourceTokenizer;
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
//import org.springframework.transaction.annotation.Transactional;

public class ResourceTokenizerImpl implements ResourceTokenizer{
	//DiggestGeneratorDAO diggestGeneratorDAO=new DiggestGeneratorDAOImpl();
	public String getTokenizedStringForUserLearningGoal(User user, TargetLearningGoal tGoal) {
		StringBuffer userStringBuffer = new StringBuffer();
		getTokenizedStringForUserLearningGoal(user, tGoal, userStringBuffer);
		return userStringBuffer.toString();
	}
	
	@Override
	//@Transactional
	public String getTokenizedStringForUser(User user) {
		
		StringBuffer userStringBuffer = new StringBuffer();
	
		//User user=diggestGeneratorDAO.getEntityManager().find(User.class, userid);
		System.out.println("FOUND USER:"+user.getLastname());
		Set<TargetLearningGoal> tLearningGoals = user.getLearningGoals();
		if(tLearningGoals==null){
			System.out.println("NULL TLEARNING GOALS:"+user.getName()+" "+user.getLastname());
		}else{
			System.out.println("HAS:"+tLearningGoals.size());
		}
		for (TargetLearningGoal tlg : tLearningGoals) {
			getTokenizedStringForUserLearningGoal(user, tlg, userStringBuffer);
		}
		
		return userStringBuffer.toString();
	}
	
	private void getTokenizedStringForUserLearningGoal(User user, TargetLearningGoal tGoal, StringBuffer userTokensBuffer) {
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
	
	private void getTokenizedStringForTagsAndHashtags(Node node, StringBuffer userTokensBuffer) {
		for (Tag ann : node.getTags()) {
			getTokenizedStringForAnnotation(ann, userTokensBuffer);
		}
		for (Tag ann : node.getHashtags()) {
			getTokenizedStringForAnnotation(ann, userTokensBuffer);
		}
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
	
	@Override
	public String getTokenizedStringForCourse(Course course) {
			
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
}
