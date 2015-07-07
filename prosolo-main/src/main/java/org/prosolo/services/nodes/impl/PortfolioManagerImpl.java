package org.prosolo.services.nodes.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.prosolo.common.domainmodel.activities.TargetActivity;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.competences.Competence;
import org.prosolo.common.domainmodel.competences.TargetCompetence;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.organization.VisibilityType;
import org.prosolo.common.domainmodel.portfolio.AchievedCompetence;
import org.prosolo.common.domainmodel.portfolio.CompletedGoal;
import org.prosolo.common.domainmodel.portfolio.CompletedResource;
import org.prosolo.common.domainmodel.portfolio.ExternalCredit;
import org.prosolo.common.domainmodel.portfolio.Portfolio;
import org.prosolo.common.domainmodel.user.TargetLearningGoal;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.general.impl.AbstractManagerImpl;
import org.prosolo.services.nodes.PortfolioManager;
import org.prosolo.services.nodes.ResourceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("org.prosolo.services.nodes.PortfolioManager")
public class PortfolioManagerImpl extends AbstractManagerImpl implements PortfolioManager {
	
	private static final long serialVersionUID = 6784778082388715327L;

	private static Logger logger = Logger.getLogger(PortfolioManagerImpl.class);
	
	@Autowired private ResourceFactory resourceFactory;
	@Autowired private EventFactory eventFactory;
	//@Autowired private LoggingNavigationBean loggingNavigationBean;
	
	@Override
	@Transactional (readOnly = false)
	public Portfolio getOrCreatePortfolio(User user) {
		String query = 
			"SELECT DISTINCT portfolio " +
			"FROM Portfolio portfolio " +
			"LEFT JOIN portfolio.user user " +
			"WHERE user = :user ";
		
		Portfolio portfolio = (Portfolio) persistence.currentManager().createQuery(query).
			setEntity("user",user).
			uniqueResult();
		
		if (portfolio == null) {
			portfolio = new Portfolio();
			portfolio.setUser(user);
			portfolio = saveEntity(portfolio);
		}
		
		return portfolio;
	}
	
	@Override
	@Transactional (readOnly = true)
	public List<TargetLearningGoal> getOngoingTargetLearningGoals(User user) {
		String query = 
			"SELECT DISTINCT tGoal " +
			"FROM User user " +
			"LEFT JOIN user.learningGoals tGoal " +
			"LEFT JOIN FETCH tGoal.learningGoal goal " +
			"WHERE user = :user " +
				"AND tGoal.deleted = :deleted " +
			"ORDER BY tGoal.dateCreated ASC";
		
		@SuppressWarnings("unchecked")
		List<TargetLearningGoal> result = persistence.currentManager().createQuery(query)
			.setEntity("user", user)
			.setBoolean("deleted", false)
			.list();
		
		return result;
	}
	
	@Override
	@Transactional (readOnly = true)
	public List<TargetLearningGoal> getPublicOngoingTargetLearningGoals(User user) {
		String query = 
			"SELECT DISTINCT tGoal " +
			"FROM User user " +
			"LEFT JOIN user.learningGoals tGoal " +
			"LEFT JOIN FETCH tGoal.learningGoal goal " +
			"WHERE user = :user " +
				"AND tGoal.progress < 100 " +
				"AND tGoal.deleted = :deleted " +
				"AND tGoal.visibility != :visibility " +
			"ORDER BY tGoal.dateCreated ASC";
		
		@SuppressWarnings("unchecked")
		List<TargetLearningGoal> result = persistence.currentManager().createQuery(query)
			.setEntity("user", user)
			.setBoolean("deleted", false)
			.setParameter("visibility", VisibilityType.PRIVATE)
			.list();
		
		return result;
	}
	
	@Override
	@Transactional (readOnly = true)
	public List<TargetLearningGoal> getPublicCompletedNonarchivedLearningGoals(User user) {
		String query = 
			"SELECT DISTINCT tGoal " +
			"FROM User user " +
			"LEFT JOIN user.learningGoals tGoal " +
			"LEFT JOIN FETCH tGoal.learningGoal goal " +
			"WHERE user = :user " +
				"AND tGoal.progress = 100 " +
				"AND tGoal.deleted = :deleted " +
				"AND tGoal.visibility != :visibility " +
			"ORDER BY tGoal.dateCreated ASC";
		
		@SuppressWarnings("unchecked")
		List<TargetLearningGoal> result = persistence.currentManager().createQuery(query)
			.setEntity("user", user)
			.setBoolean("deleted", false)
			.setParameter("visibility", VisibilityType.PRIVATE)
			.list();
		
		return result;
	}
	
	@Override
	@Transactional (readOnly = true)
	public List<CompletedGoal> getPublicCompletedArchivedGoals(User user) {
		String query = 
			"SELECT DISTINCT completedGoal " +
			"FROM Portfolio portfolio " +
			"LEFT JOIN portfolio.user user " +
			"LEFT JOIN portfolio.completedGoals completedGoal " +
			"WHERE user = :user " +
				"AND completedGoal.retaken = :retaken " +
				"AND completedGoal.visibility != :visibility " +
			"ORDER BY completedGoal.dateCreated";
 
		@SuppressWarnings("unchecked")
		List<CompletedGoal> result = persistence.currentManager().createQuery(query).setEntity("user",user)
				.setBoolean("retaken", false)
				.setParameter("visibility", VisibilityType.PRIVATE)
				.list();
		
		return result;
	}
	
	@Override
	@Transactional (readOnly = true)
	public List<CompletedGoal> getCompletedGoals(User user) {
		String query = 
			"SELECT DISTINCT completedGoal " +
			"FROM Portfolio portfolio " +
			"LEFT JOIN portfolio.user user " +
			"LEFT JOIN portfolio.completedGoals completedGoal " +
			"WHERE user = :user " +
				"AND completedGoal.retaken = :retaken " +
			"ORDER BY completedGoal.dateCreated";
		
		@SuppressWarnings("unchecked")
		List<CompletedGoal> result = persistence.currentManager().createQuery(query).setEntity("user",user).
			setBoolean("retaken", false)
			.list();
		
		if (result != null) {
			return result;
		} else {
			return new ArrayList<CompletedGoal>();
		}
	}

	@Override
	@Transactional(readOnly = false)
	public PortfolioData sendGoalToPortfolio(long targetGoalId, User user) throws ResourceCouldNotBeLoadedException {
		logger.debug("Sending goal "+targetGoalId+" to portfolio of a user "+user);
		
		PortfolioData portfolioData = new PortfolioData();
		
		if (user != null) {
			TargetLearningGoal targetGoal = loadResource(TargetLearningGoal.class, targetGoalId);
			Set<TargetLearningGoal> userGoals = user.getLearningGoals();
			
			if (userGoals.contains(targetGoal)) {
				userGoals.remove(targetGoal);
				user = saveEntity(user);
			}
			
			CompletedGoal completedGoal = getCompletedGoal(user, targetGoal);
			
			if (completedGoal == null) {
				Portfolio portfolio = getOrCreatePortfolio(user);
				completedGoal = createCompletedGoal(targetGoal, user);
				portfolio.addCompletedGoal(completedGoal);
				portfolio = saveEntity(portfolio);
			} else {
				completedGoal.setRetaken(false);
				completedGoal = saveEntity(completedGoal);
			}
			
			portfolioData.addCompletedGoal(completedGoal);
			
			Collection<TargetCompetence> targetComps = targetGoal.getTargetCompetences();
			
			if (targetComps != null && !targetComps.isEmpty()) {
				for (TargetCompetence targetComp : targetComps) {
					AchievedCompetence achievedComp = sendCompetenceToPortfolio(targetComp, user);
					portfolioData.addAchievedCompetence(achievedComp);
				}
			}
			return portfolioData;
		}
		return null;
	}

	@Transactional (readOnly = false)
	private CompletedGoal createCompletedGoal(TargetLearningGoal targetGoal, User user) {
		CompletedGoal completedGoal = new CompletedGoal();
		completedGoal.setTitle(targetGoal.getTitle());
		completedGoal.setDateCreated(new Date());
		completedGoal.setCompletedDate(targetGoal.getCompletedDate() != null ? targetGoal.getCompletedDate() : new Date());
		completedGoal.setMaker(user);
		completedGoal.setVisibility(targetGoal.getVisibility());
		completedGoal.setTargetGoal(targetGoal);
		completedGoal = saveEntity(completedGoal);
		return completedGoal;
	}
	
	@Override
	@Transactional(readOnly = false)
	public TargetLearningGoal sendBackToGoals(long targetGoalId, User user, String context) throws ResourceCouldNotBeLoadedException {
//		LearningGoal retakenGoal = resourceFactory.createLearningGoal(user, completedGoal);
		TargetLearningGoal targetOriginalGoal = loadResource(TargetLearningGoal.class, targetGoalId);
		
		targetOriginalGoal.setProgress(0);
		targetOriginalGoal.setCompletedDate(null);
		targetOriginalGoal = saveEntity(targetOriginalGoal);
		
		Collection<TargetCompetence> targetCompetences = targetOriginalGoal.getTargetCompetences();
		
		if (targetCompetences != null && !targetCompetences.isEmpty()) {
			for (TargetCompetence targetCompetence : targetCompetences) {
				
				Collection<TargetActivity> targetActivities = targetCompetence.getTargetActivities();
				
				targetCompetence.setCompleted(false);
				targetCompetence.setCompletedDay(null);
				
				if (targetActivities != null && !targetActivities.isEmpty()) {
					for (TargetActivity targetActivity : targetActivities) {
						targetActivity.setCompleted(false);
						targetActivity.setDateCompleted(null);
						saveEntity(targetActivity);
					}
				}
			}
		}
		
		user.addLearningGoal(targetOriginalGoal);
		user = saveEntity(user);
		
		Portfolio portfolio = getOrCreatePortfolio(user);
		
		Set<CompletedGoal> goals = portfolio.getCompletedGoals();
		
		if (goals != null && !goals.isEmpty()) {
			Iterator<CompletedGoal> iterator = goals.iterator();
			
			while (iterator.hasNext()) {
				CompletedGoal completedGoal = (CompletedGoal) iterator.next();

				if (completedGoal.getTargetGoal().getId() == targetGoalId) {
					iterator.remove();
				}
			}
		}
		
		saveEntity(portfolio);
		
		try {
			Map<String, String> parameters = new HashMap<String, String>();
			parameters.put("context", context);
			
			eventFactory.generateEvent(EventType.SEND_TO_LEARN, user, targetOriginalGoal, parameters);
		} catch (EventException e) {
			logger.error(e);
		}
		
		return targetOriginalGoal;
	}
	
	@Override
	@Transactional (readOnly = true)
	public boolean isGoalRetaken(User user, CompletedGoal goal) {
		String query = 
			"SELECT COUNT(DISTINCT completedGoal) " +
			"FROM Portfolio portfolio " +
			"LEFT JOIN portfolio.user user " +
			"LEFT JOIN portfolio.completedGoals completedGoal " +
			"WHERE user = :user " +
				"AND completedGoal.goal = :goal " +
				"AND completedGoal.retaken = :retaken";
		
		Long count = (Long) persistence.currentManager().createQuery(query).
				setEntity("user", user).
				setEntity("goal", goal).
				setBoolean("retaken", true).
				uniqueResult();
		
		return count > 0;
	}
	
	@Override
	@Transactional (readOnly = false)
	public AchievedCompetence sendCompetenceToPortfolio(TargetCompetence tComp, User user) {
		logger.debug("Sending targetCompetence "+tComp+" to portfolio of a user "+user);
		
		AchievedCompetence achievedCompetence = getAchievedCompetence(user, tComp.getCompetence());
		
		if (tComp != null && user != null && achievedCompetence == null) {
			achievedCompetence = createAchievedCompetence(tComp, user);
			
			Portfolio portfolio = getOrCreatePortfolio(user);
			portfolio.addAchievedCompetence(achievedCompetence);
			saveEntity(portfolio);
		}
		return achievedCompetence;
	}

	@Transactional (readOnly = false)
	private AchievedCompetence createAchievedCompetence(Competence comp, TargetCompetence tComp, User user) {
		AchievedCompetence achievedCompetence = new AchievedCompetence();
		achievedCompetence.setMaker(user);
		achievedCompetence.setCompletedDate(new Date());
		achievedCompetence.setTargetCompetence(tComp);
		achievedCompetence.setCompetence(comp);
		achievedCompetence.setTitle(comp.getTitle());
		achievedCompetence.setVisibility(VisibilityType.PUBLIC);
		achievedCompetence = saveEntity(achievedCompetence);
		return achievedCompetence;
	}
	
	@Transactional (readOnly = false)
	private AchievedCompetence createAchievedCompetence(TargetCompetence tComp, User user) {
		AchievedCompetence achievedCompetence = new AchievedCompetence();
		achievedCompetence.setMaker(user);
		achievedCompetence.setCompletedDate(new Date());
		achievedCompetence.setTargetCompetence(tComp);
		achievedCompetence.setCompetence(tComp.getCompetence());
		achievedCompetence.setTitle(tComp.getCompetence().getTitle());
		achievedCompetence.setVisibility(VisibilityType.PUBLIC);
		achievedCompetence = saveEntity(achievedCompetence);
		return achievedCompetence;
	}
	
	@Override
	@Transactional
	public CompletedResource getCompletedResource(User user, BaseEntity resource) {
		if (user != null && resource != null) {
			user = merge(user);
			
			if (resource instanceof CompletedResource)
				return (CompletedResource) merge(resource);
		
			if (resource instanceof Competence || resource instanceof TargetCompetence) {
				Competence comp = null;
				TargetCompetence tComp = null;
				
				if (resource instanceof Competence) {
					comp = (Competence) resource;
				} else if (resource instanceof TargetCompetence) {
					tComp = (TargetCompetence) resource;
					comp = ((TargetCompetence) resource).getCompetence();
				}
				AchievedCompetence achievedComp = getAchievedCompetence(user, comp);
				
				if (achievedComp == null) {
					achievedComp = createAchievedCompetence(comp, tComp, user);
					
					Portfolio portfolio = getOrCreatePortfolio(user);
					portfolio.addAchievedCompetence(achievedComp);
					saveEntity(portfolio);
				}
				return achievedComp;
			} else if (resource instanceof TargetLearningGoal) {
				CompletedGoal compGoal = getCompletedGoal(user, (TargetLearningGoal) resource);
				
				if (compGoal == null) {
					compGoal = createCompletedGoal((TargetLearningGoal) resource, user);
				}
				return compGoal;
			}
		}
		return null;
	}
	
	@Transactional (readOnly = true)
	private AchievedCompetence getAchievedCompetence(User user, Competence comp) {
		String query = 
			"SELECT DISTINCT acComp " +
			"FROM AchievedCompetence acComp " +
			"WHERE acComp.maker = :user " +
				"AND acComp.competence = :competence";
		
		AchievedCompetence result = (AchievedCompetence) persistence.currentManager().createQuery(query).
				setEntity("user", user).
				setEntity("competence", comp).
				uniqueResult();
		
		return result;
	}
	
	@Transactional (readOnly = true)
	private CompletedGoal getCompletedGoal(User user, TargetLearningGoal targetGoal) {
		String query = 
			"SELECT DISTINCT compGoal " +
			"FROM CompletedGoal compGoal " +
			"WHERE compGoal.maker = :user " +
				"AND compGoal.targetGoal = :targetGoal";
		
		CompletedGoal result = (CompletedGoal) persistence.currentManager().createQuery(query).
				setEntity("user", user).
				setEntity("targetGoal", targetGoal).
				uniqueResult();
		
		if (result != null) {
			return result;
		}
		
		return null;
	}
	
	@Override
	@Transactional (readOnly = true)
	public boolean hasUserCompletedGoal(User user, long goalId) {
		String query = 
			"SELECT COUNT(DISTINCT compGoal) " +
			"FROM Portfolio portfolio " +
			"LEFT JOIN portfolio.user user " +
			"LEFT JOIN portfolio.completedGoals compGoal " +
			"LEFT JOIN compGoal.targetGoal targetGoal " +
			"LEFT JOIN targetGoal.learningGoal goal " +
			"WHERE user = :user " +
				"AND goal.id = :goalId";
		
		Long result = (Long) persistence.currentManager().createQuery(query)
				.setLong("goalId", goalId)
				.setEntity("user",user).uniqueResult();
		
		return result > 0;
	}
	
	/*
	 * achieved competences
	 */
	@Override
	@Transactional (readOnly = true)
	public List<AchievedCompetence> getAchievedCompetences(User user) {
		String query = 
			"SELECT aComp " +
			"FROM Portfolio portfolio " +
			"LEFT JOIN portfolio.user user " +
			"LEFT JOIN portfolio.competences aComp " +
			"WHERE user = :user AND " +
				"aComp.deleted = false";
		
		@SuppressWarnings("unchecked")
		List<AchievedCompetence> result = persistence.currentManager().createQuery(query)
			.setEntity("user",user)
			.list();
		
		if (result != null) {
			return result;
		}
		return new ArrayList<AchievedCompetence>();
	}
	
	@Override
	@Transactional
	public ExternalCredit createExternalCredit(User user, 
			String title, String description, 
			String certificateLink, Date start, Date end,
			List<TargetActivity> targetActivities,
			List<Competence> competences, String context) {
		
		ExternalCredit externalCredit = new ExternalCredit();
		externalCredit.setTitle(title);
		externalCredit.setDescription(description);
		externalCredit.setCertificateLink(certificateLink);
		externalCredit.setStart(start);
		externalCredit.setEnd(end);
		externalCredit.setVisibility(VisibilityType.PUBLIC);
		
		for (TargetActivity act : targetActivities) {
			externalCredit.addTargetActivity(saveEntity(act));
		}
		for (Competence comp : competences) {
			AchievedCompetence achievedComp = (AchievedCompetence) getCompletedResource(user, comp);
			externalCredit.addAchievedCompetence(achievedComp);
		}
		
		externalCredit = saveEntity(externalCredit);
		
		Portfolio portfolio = getOrCreatePortfolio(user);
		portfolio.addExternalCredit(externalCredit);
		saveEntity(portfolio);
		
		try {
			Map<String, String> parameters = new HashMap<String, String>();
			parameters.put("context", context);
			
			eventFactory.generateEvent(EventType.Create, user, externalCredit, parameters);
		} catch (EventException e) {
			logger.error(e);
		}
		
		return externalCredit;
	}
	
	@Override
	@Transactional
	public List<ExternalCredit> deleteExternalCredit(User user, ExternalCredit externalCredit, String context) {
		Portfolio portfolio = getOrCreatePortfolio(user);
		portfolio.removeExternalCredit(externalCredit);
		saveEntity(portfolio);
		
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("context", context);
		
		try {
			eventFactory.generateEvent(EventType.Delete, user, externalCredit, parameters);
		} catch (EventException e) {
			logger.error(e);
		}
		
		markAsDeleted(externalCredit);
		return portfolio.getExternalCredits();
	}
	
	@Override
	@Transactional (readOnly = true)
	public List<ExternalCredit> getExternalCredits(User user) {
		String query = 
			"SELECT exCredit " +
			"FROM Portfolio portfolio " +
			"LEFT JOIN portfolio.user user " +
			"LEFT JOIN portfolio.externalCredits exCredit " +
			"WHERE user = :user AND " +
				"exCredit.deleted = false";
		
		@SuppressWarnings("unchecked")
		List<ExternalCredit> result = persistence.currentManager().createQuery(query)
			.setEntity("user",user)
			.list();
		
		if (result != null) {
			return result;
		}
		return new ArrayList<ExternalCredit>();
	}
	
	@Override
	@Transactional (readOnly = true)
	public List<ExternalCredit> getVisibleExternalCredits(User profileOwner) {
		String query = 
			"SELECT exCredit " +
			"FROM Portfolio portfolio " +
			"LEFT JOIN portfolio.user user " +
			"LEFT JOIN portfolio.externalCredits exCredit " +
			"WHERE user = :user " +
				"AND exCredit.visibility != :visibility " +
				"AND exCredit.deleted = :deleted";
		
		@SuppressWarnings("unchecked")
		List<ExternalCredit> result = persistence.currentManager().createQuery(query)
			.setEntity("user", profileOwner)
			.setParameter("visibility", VisibilityType.PRIVATE)
			.setBoolean("deleted", false)
			.list();
		
		if (result != null) {
			return result;
		}
		return new ArrayList<ExternalCredit>();
	}
	
	@Override
	@Transactional
	public ExternalCredit updateExternalCredit(ExternalCredit externalCredit,
			User user,
			String title, String description, Date start, Date end,
			String certificateLink, List<Competence> competences,
			List<TargetActivity> activities) {
		
		externalCredit = merge(externalCredit);
		
		externalCredit.setTitle(title);
		externalCredit.setDescription(description);
		externalCredit.setCertificateLink(certificateLink);
		externalCredit.setStart(start);
		externalCredit.setEnd(end);
		
		//saving new activities and removing the ones user has removed
//		List<TargetActivity> newActivities = new ArrayList<TargetActivity>(activities);
//		newActivities.removeAll(externalCredit.getTargetActivities());
//		
//		List<TargetActivity> oldActivities = new ArrayList<TargetActivity>(externalCredit.getTargetActivities());
//		oldActivities.removeAll(activities);
		
		externalCredit.getTargetActivities().clear();
		
		for (TargetActivity act : activities) {
			externalCredit.addTargetActivity(saveEntity(act));
		}
		
//		for (TargetActivity act : oldActivities) {
//			externalCredit.removeActivity(act);
//		}
		
		externalCredit.getCompetences().clear();
		
		for (Competence comp : competences) {
			AchievedCompetence achievedComp = (AchievedCompetence) getCompletedResource(user, comp);
			externalCredit.addAchievedCompetence(achievedComp);
		}
		
		return saveEntity(externalCredit);
	}
	
	@Override
	@Transactional (readOnly = true)
	public long getNumberOfUsersHavingCompetences(Competence competence) {
		String query = 
			"SELECT COUNT(DISTINCT user) " +
			"FROM Portfolio portfolio " +
			"LEFT JOIN portfolio.user user " +
			"LEFT JOIN portfolio.competences aComp " +
			"LEFT JOIN aComp.competence comp " +
			"WHERE comp = :comp ";
			
		Long number = (Long) persistence.currentManager().createQuery(query)
				.setEntity("comp", competence)
				.uniqueResult();
		
		return number;
	}
	
	@Override
	@Transactional (readOnly = true)
	public TargetCompetence getTargetCompetenceOfAchievedCompetence(long achievedCompetenceId) {
		return getTargetCompetenceOfAchievedCompetence(achievedCompetenceId, getPersistence().currentManager());
	}
	
	@Override
	@Transactional (readOnly = true)
	public TargetCompetence getTargetCompetenceOfAchievedCompetence(long achievedCompetenceId, Session session) {
		String query = 
			"SELECT tComp " +
			"FROM AchievedCompetence achievedComp " +
			"LEFT JOIN achievedComp.targetCompetence tComp " +
			"WHERE achievedComp.id = :achievedCompetenceId ";
		
		TargetCompetence result = (TargetCompetence) session.createQuery(query)
				.setLong("achievedCompetenceId", achievedCompetenceId)
				.uniqueResult();
		
		return result;
	}
	
	@Override
	@Transactional (readOnly = true)
	public boolean isCompetenceCompleted(long competenceId, User user) {
		String query = 
			"SELECT COUNT(DISTINCT comp) " +
			"FROM Portfolio portfolio " +
			"LEFT JOIN portfolio.user user " +
			"LEFT JOIN portfolio.competences aComp " +
			"LEFT JOIN aComp.competence comp " +
			"WHERE comp.id = :competenceId " +
				"AND user = :user ";
		
		Long number = (Long) persistence.currentManager().createQuery(query)
				.setLong("competenceId", competenceId)
				.setEntity("user", user)
				.uniqueResult();
		
		return number > 0;
	}
	
	@Override
	@Transactional (readOnly = true)
	public boolean isCompetenceCompleted(Competence competence, User user) {
		String query = 
			"SELECT COUNT(DISTINCT comp) " +
			"FROM Portfolio portfolio " +
			"LEFT JOIN portfolio.user user " +
			"LEFT JOIN portfolio.competences aComp " +
			"LEFT JOIN aComp.competence comp " +
			"WHERE comp = :comp " +
			"AND user = :user ";
		
		Long number = (Long) persistence.currentManager().createQuery(query)
				.setEntity("comp", competence)
				.setEntity("user", user)
				.uniqueResult();
		
		return number > 0;
	}
	
	@Override
	@Transactional (readOnly = true)
	public long getNumberOfCompletedCompetences(List<Competence> allCompetences, User user) {
		String query = 
			"SELECT COUNT(DISTINCT aComp) " +
			"FROM Portfolio portfolio " +
			"LEFT JOIN portfolio.user user " +
			"LEFT JOIN portfolio.competences aComp " +
			"LEFT JOIN aComp.competence comp " +
			"WHERE comp IN (:comps) " +
			"AND user = :user ";
		
		Long number = (Long) persistence.currentManager().createQuery(query)
				.setParameterList("comps", allCompetences)
				.setEntity("user", user)
				.uniqueResult();
		
		return number;
	}
	
	@Override
	@Transactional (readOnly = true)
	public List<AchievedCompetence> getPublicAchievedCompetences(User user) {
		String query = 
			"SELECT aComp " +
			"FROM Portfolio portfolio " +
			"LEFT JOIN portfolio.user user " +
			"LEFT JOIN portfolio.competences aComp " +
			"WHERE user = :user AND " +
				"aComp.deleted = false AND " +
				"aComp.visibility != :visibility ";
		
		@SuppressWarnings("unchecked")
		List<AchievedCompetence> result = persistence.currentManager().createQuery(query)
			.setEntity("user",user)
			.setParameter("visibility", VisibilityType.PRIVATE)
			.list();
		
		if (result != null) {
			return result;
		}
		return new ArrayList<AchievedCompetence>();
	}
	
	@Override
	@Transactional (readOnly = true)
	public List<TargetCompetence> getPublicCompletedNonarchivedTargetCompetences(User user) {
		String query = 
			"SELECT DISTINCT tComp " +
			"FROM User user " +
			"LEFT JOIN user.learningGoals tGoal " +
			"LEFT JOIN tGoal.targetCompetences tComp " +
			"WHERE user = :user " +
				"AND tGoal.deleted = false " +
				"AND tComp.completed = true " +
				"AND tComp.deleted = false " +
				"AND tComp.visibility != :visibility";
		
		@SuppressWarnings("unchecked")
		List<TargetCompetence> result = persistence.currentManager().createQuery(query)
			.setEntity("user", user)
			.setParameter("visibility", VisibilityType.PRIVATE)
			.list();
		
		if (result != null) {
			return result;
		}
		return new ArrayList<TargetCompetence>();
	}
	
	@Override
	@Transactional (readOnly = true)
	public List<TargetCompetence> getPublicOngoingTargetCompetences(User user) {
		String query = 
			"SELECT DISTINCT tComp " +
			"FROM User user " +
			"LEFT JOIN user.learningGoals tGoal " +
			"LEFT JOIN tGoal.targetCompetences tComp " +
			"WHERE user = :user " +
				"AND tGoal.deleted = false " +
				"AND tComp.completed = false " +
				"AND tComp.deleted = false " +
				"AND tComp.visibility != :visibility " + 
				"ORDER BY tComp.title ASC";
		
		@SuppressWarnings("unchecked")
		List<TargetCompetence> result = persistence.currentManager().createQuery(query)
			.setEntity("user", user)
			.setParameter("visibility", VisibilityType.PRIVATE)
			.list();
		
		if (result != null) {
			return result;
		}
		return new ArrayList<TargetCompetence>();
	}
}
