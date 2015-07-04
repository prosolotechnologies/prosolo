package org.prosolo.web.home.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.prosolo.core.hibernate.HibernateUtil;
import org.prosolo.domainmodel.activities.Recommendation;
import org.prosolo.domainmodel.activities.RecommendationType;
import org.prosolo.domainmodel.competences.Competence;
import org.prosolo.domainmodel.competences.TargetCompetence;
import org.prosolo.domainmodel.course.CourseCompetence;
import org.prosolo.domainmodel.general.BaseEntity;
import org.prosolo.domainmodel.general.Node;
import org.prosolo.domainmodel.user.LearningGoal;
import org.prosolo.domainmodel.user.User;
import org.prosolo.services.nodes.LearningGoalManager;
import org.prosolo.web.home.data.RecommendationData;
import org.prosolo.web.home.data.RecommendedResourceType;
import org.prosolo.web.home.data.ResourceAvailability;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/*
 * @author Zoran Jeremic 2013-05-25
 */
@Service("org.prosolo.web.home.util.RecommendationConverter")
public class RecommendationConverter {
	
	@Autowired private LearningGoalManager goalManager;

	public LinkedList<RecommendationData> convertRecommendationsToRecommendedData(List<Recommendation> recommendations) {
		LinkedList<RecommendationData> recommendationsData = new LinkedList<RecommendationData>();
		Session session = (Session) goalManager.getPersistence().openSession();
		try{
		for (Recommendation recommendation : recommendations) {
			RecommendationData rData = convertRecommendationToRecommendedData(recommendation, session);
			
			if (rData != null) {
				recommendationsData.add(rData);
			}
		}
		session.flush();
	} finally{
			HibernateUtil.close(session);
		} 
		return recommendationsData;
	}

	public RecommendationData convertRecommendationToRecommendedData(Recommendation recommendation, Session session) {
		RecommendationData rData = null;
		
		if (recommendation != null) {
			rData = new RecommendationData();
			rData.setMaker(recommendation.getMaker());
			rData.setMakerId(recommendation.getMaker().getId());
			rData.setDateCreated(recommendation.getDateCreated());
			rData.setRecommendation(recommendation);
			Node node = recommendation.getRecommendedResource();
			
			if (node != null) {
				node = (Node) session.merge(node);
				rData.setResourceId(node.getId());
				Hibernate.initialize(node);
				configureRecommendationType(rData, node, recommendation.getRecommendedTo());
				rData.setRecommendationType(RecommendationType.USER);
			}
		}
		return rData;
	}

	private void configureRecommendationType(RecommendationData rData, BaseEntity node, User recommendedTo) {
		node = HibernateUtil.initializeAndUnproxy(node);
		rData.setResource(node);
		rData.setResourceId(node.getId());
		rData.setDateCreated(node.getDateCreated());
		rData.setResourceTitle(node.getTitle());
	
		if (node!=null && node instanceof LearningGoal) {
			rData.setResourceType(RecommendedResourceType.LEARNINGGOAL);
			LearningGoal goal = (LearningGoal) node;
			
			if (isLearningGoalFreeToJoin(goal, recommendedTo)) {
				rData.setResourceAvailability(ResourceAvailability.IS_FREE_TO_JOIN);
			} else if (canBeRequestedToJoin(goal, recommendedTo)) {
				rData.setResourceAvailability(ResourceAvailability.CAN_BE_REQUESTED_TO_JOIN);
			}
		} else if (node instanceof TargetCompetence || (node instanceof Competence)) {
			rData.setResourceType(RecommendedResourceType.COMPETENCE);
		}
	}

	private boolean isLearningGoalFreeToJoin(LearningGoal goal,	User recommendedTo) {
		boolean memberOfGoal = goalManager.isUserMemberOfLearningGoal(goal.getId(),	recommendedTo);
		return !memberOfGoal && goal.isFreeToJoin();
	}

	private boolean canBeRequestedToJoin(LearningGoal goal, User recommendedTo) {
		boolean memberOfGoal = goalManager.isUserMemberOfLearningGoal(goal.getId(),	recommendedTo);
		return !memberOfGoal && !goal.isFreeToJoin();
	}

	public List<RecommendationData> convertNodesToRecommendedData(Collection<Node> nodes, User recommendedTo) {
		List<RecommendationData> recommendationData = new ArrayList<RecommendationData>();
		
		if (nodes != null) {
			for (Node node : nodes) {
				if (node != null) {
					RecommendationData rData = convertNodeToRecommendedData(node, recommendedTo);
					recommendationData.add(rData);
				}
			}
		}
		return recommendationData;
	}
	
	public List<RecommendationData> convertCourseCompetenceToRecommendedData(Collection<CourseCompetence> nodes, 
			User recommendedTo) {
		List<RecommendationData> recommendationData = new ArrayList<RecommendationData>();
		
		for (CourseCompetence objCompetence : nodes) {
			if (objCompetence != null) {
				RecommendationData rData = convertCourseCompetenceToRecommendedData(objCompetence, recommendedTo);
				recommendationData.add(rData);
			}
		}
		return recommendationData;
	}
	
	private RecommendationData convertCourseCompetenceToRecommendedData(CourseCompetence objCompetence, User recommendedTo) {
		RecommendationData rData = new RecommendationData();
		rData.setMaker(objCompetence.getCompetence().getMaker());
		rData.setRecommendationType(RecommendationType.COURSE);
		configureRecommendationType(rData, objCompetence, recommendedTo);
		return rData;
	}
	
	private RecommendationData convertNodeToRecommendedData(Node node, User recommendedTo) {
		RecommendationData rData = new RecommendationData();
 
		if (node.getMaker() != null){
			rData.setMaker(node.getMaker());
			rData.setMakerId(node.getMaker().getId());
		}

		rData.setRecommendationType(RecommendationType.SYSTEM);
		configureRecommendationType(rData, node, recommendedTo);
		return rData;
	}

}
