package org.prosolo.web.goals.competences;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.competences.Competence;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.common.web.activitywall.data.UserData;
import org.prosolo.services.annotation.DislikeManager;
import org.prosolo.services.annotation.LikeManager;
import org.prosolo.services.stats.CompetenceAnalytics;
import org.prosolo.web.goals.data.CompetenceAnalyticsData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name="competenceanalytics")
@Component("competenceanalytics")
@Scope("session")
public class CompetenceAnalyticsBean implements Serializable {

	private static final long serialVersionUID = 3293713997939622964L;
	
	private static Logger logger = Logger.getLogger(CompetenceAnalyticsBean.class);
	
	private @Autowired CompetenceAnalytics compAnalyticService;
	private @Autowired LikeManager likeManager;
	private @Autowired DislikeManager dislikeManager;

	private HashMap<Long, CompetenceAnalyticsData> compAnalyticsMap;
	
	public CompetenceAnalyticsBean() {
		compAnalyticsMap = new HashMap<Long, CompetenceAnalyticsData>();
	}
	
	@PostConstruct
	public void init() {
		logger.debug("Initializing managed bean " + this.getClass().getSimpleName());
	}
	
	public CompetenceAnalyticsData analyzeCompetence(long competenceId) {
		if (!compAnalyticsMap.containsKey(competenceId)) {
			CompetenceAnalyticsData compAnalyticsData = new CompetenceAnalyticsData();
			
			Collection<User> usersUsingCompetence = compAnalyticService.getUsersUsingCompetence(competenceId);
			
			if (!usersUsingCompetence.isEmpty()) {
				for (User user : usersUsingCompetence) {
					compAnalyticsData.addUsedBy(new UserData(user));
				}
			}
			compAnalyticsData.setNumOfUsersAchieving(compAnalyticService.getNumberOfUsersAchievingCompetence(competenceId));
			compAnalyticsData.setAverageTime(DateUtil.getTimeDuration((long) compAnalyticService.getTimeToAchieveCompetence(competenceId)));

			compAnalyticsData.setNumOfLikes(likeManager.likeCount(Competence.class, competenceId));
			compAnalyticsData.setNumOfDislikes(dislikeManager.dislikeCount(Competence.class, competenceId));

			compAnalyticsMap.put(competenceId, compAnalyticsData);
			
			return compAnalyticsData;
		}
		return compAnalyticsMap.get(competenceId);
	}
	
	public CompetenceAnalyticsData createAnalyticsData(long competenceId) {
		CompetenceAnalyticsData cad = null;
		
		if (compAnalyticsMap.containsKey(competenceId)) {
			cad = compAnalyticsMap.get(competenceId);
		} else {
			cad = analyzeCompetence(competenceId);
		}
		return cad;
	}
	
	public CompetenceAnalyticsData recalculateLikes(long competenceId, int likes) {
		CompetenceAnalyticsData cad = null;
		
		if (compAnalyticsMap.containsKey(competenceId)) {
			cad = compAnalyticsMap.get(competenceId);
			cad.setNumOfLikes(likes);
			compAnalyticsMap.put(competenceId, cad);
		}
		return cad;
	}
	
	public CompetenceAnalyticsData recalculateDislikes(long competenceId, int dislikes) {
		CompetenceAnalyticsData cad = null;
		
		if (compAnalyticsMap.containsKey(competenceId)) {
			cad = compAnalyticsMap.get(competenceId);
			cad.setNumOfDislikes(dislikes);
			compAnalyticsMap.put(competenceId, cad);
		}
		return cad;
	}

//	public int getCompetenceUsedByUsersNumber(Competence competence) {
//		if (competence != null) {
//			return createAnalyticsData(competence).getNumOfUsedBy();
//		}
//		return 0;
//	}

}
