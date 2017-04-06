package org.prosolo.web.courses.credential;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.bigdata.common.exceptions.ResourceNotFoundException;
import org.prosolo.common.domainmodel.activities.events.EventType;
import org.prosolo.common.domainmodel.assessment.CredentialAssessment;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.search.UserTextSearch;
import org.prosolo.search.impl.TextSearchResponse1;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.nodes.Activity1Manager;
import org.prosolo.services.nodes.AssessmentManager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.ActivityData;
import org.prosolo.services.nodes.data.CompetenceData1;
import org.prosolo.services.nodes.data.CredentialData;
import org.prosolo.services.nodes.data.ResourceCreator;
import org.prosolo.services.nodes.data.UserData;
import org.prosolo.services.nodes.data.assessments.AssessmentRequestData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@ManagedBean(name = "credentialViewBean")
@Component("credentialViewBean")
@Scope("view")
public class CredentialViewBeanUser implements Serializable {

	private static final long serialVersionUID = 2225577288550403383L;

	private static Logger logger = Logger.getLogger(CredentialViewBeanUser.class);

	@Inject
	private LoggedUserBean loggedUser;
	@Inject
	private CredentialManager credentialManager;
	@Inject
	private Activity1Manager activityManager;
	@Inject
	private UrlIdEncoder idEncoder;
	@Inject
	private AssessmentManager assessmentManager;
	@Autowired
	@Qualifier("taskExecutor")
	private ThreadPoolTaskExecutor taskExecutor;
	@Autowired
	private EventFactory eventFactory;
	@Inject private UserTextSearch userTextSearch;
	

	private String id;
	private long decodedId;
	private String mode;
	private boolean justEnrolled;

	private long numberOfUsersLearningCred;

	private CredentialData credentialData;
	private AssessmentRequestData assessmentRequestData = new AssessmentRequestData();

	private boolean noRandomAssessor = false;

	// used for search in the Ask for Assessment modal
	private List<UserData> peersForAssessment;
	private String peerSearchTerm;
	private List<Long> peersToExcludeFromSearch;
	
	private int numberOfTags;

	public void init() {
		decodedId = idEncoder.decodeId(id);
		if (decodedId > 0) {
			try {
				if ("preview".equals(mode)) {
					credentialData = credentialManager.getCredentialData(decodedId, false, true, loggedUser.getUserId(),
							UserGroupPrivilege.Edit);
					ResourceCreator rc = new ResourceCreator();
					rc.setFullName(loggedUser.getFullName());
					rc.setAvatarUrl(loggedUser.getAvatar());
					credentialData.setCreator(rc);
				} else {
					credentialData = credentialManager.getFullTargetCredentialOrCredentialData(decodedId,
							loggedUser.getUserId());
					if (justEnrolled) {
						PageUtil.fireSuccessfulInfoMessage(
								"You have enrolled in the credential " + credentialData.getTitle());
					}
				}

				if (credentialData.isEnrolled()) {
					numberOfUsersLearningCred = credentialManager.getNumberOfUsersLearningCredential(decodedId);
					numberOfTags = credentialManager.getNumberOfTags(credentialData.getTargetCredId());
				}
			} catch (ResourceNotFoundException rnfe) {
				try {
					FacesContext.getCurrentInstance().getExternalContext().dispatch("/notfound.xhtml");
				} catch (IOException e) {
					logger.error(e);
				}
			} catch (Exception e) {
				logger.error(e);
				e.printStackTrace();
				PageUtil.fireErrorMessage("Error while retrieving credential data");
			}
		} else {
			try {
				FacesContext.getCurrentInstance().getExternalContext().dispatch("/notfound.xhtml");
			} catch (IOException ioe) {
				ioe.printStackTrace();
				logger.error(ioe);
			}
		}
	}

	public boolean isCurrentUserCreator() {
		return credentialData == null || credentialData.getCreator() == null ? false
				: credentialData.getCreator().getId() == loggedUser.getUserId();
	}

	public String getLabelForCredential() {
		//TODO cred-redesign-07
//		if (isPreview()) {
//			return "(Preview)";
//		} else if (isCurrentUserCreator() && !credentialData.isEnrolled() && !credentialData.isPublished()) {
//			return "(Unpublished)";
//		} else {
//			return "";
//		}
		return "";
	}

	public boolean isPreview() {
		return "preview".equals(mode);
	}

	/*
	 * ACTIONS
	 */

	public void loadCompetenceActivitiesIfNotLoaded(CompetenceData1 cd) {
		if (!cd.isActivitiesInitialized()) {
			List<ActivityData> activities = new ArrayList<>();
			if (cd.isEnrolled()) {
				activities = activityManager.getTargetActivitiesData(cd.getTargetCompId());
			} else {
				activities = activityManager.getCompetenceActivitiesData(cd.getCompetenceId());
			}
			cd.setActivities(activities);
			cd.setActivitiesInitialized(true);
		}
	}

	public void enrollInCredential() {
		try {
			LearningContextData lcd = new LearningContextData();
			lcd.setPage(FacesContext.getCurrentInstance().getViewRoot().getViewId());
			lcd.setLearningContext(PageUtil.getPostParameter("context"));
			lcd.setService(PageUtil.getPostParameter("service"));
			CredentialData cd = credentialManager.enrollInCredential(decodedId, loggedUser.getUserId(), lcd);
			credentialData = cd;
			numberOfUsersLearningCred = credentialManager.getNumberOfUsersLearningCredential(decodedId);
		} catch (DbConnectionException e) {
			logger.error(e);
			e.printStackTrace();
			PageUtil.fireErrorMessage(e.getMessage());
		}
	}

	public boolean hasMoreCompetences(int index) {
		return credentialData.getCompetences().size() != index + 1;
	}

	/*
	 * Ask for Assessment modal
	 */
	public void resetAskForAssessmentModal() {
		noRandomAssessor = false;
		assessmentRequestData = new AssessmentRequestData();
		peersForAssessment = null;
		peerSearchTerm = null;
	}

	public void chooseRandomPeerForAssessor() {
		resetAskForAssessmentModal();

		UserData randomPeer = credentialManager.chooseRandomPeer(credentialData.getId(), loggedUser.getUserId());

		if (randomPeer != null) {
			assessmentRequestData.setAssessorId(randomPeer.getId());
			assessmentRequestData.setAssessorFullName(randomPeer.getFullName());
			assessmentRequestData.setAssessorAvatarUrl(randomPeer.getAvatarUrl());
			noRandomAssessor = false;
		} else {
			noRandomAssessor = true;
			;
		}
	}

	public void searchCredentialPeers() {
		if (peerSearchTerm == null && peerSearchTerm.isEmpty()) {
			peersForAssessment = null;
		} else {
			try {
				if (peersToExcludeFromSearch == null) {
					peersToExcludeFromSearch = credentialManager
							.getAssessorIdsForUserAndCredential(credentialData.getId(), loggedUser.getUserId());
					peersToExcludeFromSearch.add(loggedUser.getUserId());
				}

				TextSearchResponse1<UserData> result = userTextSearch.searchPeersWithoutAssessmentRequest(
						peerSearchTerm, 3, decodedId, peersToExcludeFromSearch);
				peersForAssessment = result.getFoundNodes();
			} catch (Exception e) {
				logger.error(e);
			}
		}
	}

	public void setupAssessmentRequestRecepient() {
		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String id = params.get("assessmentRecipient");
		if (StringUtils.isNotBlank(id)) {
			assessmentRequestData.setAssessorId(Long.valueOf(id));
		}
	}

	public void setAssessor(UserData assessorData) {
		assessmentRequestData.setAssessorId(assessorData.getId());
		assessmentRequestData.setAssessorFullName(assessorData.getFullName());
		assessmentRequestData.setAssessorAvatarUrl(assessorData.getAvatarUrl());

		noRandomAssessor = false;
	}

	public void submitAssessment() {
		// at this point, assessor should be set either from credential data or
		// user-submitted peer id
		if (assessmentRequestData.isAssessorSet()) {
			populateAssessmentRequestFields();
			assessmentRequestData.setMessageText(assessmentRequestData.getMessageText().replace("\r", ""));
			assessmentRequestData.setMessageText(assessmentRequestData.getMessageText().replace("\n", "<br/>"));
			LearningContextData lcd = new LearningContextData();
			lcd.setPage(PageUtil.getPostParameter("page"));
			lcd.setLearningContext(PageUtil.getPostParameter("learningContext"));
			lcd.setService(PageUtil.getPostParameter("service"));
			long assessmentId = assessmentManager.requestAssessment(assessmentRequestData, lcd);
			String page = PageUtil.getPostParameter("page");
			String lContext = PageUtil.getPostParameter("learningContext");
			String service = PageUtil.getPostParameter("service");
			notifyAssessmentRequestedAsync(assessmentId, assessmentRequestData.getAssessorId(), page, lContext,
					service);

			PageUtil.fireSuccessfulInfoMessage("Assessment request sent");

			if (peersToExcludeFromSearch != null) {
				peersToExcludeFromSearch.add(assessmentRequestData.getAssessorId());
			}
		} else {
			logger.error("Student " + loggedUser.getFullName() + " tried to submit assessment request for credential : "
					+ credentialData.getId() + ", but credential has no assessor/instructor set!");
			PageUtil.fireErrorMessage("No assessor set");
		}
		resetAskForAssessmentModal();
	}

	private void notifyAssessmentRequestedAsync(final long assessmentId, long assessorId, String page, String lContext,
			String service) {
		taskExecutor.execute(() -> {
			User assessor = new User();
			assessor.setId(assessorId);
			CredentialAssessment assessment = new CredentialAssessment();
			assessment.setId(assessmentId);
			Map<String, String> parameters = new HashMap<>();
			parameters.put("credentialId", decodedId + "");
			try {
				eventFactory.generateEvent(EventType.AssessmentRequested, loggedUser.getUserId(), assessment, assessor,
						page, lContext, service, parameters);
			} catch (Exception e) {
				logger.error("Eror sending notification for assessment request", e);
			}
		});

	}

	private void populateAssessmentRequestFields() {
		assessmentRequestData.setCredentialTitle(credentialData.getTitle());
		assessmentRequestData.setStudentId(loggedUser.getUserId());
		assessmentRequestData.setCredentialId(credentialData.getId());
		assessmentRequestData.setTargetCredentialId(credentialData.getTargetCredId());
	}

	public boolean userHasAssessmentForCredential() {
		Long assessmentCount = assessmentManager.countAssessmentsForUserAndCredential(loggedUser.getUserId(),
				decodedId);
		if (assessmentCount > 0) {
			logger.debug("We found " + assessmentCount + " assessments for user " + loggedUser.getUserId()
					+ "for credential" + decodedId);
			return true;
		}
		return false;
	}

	public String getAssessmentIdForUser() {
		return idEncoder.encodeId(
				assessmentManager.getAssessmentIdForUser(loggedUser.getUserId(), credentialData.getTargetCredId()));
	}

	/*
	 * GETTERS / SETTERS
	 */

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public CredentialData getCredentialData() {
		return credentialData;
	}

	public void setCredentialData(CredentialData credentialData) {
		this.credentialData = credentialData;
	}

	public long getDecodedId() {
		return decodedId;
	}

	public void setDecodedId(long decodedId) {
		this.decodedId = decodedId;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public boolean isJustEnrolled() {
		return justEnrolled;
	}

	public void setJustEnrolled(boolean justEnrolled) {
		this.justEnrolled = justEnrolled;
	}

	public AssessmentRequestData getAssessmentRequestData() {
		return assessmentRequestData;
	}

	public void setAssessmentRequestData(AssessmentRequestData assessmentRequestData) {
		this.assessmentRequestData = assessmentRequestData;
	}

	public AssessmentManager getAssessmentManager() {
		return assessmentManager;
	}

	public void setAssessmentManager(AssessmentManager assessmentManager) {
		this.assessmentManager = assessmentManager;
	}

	public void setTaskExecutor(ThreadPoolTaskExecutor taskExecutor) {
		this.taskExecutor = taskExecutor;
	}

	public void setEventFactory(EventFactory eventFactory) {
		this.eventFactory = eventFactory;
	}

	public long getNumberOfUsersLearningCred() {
		return numberOfUsersLearningCred;
	}

	public void setNumberOfUsersLearningCred(long numberOfUsersLearningCred) {
		this.numberOfUsersLearningCred = numberOfUsersLearningCred;
	}

	public boolean isNoRandomAssessor() {
		return noRandomAssessor;
	}

	public String getPeerSearchTerm() {
		return peerSearchTerm;
	}

	public void setPeerSearchTerm(String peerSearchTerm) {
		this.peerSearchTerm = peerSearchTerm;
	}

	public List<UserData> getPeersForAssessment() {
		return peersForAssessment;
	}

	public int getNumberOfTags() {
		return numberOfTags;
	}

}
