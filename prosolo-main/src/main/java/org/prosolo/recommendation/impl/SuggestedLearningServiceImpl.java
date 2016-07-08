package org.prosolo.recommendation.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.LazyInitializationException;
import org.prosolo.common.domainmodel.activities.Recommendation;
import org.prosolo.common.domainmodel.activities.RecommendationType;
import org.prosolo.common.domainmodel.general.Node;
import org.prosolo.common.domainmodel.portfolio.AchievedCompetence;
import org.prosolo.common.domainmodel.portfolio.CompletedGoal;
import org.prosolo.common.domainmodel.portfolio.Portfolio;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.recommendation.SuggestedLearningService;
import org.prosolo.recommendation.dal.SuggestedLearningQueries;
import org.prosolo.services.es.MoreNodesLikeThis;
import org.prosolo.services.nodes.CourseManager;
import org.prosolo.services.nodes.DefaultManager;
import org.prosolo.services.nodes.LearningGoalManager;
import org.prosolo.services.nodes.PortfolioManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("org.prosolo.recommendation.SuggestedLearningService")
public class SuggestedLearningServiceImpl implements SuggestedLearningService{
	
	@Autowired private SuggestedLearningQueries suggestedLearningQueries;
	@Autowired private MoreNodesLikeThis mnlt;
	@Autowired private DefaultManager defaultManager;
	@Autowired private LearningGoalManager learningGoalManager;
	@Autowired private CourseManager courseManager;
	@Autowired private PortfolioManager portfolioManager;
	 
	
	private static Logger logger = Logger.getLogger(SuggestedLearningService.class);
	
	@Override
	public List<Recommendation> findSuggestedLearningResourcesByCollegues(
			long userId, RecommendationType recType, int page, int limit) {
		return suggestedLearningQueries.findSuggestedLearningResourcesByCollegues(userId, recType, page, limit);
	}
	
	@Override
	public int findNumberOfSuggestedLearningResourcesByCollegues(long userId, RecommendationType recType){
		return suggestedLearningQueries.findNumberOfSuggestedLearningResourcesByCollegues(userId, recType);
	}

	@Override
	public List<Node> findSuggestedLearningResourcesBySystem(long userId, int limit) throws ResourceCouldNotBeLoadedException {

		if (userId == 0) {
			return null;
		}
		Collection<Node> ignoredNodes = new ArrayList<Node>();
		User user = defaultManager.loadResource(User.class, userId);
		
		try {
			ignoredNodes.addAll(user.getLearningGoals());
		} catch (LazyInitializationException exc) {
			logger.error("Couldn't initialise users's learning goals due to the LeazyInitializationException");
		}
		Portfolio portfolio = portfolioManager.getOrCreatePortfolio(userId);
		Set<CompletedGoal> completedGoals = portfolio.getCompletedGoals();
		for (CompletedGoal compGoal : completedGoals) {
			if (!ignoredNodes.contains(compGoal.getTargetGoal())) {
				ignoredNodes.add(compGoal.getTargetGoal());
			} 
		}
		Set<AchievedCompetence> achievedCompetences = portfolio.getCompetences();
		
		for (AchievedCompetence achCompetence : achievedCompetences) {
			if (!ignoredNodes.contains(achCompetence.getCompetence())) {
				ignoredNodes.add(achCompetence.getCompetence());
			}
		}
		List<Node> dismissedRecommendedResources = suggestedLearningQueries.findDismissedRecommendedResources(user);
		for(Node resource:dismissedRecommendedResources){
			if(!ignoredNodes.contains(resource)){
				ignoredNodes.add(resource);
			}
		} 
		String tokenizedStringForUser=null;
		try{
			 tokenizedStringForUser=learningGoalManager.getTokensForLearningGoalsAndCompetenceForUser(user);
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
		List<Node> suggestedResources=mnlt.getSuggestedResourcesForUser(tokenizedStringForUser, ignoredNodes, limit);
		return suggestedResources;
	}

	@Override
	public List<Node> findSuggestedLearningResourcesByCourse(long userId,
			int defaultLikeThisItemsNumber) {
		
		List<Node> competences= courseManager.getCourseCompetencesFromActiveCourse(userId);
		 
		return competences;
	}

}
