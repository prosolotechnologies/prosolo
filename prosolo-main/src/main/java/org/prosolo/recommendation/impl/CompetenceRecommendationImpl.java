package org.prosolo.recommendation.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.prosolo.domainmodel.competences.Competence;
import org.prosolo.domainmodel.competences.TargetCompetence;
import org.prosolo.domainmodel.portfolio.AchievedCompetence;
import org.prosolo.domainmodel.user.TargetLearningGoal;
import org.prosolo.domainmodel.user.User;
import org.prosolo.recommendation.CompetenceRecommendation;
import org.prosolo.services.es.MoreNodesLikeThis;
import org.prosolo.services.nodes.PortfolioManager;
import org.prosolo.similarity.ResourceTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("org.prosolo.recommendation.CompetenceRecommendation")
public class CompetenceRecommendationImpl implements CompetenceRecommendation {
	
	@Autowired private MoreNodesLikeThis mnlt;
	@Autowired private ResourceTokenizer resTokenizer;
	@Autowired private PortfolioManager portfolioManager;
	 
	@Override
	public List<Competence> recommendCompetences(User user, TargetLearningGoal goal, int limit) {
		String inputText = resTokenizer.getTokenizedStringForUserLearningGoal(user, goal);
		Collection<Competence> ignoredCompetences = new ArrayList<Competence>();
		
		// TODO: Zoran
  		for (TargetCompetence tc : goal.getTargetCompetences()) {
  			tc = mnlt.merge(tc);
  			ignoredCompetences.add(tc.getCompetence());
  		}
		
		Set<AchievedCompetence> achievedCompetences = portfolioManager.getOrCreatePortfolio(user).getCompetences();
		
		for (AchievedCompetence achCompetence : achievedCompetences) {
			ignoredCompetences.add(achCompetence.getCompetence());
		}
		return mnlt.getCompetencesForUserAndLearningGoal(inputText, ignoredCompetences, limit);
	}

}
