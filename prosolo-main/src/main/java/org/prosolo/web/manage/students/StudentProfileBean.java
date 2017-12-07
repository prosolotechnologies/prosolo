package org.prosolo.web.manage.students;

import org.apache.log4j.Logger;
import org.prosolo.app.Settings;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.socialNetworks.SocialNetworkName;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.common.web.activitywall.data.UserData;
import org.prosolo.config.AnalyticalServerConfig;
import org.prosolo.services.nodes.*;
import org.prosolo.services.nodes.data.ActivityData;
import org.prosolo.services.nodes.data.CompetenceData1;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.achievements.data.TargetCredentialData;
import org.prosolo.web.manage.students.data.ActivityProgressData;
import org.prosolo.web.manage.students.data.CompetenceProgressData;
import org.prosolo.web.manage.students.data.CredentialProgressData;
import org.prosolo.web.manage.students.data.observantions.StudentData;
import org.prosolo.web.profile.data.UserSocialNetworksData;
import org.prosolo.web.util.ResourceBundleUtil;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

	private String id;
	private long decodedId;

	private StudentData student;
	private UserSocialNetworksData userSocialNetworksData;

	private List<CredentialProgressData> credentials;
	private CredentialProgressData selectedCredential;
	
	private Map<String, String> nameMap = new HashMap<>();


	public void initStudent() {
		decodedId = idEncoder.decodeId(id);

		if (decodedId > 0) {
			try {
				User user = userManager.loadResource(User.class, decodedId, true);
				student = new StudentData(user);
				loadSocialNetworkData();
				initializeSocialNetworkNameMap();
				initCredentials();

				observationBean.setStudentId(decodedId);
				observationBean.setStudentName(student.getName());
				//observationBean.setTargetCredentialId(selectedCredential.getId());
				observationBean.initializeObservationData();

				logger.info("User with id "+ 
						loggedUserBean.getUserId() + 
						" came to the studentProfile page for student with id " + decodedId);

			} catch (ResourceCouldNotBeLoadedException e) {
				logger.error(e);
				PageUtil.notFound();
			} catch (DbConnectionException dbce) {
				logger.error(dbce);
				PageUtil.fireErrorMessage(dbce.getMessage());
			} catch (Exception ex) {
				logger.error(ex);
			}
		} else {
			PageUtil.notFound();
		}

	}

	public void loadSocialNetworkData() {
		try {
//			if (student.getInterests() == null) {
//				User user = new User();
//				user.setId(decodedId);
//
//				TopicPreference topicPreference = (TopicPreference) userManager.getUserPreferences(user,
//						TopicPreference.class);
//				Set<Tag> preferredKeywords = topicPreference.getPreferredKeywords();
//
//				student.addInterests(preferredKeywords);
//			}
			if (userSocialNetworksData == null) {
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
			List<TargetCredentialData> userCredentials = credentialManager.getAllCredentials(decodedId, false);
			boolean first = true;

			for (TargetCredentialData targetCred : userCredentials) {
				CredentialProgressData credProgressData = new CredentialProgressData(targetCred);
				credentials.add(credProgressData);

				if (first) {
					selectCredential(credProgressData);
					first = false;
				}
			}
		} catch (DbConnectionException e) {
			logger.error(e);
			PageUtil.fireErrorMessage("Error loading " + ResourceBundleUtil.getMessage("label.credential").toLowerCase());
		}
	}

	public void selectCredential(CredentialProgressData credProgressData) {
		System.out.println("SELECT CREDENTIAL:"+credProgressData.getName()+" id:"+credProgressData.getCredentialId());
		try {
			if (selectedCredential != null) {
				selectedCredential.setCompetences(null);
			}
			selectedCredential = credProgressData;

			List<CompetenceData1> competences = compManager
					.getCompetencesForCredential(
							credProgressData.getCredentialId(),
							decodedId,
							false,
							false,
							false);

			boolean first = true;
			
			List<CompetenceProgressData> competenecesProgress = new ArrayList<>();

			for (CompetenceData1 comp : competences) {
				CompetenceProgressData compProgress = new CompetenceProgressData(comp);
				
//				long acceptedSubmissions = evalManager.getApprovedEvaluationCountForResource(TargetCompetence.class, comp.getTargetCompId());
//				compProgress.setApprovedSubmissionNumber(acceptedSubmissions);
//				long rejectedSubmissions = evalManager.getRejectedEvaluationCountForResource(TargetCompetence.class, comp.getTargetCompId());
//				compProgress.setRejectedSubmissionNumber(rejectedSubmissions);
//				boolean trophy = evalManager.hasAnyBadge(TargetCompetence.class, comp.getTargetCompId());
//				compProgress.setTrophyWon(trophy);

				if (first) {
					selectCompetence(compProgress);
					first = false;
				}
				
				competenecesProgress.add(compProgress);
			}
			selectedCredential.setCompetences(competenecesProgress);

			// set selected target credential id to observation bean
			//observationBean.resetObservationData(selectedCredential.getId());
		} catch (DbConnectionException e) {
			logger.error(e);
			PageUtil.fireErrorMessage("Error loading competences.");
		}
	}

//	public void loadSubmissions(CompetenceProgressData cd) {
//		try {
//			if (cd.getSubmissions() == null) {
//				cd.setSubmissions(new ArrayList<EvaluationSubmissionData>());
//				List<Evaluation> evals = evalManager.getEvaluationsForAResource(TargetCompetence.class, cd.getId());
//				for (Evaluation e : evals) {
//					cd.getSubmissions().add(new EvaluationSubmissionData(e));
//				}
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			logger.error(e);
//			PageUtil.fireErrorMessage(e.getMessage());
//		}
//	}

	public void selectCompetence(CompetenceProgressData cd) {
		try {
			if (selectedCredential.getSelectedCompetence() != null) {
				selectedCredential.getSelectedCompetence().setActivities(null);
			}

			selectedCredential.setSelectedCompetence(cd);

			List<ActivityData> activities = null;
			if (cd.getId() > 0) {
				activities = activityManager.getTargetActivitiesData(cd.getId());
			} else {
				activities = activityManager.getCompetenceActivitiesData(cd.getCompetenceId());
			}
			
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
		if (userSocialNetworksData == null) {
			try {
				userSocialNetworksData = socialNetworksManager.getUserSocialNetworkData(student.getId());
			} catch (ResourceCouldNotBeLoadedException e) {
				logger.error(e);
			}
		}
	}

	public String getCompletedActivitiesServicePath() {
		long compId = 0;
		
		if (selectedCredential != null) {
			compId = selectedCredential.getSelectedCompetence() != null ? 
					selectedCredential.getSelectedCompetence().getId() : 0;
		}
		return getApiHost() + "/competences/" + compId + "/activities";
	}
	
	private String getApiHost() {
		AnalyticalServerConfig config = Settings.getInstance().config.analyticalServerConfig;
		return config.apiHost + config.apiServicesPath;
	}
	
	private void initializeSocialNetworkNameMap() {
		nameMap.put(SocialNetworkName.BLOG.toString(), "website");
		nameMap.put(SocialNetworkName.FACEBOOK.toString(), "facebook");
		nameMap.put(SocialNetworkName.GPLUS.toString(), "gplus");
		nameMap.put(SocialNetworkName.LINKEDIN.toString(), "linkedIn");
		nameMap.put(SocialNetworkName.TWITTER.toString(), "twitter");
	}
	
	public String getAlternativeName(SocialNetworkName name) {
		return nameMap.get(name.toString());
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

		System.out.println("DECODED ID FROM:"+id+" is:"+idEncoder.decodeId(id));
		return idEncoder.decodeId(id);
	}

	public void setDecodedId(long decodedId) {
		this.decodedId = decodedId;
	}

	public UserSocialNetworksData getUserSocialNetworksData() {
		return userSocialNetworksData;
	}

	public void setUserSocialNetworksData(UserSocialNetworksData userSocialNetworksData) {
		this.userSocialNetworksData = userSocialNetworksData;
	}

	public List<CredentialProgressData> getCredentials() {
		System.out.println("GET CREDENTIALS:"+credentials.size());
		return credentials;
	}

	public void setCredentials(List<CredentialProgressData> credentials) {
		this.credentials = credentials;
	}

	public CredentialProgressData getSelectedCredential() {
		return selectedCredential;
	}

	public long getSelectedCredentialId(){
		System.out.println("SELECTED CREDENTIAL IS:"+selectedCredential.getCredentialId()+" name:"+selectedCredential.getName());
		return selectedCredential.getCredentialId();
	}

	public void setSelectedCredential(CredentialProgressData selectedCredential) {
		this.selectedCredential = selectedCredential;
	}

}
