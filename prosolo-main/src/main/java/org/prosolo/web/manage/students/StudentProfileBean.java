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
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.competences.TargetCompetence;
import org.prosolo.common.domainmodel.credential.TargetCredential1;
import org.prosolo.common.domainmodel.evaluation.Evaluation;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.preferences.TopicPreference;
import org.prosolo.common.domainmodel.user.socialNetworks.UserSocialNetworks;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.common.web.activitywall.data.UserData;
import org.prosolo.services.common.exception.DbConnectionException;
import org.prosolo.services.nodes.Activity1Manager;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.EvaluationManager;
import org.prosolo.services.nodes.SocialNetworksManager;
import org.prosolo.services.nodes.UserManager;
import org.prosolo.services.nodes.data.ActivityData;
import org.prosolo.services.nodes.data.CompetenceData1;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.datatopagemappers.SocialNetworksDataToPageMapper;
import org.prosolo.web.manage.students.data.ActivityProgressData;
import org.prosolo.web.manage.students.data.CompetenceProgressData;
import org.prosolo.web.manage.students.data.CredentialProgressData;
import org.prosolo.web.manage.students.data.EvaluationSubmissionData;
import org.prosolo.web.manage.students.data.observantions.StudentData;
import org.prosolo.web.portfolio.data.SocialNetworksData;
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
	private CredentialManager credentialManager;
	@Inject
	private Competence1Manager compManager;
	@Inject
	private Activity1Manager activityManager;
	@Inject
	private EvaluationManager evalManager;


	private String id;
	private long decodedId;

	private StudentData student;
	private SocialNetworksData socialNetworksData;
	
	private UserSocialNetworks userSocialNetworks;

	private List<CredentialProgressData> credentials;
	private CredentialProgressData selectedCredential;


	public void initStudent() {
		decodedId = idEncoder.decodeId(id);

		if (decodedId > 0) {
			try {
				User user = userManager.loadResource(User.class, decodedId, true);
				student = new StudentData(user);

				initCredentials();

				observationBean.setStudentId(decodedId);
				observationBean.setStudentName(student.getName());
				observationBean.setTargetCredentialId(selectedCredential.getId());
				observationBean.initializeObservationData();


				logger.info("User with id "+ 
						loggedUserBean.getUser().getId() + 
						" came to the studentProfile page for student with id " + decodedId);

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

	private void initCredentials() {
		try {
			credentials = new ArrayList<>();
			List<TargetCredential1> userCredentials = credentialManager.getAllCredentials(decodedId);
			boolean first = true;

			for (TargetCredential1 targetCred : userCredentials) {
				CredentialProgressData credProgressData = new CredentialProgressData(targetCred);
				credentials.add(credProgressData);

				if (first) {
					selectCredential(credProgressData);
					first = false;
				}
			}
		} catch (DbConnectionException e) {
			logger.error(e);
			PageUtil.fireErrorMessage("Error loading credentials.");
		}
	}

	public void selectCredential(CredentialProgressData credProgressData) {
		try {
			if (selectedCredential != null) {
				selectedCredential.setCompetences(null);
			}
			selectedCredential = credProgressData;

			List<CompetenceData1> competences = compManager.getTargetCompetencesData(credProgressData.getId(), false);
			boolean first = true;
			
			List<CompetenceProgressData> competenecesProgress = new ArrayList<>();

			for (CompetenceData1 comp : competences) {
				CompetenceProgressData compProgress = new CompetenceProgressData(comp);
				
				long acceptedSubmissions = evalManager.getApprovedEvaluationCountForResource(TargetCompetence.class, comp.getTargetCompId());
				compProgress.setApprovedSubmissionNumber(acceptedSubmissions);
				long rejectedSubmissions = evalManager.getRejectedEvaluationCountForResource(TargetCompetence.class, comp.getTargetCompId());
				compProgress.setRejectedSubmissionNumber(rejectedSubmissions);
				boolean trophy = evalManager.hasAnyBadge(TargetCompetence.class, comp.getTargetCompId());
				compProgress.setTrophyWon(trophy);

				if (first) {
					selectCompetence(compProgress);
					first = false;
				}
				
				competenecesProgress.add(compProgress);
			}
			selectedCredential.setCompetences(competenecesProgress);

			// set selected target credential id to observation bean
			observationBean.resetObservationData(selectedCredential.getId());
		} catch (DbConnectionException e) {
			logger.error(e);
			PageUtil.fireErrorMessage("Error loading competences.");
		}
	}

	public void loadSubmissions(CompetenceProgressData cd) {
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

	public void selectCompetence(CompetenceProgressData cd) {
		try {
			if (selectedCredential.getSelectedCompetence() != null) {
				selectedCredential.getSelectedCompetence().setActivities(null);
			}

			selectedCredential.setSelectedCompetence(cd);
			List<ActivityData> activities = activityManager.getTargetActivitiesData(cd.getId());
			
			List<ActivityProgressData> activitiesProgressData = new ArrayList<>();
			
			for (ActivityData activityData : activities) {
				activitiesProgressData.add(new ActivityProgressData(activityData));
			}
			cd.setActivities(activitiesProgressData);
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
			socialNetworksData = new SocialNetworksDataToPageMapper()
					.mapDataToPageObject(userSocialNetworks);
		}
	}

	public String getCompletedActivitiesServicePath() {
		long compId = 0;
		
		if (selectedCredential != null) {
			compId = selectedCredential.getSelectedCompetence() != null ? selectedCredential.getSelectedCompetence().getId() : 0;
		}
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

	public List<CredentialProgressData> getCredentials() {
		return credentials;
	}

	public void setCredentials(List<CredentialProgressData> credentials) {
		this.credentials = credentials;
	}

	public CredentialProgressData getSelectedCredential() {
		return selectedCredential;
	}

	public void setSelectedCredential(CredentialProgressData selectedCredential) {
		this.selectedCredential = selectedCredential;
	}

}
