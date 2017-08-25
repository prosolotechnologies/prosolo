package org.prosolo.web.courses.credential;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.bigdata.common.exceptions.ResourceNotFoundException;
import org.prosolo.common.domainmodel.assessment.CredentialAssessment;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.event.context.data.LearningContextData;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.search.UserTextSearch;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.nodes.Activity1Manager;
import org.prosolo.services.nodes.AssessmentManager;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.ActivityData;
import org.prosolo.services.nodes.data.CompetenceData1;
import org.prosolo.services.nodes.data.CredentialData;
import org.prosolo.services.nodes.data.UserData;
import org.prosolo.services.nodes.data.assessments.AssessmentRequestData;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessData;
import org.prosolo.services.nodes.data.resourceAccess.RestrictedAccessResult;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	@Inject private Competence1Manager compManager;
	

	private String id;
	private long decodedId;
	private boolean justEnrolled;

	private long numberOfUsersLearningCred;

	private CredentialData credentialData;
	private ResourceAccessData access;
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
				retrieveUserCredentialData();
				
				/*
				 * if user does not have at least access to resource in read only mode throw access denied exception.
				 */
				if (!access.isCanRead()) {
					PageUtil.accessDenied();
				} else {
					if (justEnrolled) {
						PageUtil.fireSuccessfulInfoMessage(
								"You have enrolled in the credential " + credentialData.getTitle());
					}
	
					if (credentialData.isEnrolled()) {
						numberOfUsersLearningCred = credentialManager.getNumberOfUsersLearningCredential(decodedId);
						numberOfTags = credentialManager.getNumberOfTags(credentialData.getId());
					}
				}
			} catch (ResourceNotFoundException rnfe) {
				PageUtil.notFound();
			} catch (Exception e) {
				logger.error(e);
				e.printStackTrace();
				PageUtil.fireErrorMessage("Error while retrieving credential data");
			}
		} else {
			PageUtil.notFound();
		}
	}

	private void retrieveUserCredentialData() {
		RestrictedAccessResult<CredentialData> res = credentialManager
				.getFullTargetCredentialOrCredentialData(decodedId, loggedUser.getUserId());
		unpackResult(res);
	}

	private void unpackResult(RestrictedAccessResult<CredentialData> res) {
		credentialData = res.getResource();
		access = res.getAccess();
	}

//	public boolean isCurrentUserCreator() {
//		return credentialData == null || credentialData.getCreator() == null ? false
//				: credentialData.getCreator().getId() == loggedUser.getUserId();
//	}

	/*
	 * ACTIONS
	 */
	
	public void enrollInCompetence(CompetenceData1 comp) {
		try {

			compManager.enrollInCompetence(comp.getCompetenceId(), loggedUser.getUserId(), loggedUser.getUserContext());

			PageUtil.redirect("/credentials/" + id + "/" + idEncoder.encodeId(comp.getCompetenceId()) + "?justEnrolled=true");
		} catch(Exception e) {
			logger.error(e);
			PageUtil.fireErrorMessage("Error while enrolling in a competency");
		}
	}

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
			credentialManager.enrollInCredential(decodedId, loggedUser.getUserContext(lcd));
			//reload user credential data after enroll
			retrieveUserCredentialData();
			numberOfUsersLearningCred = credentialManager.getNumberOfUsersLearningCredential(decodedId);
		} catch (DbConnectionException e) {
			logger.error(e);
			e.printStackTrace();
			PageUtil.fireErrorMessage(e.getMessage());
		} catch (EventException e) {
			logger.error(e);
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

				PaginatedResult<UserData> result = userTextSearch.searchPeersWithoutAssessmentRequest(
						loggedUser.getOrganizationId(), peerSearchTerm, 3, decodedId, peersToExcludeFromSearch);
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
		try {
			// at this point, assessor should be set either from credential data or
			// user-submitted peer id
			if (assessmentRequestData.isAssessorSet()) {
				populateAssessmentRequestFields();
				assessmentRequestData.setMessageText(assessmentRequestData.getMessageText().replace("\r", ""));
				assessmentRequestData.setMessageText(assessmentRequestData.getMessageText().replace("\n", "<br/>"));
				long assessmentId = assessmentManager.requestAssessment(assessmentRequestData, loggedUser.getUserContext());

				notifyAssessmentRequestedAsync(assessmentId, assessmentRequestData.getAssessorId());

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
		} catch (EventException e) {
			logger.error(e);
		} catch (Exception e) {
			logger.error(e);
			PageUtil.fireErrorMessage("Error while seding assessment request");
		}
	}

	private void notifyAssessmentRequestedAsync(final long assessmentId, long assessorId) {
		UserContextData context = loggedUser.getUserContext();
		taskExecutor.execute(() -> {
			User assessor = new User();
			assessor.setId(assessorId);
			CredentialAssessment assessment = new CredentialAssessment();
			assessment.setId(assessmentId);
			Map<String, String> parameters = new HashMap<>();
			parameters.put("credentialId", decodedId + "");
			try {
				eventFactory.generateEvent(EventType.AssessmentRequested, context, assessment, assessor,
						null, parameters);
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

	public ResourceAccessData getAccess() {
		return access;
	}

}
