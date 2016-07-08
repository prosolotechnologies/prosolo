package org.prosolo.web.goals.competences;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.prosolo.services.nodes.CompetenceManager;
import org.prosolo.services.nodes.PortfolioManager;
import org.prosolo.web.LoggedUserBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name="competenceStatusCache")
@Component("competenceStatusCache")
@Scope("session")
public class CompetenceStatusCache implements Serializable {
	
	private static final long serialVersionUID = -7151583343478496295L;

	protected static Logger logger = Logger.getLogger(CompetenceStatusCache.class);
	
	@Autowired private LoggedUserBean loggedUser;
	@Autowired private PortfolioManager portfolioManager;
	@Autowired private CompetenceManager competenceManager;
	
	private Map<Long, StatusType> statusCache = new HashMap<Long, StatusType>();
	
	@PostConstruct
	public void init() {
		logger.debug("Initializing");
	}
	
	public StatusType getCompetenceStatus(long competenceId) {
		if (statusCache.containsKey(competenceId)) {
			return statusCache.get(competenceId);
		}
		
		StatusType status = null;
		
		if (portfolioManager.isCompetenceCompleted(competenceId, loggedUser.getUserId())
				|| competenceManager.hasUserCompletedCompetence(competenceId, loggedUser.getUserId())) {
			status = StatusType.COMPLETED;
		} else if (competenceManager.isUserAcquiringCompetence(competenceId, loggedUser.getUserId())) {
			status = StatusType.IN_PROGRESS;
		} else {
			status = StatusType.DO_NOT_HAVE;
		}
		
		statusCache.put(competenceId, status);
		return status;
	}
	
	public long getNumberOfCompletedCompetences(List<Long> allCompetences) {
		int count = 0;
		
		for (long competenceId : allCompetences) {
			if (getCompetenceStatus(competenceId).equals(StatusType.COMPLETED)) {
				count++;
			}
		}
		
		return count;
	}
	
	public void addCompletedCompetence(long competenceId) {
		statusCache.put(competenceId, StatusType.COMPLETED);
	}
	
	public void addInProgressCompetence(long competenceId) {
		statusCache.put(competenceId, StatusType.IN_PROGRESS);
	}

	public boolean isCompleted(long competenceId) {
		return doesCompetenceHaveStatus(competenceId, StatusType.COMPLETED);
	}

	public boolean isInProgress(long competenceId) {
		return doesCompetenceHaveStatus(competenceId, StatusType.IN_PROGRESS);
	}
	
	public boolean doNotHave(long competenceId) {
		return doesCompetenceHaveStatus(competenceId, StatusType.DO_NOT_HAVE);
	}
	
	public boolean doesCompetenceHaveStatus(long competenceId, StatusType status) {
		StatusType competenceStatus = getCompetenceStatus(competenceId);
		
		if (competenceStatus != null) {
			return competenceStatus.equals(status);
		}
		return false;
	}

	enum StatusType {
		COMPLETED,
		IN_PROGRESS,
		DO_NOT_HAVE
	}

}
