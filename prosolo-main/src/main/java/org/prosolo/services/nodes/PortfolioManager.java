package org.prosolo.services.nodes;

import java.util.Date;
import java.util.List;

import org.hibernate.Session;
import org.prosolo.common.domainmodel.activities.TargetActivity;
import org.prosolo.common.domainmodel.competences.Competence;
import org.prosolo.common.domainmodel.competences.TargetCompetence;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.portfolio.AchievedCompetence;
import org.prosolo.common.domainmodel.portfolio.CompletedGoal;
import org.prosolo.common.domainmodel.portfolio.CompletedResource;
import org.prosolo.common.domainmodel.portfolio.ExternalCredit;
import org.prosolo.common.domainmodel.portfolio.Portfolio;
import org.prosolo.common.domainmodel.user.TargetLearningGoal;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.common.exception.DbConnectionException;
import org.prosolo.services.general.AbstractManager;
import org.prosolo.services.nodes.impl.PortfolioData;

public interface PortfolioManager extends AbstractManager {
	
	Portfolio getOrCreatePortfolio(User user);
	
	CompletedResource getCompletedResource(User user, BaseEntity resource);
	
	/*
	 * completed goals
	 */
	PortfolioData sendGoalToPortfolio(long targetGoalId, User user) throws ResourceCouldNotBeLoadedException;
	
	TargetLearningGoal sendBackToGoals(long targetGoalId, User user, String context) throws ResourceCouldNotBeLoadedException;
	
	AchievedCompetence sendCompetenceToPortfolio(TargetCompetence goal, User user);
	
	List<TargetLearningGoal> getPublicOngoingTargetLearningGoals(User user);
	
	List<TargetLearningGoal> getOngoingTargetLearningGoals(User user);
	
	List<CompletedGoal> getPublicCompletedArchivedGoals(User user);
	
	List<CompletedGoal> getCompletedGoals(User user);
	
	boolean isGoalRetaken(User user, CompletedGoal goal);
	
	boolean hasUserCompletedGoal(User user, long goalId);
	
	List<TargetLearningGoal> getPublicCompletedNonarchivedLearningGoals(User user);
	
	/*
	 * achieved competences
	 */
	List<AchievedCompetence> getAchievedCompetences(User user);
	
	List<AchievedCompetence> getPublicAchievedCompetences(User user);
	
	long getNumberOfUsersHavingCompetences(Competence competence);
	
	TargetCompetence getTargetCompetenceOfAchievedCompetence(long achievedCompetenceId);
	
	TargetCompetence getTargetCompetenceOfAchievedCompetence(long resourceId, Session session);
	
	boolean isCompetenceCompleted(long competenceId, User user);
	
	boolean isCompetenceCompleted(Competence competence, User user);
	
	long getNumberOfCompletedCompetences(List<Competence> allCompetences, User user);
	
	List<TargetCompetence> getPublicCompletedNonarchivedTargetCompetences(User user);

	List<TargetCompetence> getPublicOngoingTargetCompetences(User user);
	
	/*
	 * external credits
	 */
	ExternalCredit createExternalCredit(User user, String title, String description, String certificateLink, Date start, Date end, 
			List<TargetActivity> activities, List<Competence> competences, String context);
	
	List<ExternalCredit> deleteExternalCredit(User user, ExternalCredit externalCredit, String context);
	
	List<ExternalCredit> getExternalCredits(User user);
	
	List<ExternalCredit> getVisibleExternalCredits(User profileOwner);
	
	ExternalCredit updateExternalCredit(ExternalCredit externalCredit, User user, String title, String description, Date start, 
			Date end, String certificateLink, List<Competence> competences, List<TargetActivity> activities);

	public List<TargetLearningGoal> getAllArchivedGoals(long userId) throws DbConnectionException;
	
	public List<TargetLearningGoal> getAllNonArchivedGoals(long userId) throws DbConnectionException;
	
	public List<TargetLearningGoal> getAllGoals(long userId) throws DbConnectionException;
}
