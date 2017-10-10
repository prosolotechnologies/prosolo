package org.prosolo.web.courses.credential.announcements;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.assessment.CredentialAssessment;
import org.prosolo.common.domainmodel.credential.Announcement;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.credential.CredentialType;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.search.UserTextSearch;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.nodes.AnnouncementManager;
import org.prosolo.services.nodes.AssessmentManager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.AnnouncementData;
import org.prosolo.services.nodes.data.CredentialData;
import org.prosolo.services.nodes.data.UserData;
import org.prosolo.services.nodes.data.assessments.AssessmentRequestData;
import org.prosolo.services.nodes.data.resourceAccess.AccessMode;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessData;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessRequirements;
import org.prosolo.services.nodes.data.resourceAccess.RestrictedAccessResult;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.page.PageUtil;
import org.prosolo.web.util.pagination.Paginable;
import org.prosolo.web.util.pagination.PaginationData;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ManagedBean(name = "announcementBean")
@Component("announcementBean")
@Scope("view")
public class AnnouncementBean implements Serializable, Paginable {

	private static final long serialVersionUID = -4020351250960511686L;

	private static Logger logger = Logger.getLogger(AnnouncementBean.class);

	@Inject
	private LoggedUserBean loggedUser;
	@Inject
	private AnnouncementManager announcementManager;
	@Inject
	private UrlIdEncoder idEncoder;
	@Inject
	private CredentialManager credManager;
	@Inject
	private ThreadPoolTaskExecutor taskExecutor;
	@Inject
	private EventFactory eventFactory;
	@Inject
	private AssessmentManager assessmentManager;
	@Inject
	private UserTextSearch userTextSearch;
	
	//credential related data
	private String credentialId;
	private long decodedId;
	
	private List<AnnouncementData> credentialAnnouncements = new ArrayList<>();
	private String credentialTitle;
	private boolean credentialMandatoryFlow;
	private String credentialDurationString;
	private CredentialData basicCredentialData;
	private CredentialData credentialData;

	//new announcement related data
	private String newAnnouncementTitle;
	private String newAnnouncementText;
	private AnnouncementPublishMode newAnouncementPublishMode = AnnouncementPublishMode.ALL_STUDENTS;

	// used for search in the Ask for Assessment modal
	private List<UserData> peersForAssessment;
	private String peerSearchTerm;
	private List<Long> peersToExcludeFromSearch;
	private boolean noRandomAssessor = false;
	private AssessmentRequestData assessmentRequestData = new AssessmentRequestData();

	private PaginationData paginationData = new PaginationData();
	
	private ResourceAccessData access;
	private boolean studentAnnouncements = false;

	public void init() {
		resetNewAnnouncementValues();
		
		if (StringUtils.isNotBlank(credentialId)) {
			try {
				decodedId = idEncoder.decodeId(credentialId);
				
				this.basicCredentialData = credManager.getBasicCredentialData(
						idEncoder.decodeId(credentialId), loggedUser.getUserId(), CredentialType.Delivery);
				if (basicCredentialData == null) {
					PageUtil.notFound();
				} else {
					credentialTitle = basicCredentialData.getTitle();
					credentialMandatoryFlow = basicCredentialData.isMandatoryFlow();
					credentialDurationString = basicCredentialData.getDurationString();
					
					//user needs instruct or edit privilege to be able to access this page
					access = credManager.getResourceAccessData(decodedId, loggedUser.getUserId(),
							ResourceAccessRequirements.of(AccessMode.MANAGER)
													  .addPrivilege(UserGroupPrivilege.Instruct)
													  .addPrivilege(UserGroupPrivilege.Edit));
					if(!access.isCanAccess()) {
						PageUtil.accessDenied();
					} else {
						credentialAnnouncements = announcementManager
								.getAllAnnouncementsForCredential(decodedId, paginationData.getPage() - 1, paginationData.getLimit());
						paginationData.update(announcementManager.numberOfAnnouncementsForCredential(
								idEncoder.decodeId(credentialId)));
					}
				}
			} catch (ResourceCouldNotBeLoadedException e) {
				logger.error("Could not initialize list of announcements", e);
				PageUtil.fireErrorMessage("Could not initialize list of announcements");
			}
		} else {
			logger.error("Could not initialize list of announcements, credentialId is null");
			PageUtil.fireErrorMessage("Could not initialize list of announcements");
		}
	}

	public void initAllAssessments(){
		if (StringUtils.isNotBlank(credentialId)) {
			try {
				decodedId = idEncoder.decodeId(credentialId);
				retrieveUserCredentialData(decodedId);
				this.basicCredentialData = credManager.getBasicCredentialData(
						idEncoder.decodeId(credentialId), loggedUser.getUserId(), CredentialType.Delivery);
				if (basicCredentialData == null) {
					PageUtil.notFound();
				} else {
					credentialTitle = basicCredentialData.getTitle();
					credentialMandatoryFlow = basicCredentialData.isMandatoryFlow();
					credentialDurationString = basicCredentialData.getDurationString();
					boolean userEnrolled = credManager.isUserEnrolled(decodedId, loggedUser.getUserId());

					if(!userEnrolled) {
						try {
							FacesContext.getCurrentInstance().getExternalContext().dispatch(
									"/accessDenied.xhtml");
						} catch (IOException e) {
							logger.error(e);
						}
					} else {
						setStudentAnnouncements(true);
						credentialAnnouncements = announcementManager
								.getAllAnnouncementsForCredential(decodedId, paginationData.getPage() - 1, paginationData.getLimit());
						paginationData.update(announcementManager.numberOfAnnouncementsForCredential(
								idEncoder.decodeId(credentialId)));
					}
				}
			} catch (ResourceCouldNotBeLoadedException e) {
				logger.error("Could not initialize list of announcements", e);
				PageUtil.fireErrorMessage("Could not initialize list of announcements");
			}
		} else {
			logger.error("Could not initialize list of announcements, credentialId is null");
			PageUtil.fireErrorMessage("Could not initialize list of announcements");
		}
	}


	private void retrieveUserCredentialData(long decodedCredId) {
		/*RestrictedAccessResult<CredentialData> res = credManager
				.getFullTargetCredentialOrCredentialData(decodedCredId, loggedUser.getUserId());
		unpackResult(res);*/
		credentialData = credManager
				.getFullTargetCredentialOrCredentialData(decodedCredId, loggedUser.getUserId());
	}

	private void unpackResult(RestrictedAccessResult<CredentialData> res) {
		credentialData = res.getResource();
		access = res.getAccess();
	}

	
	private void resetNewAnnouncementValues() {
		newAnnouncementTitle = "";
		newAnnouncementText = "";
	}

	public void publishAnnouncement() {
		AnnouncementData created = announcementManager.createAnnouncement(idEncoder.decodeId(credentialId), newAnnouncementTitle, 
				newAnnouncementText, loggedUser.getUserId(), newAnouncementPublishMode);
		
		created.setCreatorAvatarUrl(loggedUser.getAvatar());
		created.setCreatorFullName(loggedUser.getFullName());

		notifyForAnnouncementAsync(idEncoder.decodeId(created.getEncodedId()),
				idEncoder.decodeId(credentialId));
		
		PageUtil.fireSuccessfulInfoMessage("The announcement has been published");
		init();
	}
	
	public void setPublishMode() {
		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String publishModeValue = params.get("value");
		if(StringUtils.isBlank(publishModeValue)){
			logger.error("User "+loggedUser.getUserId()+" has sent blank publish value");
		}
		else {
			newAnouncementPublishMode = AnnouncementPublishMode.fromString(publishModeValue);
		}
	}
	
	private void notifyForAnnouncementAsync(long announcementId, long credentialId) {
		UserContextData context = loggedUser.getUserContext();
		taskExecutor.execute(() -> {
			Announcement announcement = new Announcement();
			announcement.setId(announcementId);
			
			try {
				Credential1 cred = credManager.loadResource(Credential1.class, credentialId, true);
				Map<String, String> parameters = new HashMap<>();
				parameters.put("credentialId", credentialId + "");
				parameters.put("publishMode", newAnouncementPublishMode.getText());
				try {
					eventFactory.generateEvent(EventType.AnnouncementPublished, context,
							announcement, cred, null, parameters);
				} catch (Exception e) {
					logger.error("Eror sending notification for announcement", e);
				}
			} catch (Exception e1) {
				logger.error(e1);
			}
			
		});
	}


	public String getAssessmentIdForUser() {
		return idEncoder.encodeId(
				assessmentManager.getAssessmentIdForUser(loggedUser.getUserId(), credentialData.getTargetCredId()));
	}

	public void searchCredentialPeers() {
		if (peerSearchTerm == null && peerSearchTerm.isEmpty()) {
			peersForAssessment = null;
		} else {
			try {
				if (peersToExcludeFromSearch == null) {
					peersToExcludeFromSearch = credManager
							.getAssessorIdsForUserAndCredential(credentialData.getId(), loggedUser.getUserId());
					peersToExcludeFromSearch.add(loggedUser.getUserId());
				}

				PaginatedResult<UserData> result = userTextSearch.searchPeersWithoutAssessmentRequest(
						loggedUser.getOrganizationId(), peerSearchTerm, 3, idEncoder.decodeId(credentialId), peersToExcludeFromSearch);
				peersForAssessment = result.getFoundNodes();
			} catch (Exception e) {
				logger.error(e);
			}
		}
	}

	public void chooseRandomPeerForAssessor() {
		resetAskForAssessmentModal();

		UserData randomPeer = credManager.chooseRandomPeer(credentialData.getId(), loggedUser.getUserId());

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

	public void resetAskForAssessmentModal() {
		noRandomAssessor = false;
		assessmentRequestData = new AssessmentRequestData();
		peersForAssessment = null;
		peerSearchTerm = null;
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
				String page = PageUtil.getPostParameter("page");
				String lContext = PageUtil.getPostParameter("learningContext");
				String service = PageUtil.getPostParameter("service");
				notifyAssessmentRequestedAsync(assessmentId, assessmentRequestData.getAssessorId(), page, lContext,
						service);

				PageUtil.fireSuccessfulInfoMessage("Your assessment request is sent");

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
			PageUtil.fireErrorMessage("Error while sending assessment request");
		}
	}
	private void populateAssessmentRequestFields() {
		assessmentRequestData.setCredentialTitle(credentialData.getTitle());
		assessmentRequestData.setStudentId(loggedUser.getUserId());
		assessmentRequestData.setCredentialId(credentialData.getId());
		assessmentRequestData.setTargetCredentialId(credentialData.getTargetCredId());
	}
	private void notifyAssessmentRequestedAsync(final long assessmentId, long assessorId, String page, String lContext,
												String service) {
		taskExecutor.execute(() -> {
			User assessor = new User();
			assessor.setId(assessorId);
			CredentialAssessment assessment = new CredentialAssessment();
			assessment.setId(assessmentId);
			Map<String, String> parameters = new HashMap<>();
			parameters.put("credentialId", idEncoder.decodeId(credentialId) + "");
			try {
				eventFactory.generateEvent(EventType.AssessmentRequested, loggedUser.getUserContext(), assessment,
						assessor,null, parameters);
			} catch (Exception e) {
				logger.error("Eror sending notification for assessment request", e);
			}
		});

	}

	public List<UserData> getPeersForAssessment() {
		return peersForAssessment;
	}

	public void setPeersForAssessment(List<UserData> peersForAssessment) {
		this.peersForAssessment = peersForAssessment;
	}

	public String getPeerSearchTerm() {
		return peerSearchTerm;
	}

	public void setPeerSearchTerm(String peerSearchTerm) {
		this.peerSearchTerm = peerSearchTerm;
	}

	public List<Long> getPeersToExcludeFromSearch() {
		return peersToExcludeFromSearch;
	}

	public void setPeersToExcludeFromSearch(List<Long> peersToExcludeFromSearch) {
		this.peersToExcludeFromSearch = peersToExcludeFromSearch;
	}

	public boolean isNoRandomAssessor() {
		return noRandomAssessor;
	}

	public void setNoRandomAssessor(boolean noRandomAssessor) {
		this.noRandomAssessor = noRandomAssessor;
	}

	public void setPaginationData(PaginationData paginationData) {
		this.paginationData = paginationData;
	}

	public void setAccess(ResourceAccessData access) {
		this.access = access;
	}

	public CredentialData getCredentialData() {
		return credentialData;
	}

	public ResourceAccessData getAccess() {
		return access;
	}

	public boolean canEdit() {
		return access != null && access.isCanEdit();
	}

	public LoggedUserBean getLoggedUser() {
		return loggedUser;
	}

	public void setLoggedUser(LoggedUserBean loggedUser) {
		this.loggedUser = loggedUser;
	}

	public AnnouncementManager getAnnouncementManager() {
		return announcementManager;
	}

	public void setAnnouncementManager(AnnouncementManager announcementManager) {
		this.announcementManager = announcementManager;
	}

	public String getCredentialId() {
		return credentialId;
	}

	public void setCredentialId(String credentialId) {
		this.credentialId = credentialId;
	}

	public UrlIdEncoder getIdEncoder() {
		return idEncoder;
	}

	public void setIdEncoder(UrlIdEncoder idEncoder) {
		this.idEncoder = idEncoder;
	}

	public List<AnnouncementData> getCredentialAnnouncements() {
		return credentialAnnouncements;
	}

	public void setCredentialAnnouncements(List<AnnouncementData> credentialAnnouncements) {
		this.credentialAnnouncements = credentialAnnouncements;
	}

	public String getNewAnnouncementTitle() {
		return newAnnouncementTitle;
	}

	public void setNewAnnouncementTitle(String newAnnouncementTitle) {
		this.newAnnouncementTitle = newAnnouncementTitle;
	}

	public String getNewAnnouncementText() {
		return newAnnouncementText;
	}

	public void setNewAnnouncementText(String newAnnouncementText) {
		this.newAnnouncementText = newAnnouncementText;
	}

	@Override
	public void changePage(int page) {
		if (paginationData.getPage() != page) {
			paginationData.setPage(page);
			if(isStudentAnnouncements()){
				initAllAssessments();
			}else {
				init();
			}
		}

	}

	public boolean isStudentAnnouncements() {
		return studentAnnouncements;
	}

	public void setStudentAnnouncements(boolean studentAnnouncements) {
		this.studentAnnouncements = studentAnnouncements;
	}

	public AssessmentRequestData getAssessmentRequestData() {
		return assessmentRequestData;
	}

	public void setAssessmentRequestData(AssessmentRequestData assessmentRequestData) {
		this.assessmentRequestData = assessmentRequestData;
	}

	public PaginationData getPaginationData() {
		return paginationData;
	}

	public CredentialManager getCredManager() {
		return credManager;
	}

	public void setCredManager(CredentialManager credManager) {
		this.credManager = credManager;
	}

	public String getCredentialTitle() {
		return credentialTitle;
	}

	public void setCredentialTitle(String credentialTitle) {
		this.credentialTitle = credentialTitle;
	}

	public boolean isCredentialMandatoryFlow() {
		return credentialMandatoryFlow;
	}

	public void setCredentialMandatoryFlow(boolean credentialMandatoryFlow) {
		this.credentialMandatoryFlow = credentialMandatoryFlow;
	}

	public String getCredentialDurationString() {
		return credentialDurationString;
	}

	public void setCredentialDurationString(String credentialDurationString) {
		this.credentialDurationString = credentialDurationString;
	}

	public ThreadPoolTaskExecutor getTaskExecutor() {
		return taskExecutor;
	}

	public void setTaskExecutor(ThreadPoolTaskExecutor taskExecutor) {
		this.taskExecutor = taskExecutor;
	}

	public EventFactory getEventFactory() {
		return eventFactory;
	}

	public void setEventFactory(EventFactory eventFactory) {
		this.eventFactory = eventFactory;
	}

	public long getDecodedId() {
		return decodedId;
	}
}
