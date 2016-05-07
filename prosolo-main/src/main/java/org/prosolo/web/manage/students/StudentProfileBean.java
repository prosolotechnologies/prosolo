package org.prosolo.web.manage.students;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.prosolo.app.Settings;
import org.prosolo.common.domainmodel.activities.TargetActivity;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.competences.TargetCompetence;
import org.prosolo.common.domainmodel.evaluation.Evaluation;
import org.prosolo.common.domainmodel.user.TargetLearningGoal;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.preferences.TopicPreference;
import org.prosolo.common.domainmodel.user.socialNetworks.SocialNetworkAccount;
import org.prosolo.common.domainmodel.user.socialNetworks.SocialNetworkName;
import org.prosolo.common.domainmodel.user.socialNetworks.UserSocialNetworks;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.common.web.activitywall.data.UserData;
import org.prosolo.services.lti.exceptions.DbConnectionException;
import org.prosolo.services.nodes.CompetenceManager;
import org.prosolo.services.nodes.EvaluationManager;
import org.prosolo.services.nodes.PortfolioManager;
import org.prosolo.services.nodes.SocialNetworksManager;
import org.prosolo.services.nodes.UserManager;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.datatopagemappers.SocialNetworksDataToPageMapper;
import org.prosolo.web.portfolio.data.SocialNetworksData;
import org.prosolo.web.students.data.StudentData;
import org.prosolo.web.students.data.learning.ActivityData;
import org.prosolo.web.students.data.learning.CompetenceData;
import org.prosolo.web.students.data.learning.EvaluationSubmissionData;
import org.prosolo.web.students.data.learning.LearningGoalData;
import org.prosolo.web.util.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ManagedBean(name = "studentProfileBean")
@Component
@Scope("view")
public class StudentProfileBean implements Serializable {

	private static final long serialVersionUID = -569778470324074695L;

	private static Logger logger = Logger.getLogger(StudentProfileBean.class);

	@Inject
	private ObservationBean observationBean;
	@Inject
	private UrlIdEncoder idEncoder;
	@Inject
	private UserManager userManager;
	@Inject
	private SocialNetworksManager socialNetworksManager;
	@Inject
	private LoggedUserBean loggedUserBean;
	@Inject
	private PortfolioManager portfolioManager;
	@Inject
	private CompetenceManager compManager;
	@Inject
	private EvaluationManager evalManager;

	private String id;
	private long decodedId;

	private StudentData student;
	private SocialNetworksData socialNetworksData;
	
	private UserSocialNetworks userSocialNetworks;

	private List<LearningGoalData> lGoals;
	private LearningGoalData selectedGoal;

	public void initStudent() {
		decodedId = idEncoder.decodeId(id);

		if (decodedId > 0) {
			try {
				User user = userManager.loadResource(User.class, decodedId, true);
				student = new StudentData(user);

				initLearningGoals();

				observationBean.setStudentId(decodedId);
				observationBean.setStudentName(student.getName());
				observationBean.setTargetGoalId(selectedGoal.getId());
				observationBean.initializeObservationData();

				logger.info("User with id " + loggedUserBean.getUser().getId()
						+ " came to the studentProfile page for student with id " + decodedId);
			} catch (ResourceCouldNotBeLoadedException e) {
				logger.error(e);
				try {
					FacesContext.getCurrentInstance().getExternalContext().dispatch("/notfound.xhtml");
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			} catch (DbConnectionException dbce) {
				logger.error(dbce);
				PageUtil.fireErrorMessage(dbce.getMessage());
			} catch (Exception ex) {
				logger.error(ex);
			}
		} else {
			try {
				FacesContext.getCurrentInstance().getExternalContext().dispatch("/notfound.xhtml");
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}

	}

	public void loadSocialNetworkData() {
		try {
			if (student.getInterests() == null) {
				User user = new User();
				user.setId(decodedId);

				TopicPreference topicPreference = (TopicPreference) userManager.getUserPreferences(user,
						TopicPreference.class);
				Set<Tag> preferredKeywords = topicPreference.getPreferredKeywords();

				student.addInterests(preferredKeywords);
			}
			if (socialNetworksData == null) {
				initSocialNetworks();
			}
		} catch (Exception e) {
			logger.error(e);
			PageUtil.fireErrorMessage("Error while loading social network data");
		}
	}

	private void initLearningGoals() {
		try {
			lGoals = new ArrayList<>();
			List<TargetLearningGoal> goals = portfolioManager.getAllGoals(decodedId);
			boolean first = true;

			for (TargetLearningGoal tg : goals) {
				LearningGoalData lgd = new LearningGoalData(tg);
				lGoals.add(lgd);

				if (first) {
					selectGoal(lgd);
					first = false;
				}
			}
		} catch (DbConnectionException e) {
			logger.error(e);
			PageUtil.fireErrorMessage("Error loading learning goals.");
		}
	}

	public void selectGoal(LearningGoalData goal) {
		try {
			if (selectedGoal != null) {
				selectedGoal.setCompetences(null);
			}
			selectedGoal = goal;

			List<TargetCompetence> competences = compManager.getTargetCompetencesForTargetLearningGoal(goal.getId());
			List<CompetenceData> compData = new ArrayList<>();
			boolean first = true;

			for (TargetCompetence tg : competences) {
				CompetenceData cd = new CompetenceData(tg);
				long acceptedSubmissions = evalManager.getApprovedEvaluationCountForResource(TargetCompetence.class,
						cd.getId());
				cd.setApprovedSubmissionNumber(acceptedSubmissions);
				long rejectedSubmissions = evalManager.getRejectedEvaluationCountForResource(TargetCompetence.class,
						cd.getId());
				cd.setRejectedSubmissionNumber(rejectedSubmissions);
				boolean trophy = evalManager.hasAnyBadge(TargetCompetence.class, cd.getId());
				cd.setTrophyWon(trophy);
				compData.add(cd);

				if (first) {
					selectCompetence(cd);
					first = false;
				}
			}
			selectedGoal.setCompetences(compData);

			// set selected target goal id to observation bean
			observationBean.resetObservationData(selectedGoal.getId());
		} catch (DbConnectionException e) {
			logger.error(e);
			PageUtil.fireErrorMessage("Error loading competences.");
		}
	}

	public void loadSubmissions(CompetenceData cd) {
		try {
			if (cd.getSubmissions() == null) {
				cd.setSubmissions(new ArrayList<EvaluationSubmissionData>());
				List<Evaluation> evals = evalManager.getEvaluationsForAResource(TargetCompetence.class, cd.getId());
				for (Evaluation e : evals) {
					cd.getSubmissions().add(new EvaluationSubmissionData(e));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
			PageUtil.fireErrorMessage(e.getMessage());
		}
	}

	public void selectCompetence(CompetenceData cd) {
		try {
			if (selectedGoal.getSelectedCompetence() != null) {
				selectedGoal.getSelectedCompetence().setActivities(null);
			}

			selectedGoal.setSelectedCompetence(cd);
			List<TargetActivity> activities = compManager.getTargetActivities(cd.getId());
			List<ActivityData> actData = new ArrayList<>();

			for (TargetActivity ta : activities) {
				if (ta != null) {
					actData.add(new ActivityData(ta));
				}
			}
			cd.setActivities(actData);
		} catch (Exception e) {
			throw new DbConnectionException("Error while loading activities");
		}
	}

	public UserData getMessageReceiverData() {
		UserData ud = new UserData();
		ud.setName(student.getName());
		ud.setId(decodedId);
		return ud;
	}

	public void initSocialNetworks() {
		if (socialNetworksData == null) {
			userSocialNetworks = socialNetworksManager.getSocialNetworks(loggedUserBean.getUser());
			socialNetworksData = new SocialNetworksDataToPageMapper(userSocialNetworks)
					.mapDataToPageObject(socialNetworksData);
		}
	}

	public String getCompletedActivitiesServicePath() {
		long compId = selectedGoal.getSelectedCompetence() != null ? selectedGoal.getSelectedCompetence().getId() : 0;
		return Settings.getInstance().config.application.domain + "api/competences/" + compId + "/activities";
	}

	public ObservationBean getObservationBean() {
		return observationBean;
	}

	public void setObservationBean(ObservationBean observationBean) {
		this.observationBean = observationBean;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public StudentData getStudent() {
		return student;
	}

	public void setStudent(StudentData student) {
		this.student = student;
	}

	public long getDecodedId() {
		return decodedId;
	}

	public long getDecodedId(String id) {
		return idEncoder.decodeId(id);
	}

	public void setDecodedId(long decodedId) {
		this.decodedId = decodedId;
	}

	public SocialNetworksData getSocialNetworksData() {
		return socialNetworksData;
	}

	public void setSocialNetworksData(SocialNetworksData socialNetworksData) {
		this.socialNetworksData = socialNetworksData;
	}

	public List<LearningGoalData> getlGoals() {
		return lGoals;
	}

	public void setlGoals(List<LearningGoalData> lGoals) {
		this.lGoals = lGoals;
	}

	public LearningGoalData getSelectedGoal() {
		return selectedGoal;
	}

	public void setSelectedGoal(LearningGoalData selectedGoal) {
		this.selectedGoal = selectedGoal;
	}

}
