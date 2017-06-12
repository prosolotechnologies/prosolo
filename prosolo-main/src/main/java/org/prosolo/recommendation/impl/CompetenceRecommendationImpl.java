package org.prosolo.recommendation.impl;

/**
 * @author Zoran Jeremic
 * @version 0.5
 * @deprecated since 0.7
 */
@Deprecated
//@Service("org.prosolo.recommendation.CompetenceRecommendation")
public class CompetenceRecommendationImpl 
	//implements CompetenceRecommendation 
{
//	
//	private static Logger logger = Logger.getLogger(CompetenceRecommendationImpl.class);
//	
//	@Autowired private MoreNodesLikeThis mnlt;
//	@Autowired private ResourceTokenizer resTokenizer;
//	@Autowired private PortfolioManager portfolioManager;
//	 
//	@Override
//	public List<Competence> recommendCompetences(long userId, TargetLearningGoal goal, int limit) {
//		String inputText = resTokenizer.getTokenizedStringForUserLearningGoal( goal);
//		Collection<Competence> ignoredCompetences = new ArrayList<Competence>();
//		
//		// TODO: Zoran
//  		for (TargetCompetence tc : goal.getTargetCompetences()) {
//  			tc = mnlt.merge(tc);
//  			ignoredCompetences.add(tc.getCompetence());
//  		}
//		
//		try {
//			Set<AchievedCompetence> achievedCompetences = portfolioManager.getOrCreatePortfolio(userId).getCompetences();
//			for (AchievedCompetence achCompetence : achievedCompetences) {
//				ignoredCompetences.add(achCompetence.getCompetence());
//			}
//		} catch (ResourceCouldNotBeLoadedException e) {
//			logger.error(e);
//		}
//		
//		return mnlt.getCompetencesForUserAndLearningGoal(inputText, ignoredCompetences, limit);
//	}

}
