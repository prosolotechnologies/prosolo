package org.prosolo.recommendation.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.competences.Competence;
import org.prosolo.common.domainmodel.competences.TargetCompetence;
import org.prosolo.common.domainmodel.portfolio.AchievedCompetence;
import org.prosolo.common.domainmodel.user.TargetLearningGoal;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.recommendation.CompetenceRecommendation;
import org.prosolo.services.es.MoreNodesLikeThis;
import org.prosolo.services.nodes.PortfolioManager;
import org.prosolo.similarity.ResourceTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("org.prosolo.recommendation.CompetenceRecommendation")
public class CompetenceRecommendationImpl implements CompetenceRecommendation {
	
	private static Logger logger = Logger.getLogger(CompetenceRecommendationImpl.class);
	
	@Autowired private MoreNodesLikeThis mnlt;
	@Autowired private ResourceTokenizer resTokenizer;
	@Autowired private PortfolioManager portfolioManager;
	 
	@Override
	public List<Competence> recommendCompetences(long userId, TargetLearningGoal goal, int limit) {
		String inputText = resTokenizer.getTokenizedStringForUserLearningGoal( goal);
		Collection<Competence> ignoredCompetences = new ArrayList<Competence>();
		
		// TODO: Zoran
  		for (TargetCompetence tc : goal.getTargetCompetences()) {
  			tc = mnlt.merge(tc);
  			ignoredCompetences.add(tc.getCompetence());
  		}
		
		try {
			Set<AchievedCompetence> achievedCompetences = portfolioManager.getOrCreatePortfolio(userId).getCompetences();
			for (AchievedCompetence achCompetence : achievedCompetences) {
				ignoredCompetences.add(achCompetence.getCompetence());
			}
		} catch (ResourceCouldNotBeLoadedException e) {
			logger.error(e);
		}
		
		return mnlt.getCompetencesForUserAndLearningGoal(inputText, ignoredCompetences, limit);
	}

}
