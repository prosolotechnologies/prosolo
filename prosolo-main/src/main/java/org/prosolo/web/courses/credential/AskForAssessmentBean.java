package org.prosolo.web.courses.credential;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.assessment.CredentialAssessment;
import org.prosolo.common.domainmodel.credential.CredentialType;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.search.UserTextSearch;
import org.prosolo.search.impl.PaginatedResult;
import org.prosolo.services.event.EventException;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.nodes.AssessmentManager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.CredentialData;
import org.prosolo.services.nodes.data.UserData;
import org.prosolo.services.nodes.data.assessments.AssessmentRequestData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.page.PageUtil;
import org.prosolo.web.util.pagination.PaginationData;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Bojan Trifkovic
 * @date 2017-10-10
 * @since 1.0.0
 */

@ManagedBean(name = "askForAssessmentBean")
@Component("askForAssessmentBean")
@Scope("view")
public class AskForAssessmentBean implements Serializable {

    private static Logger logger = Logger.getLogger(AskForAssessmentBean.class);

    @Inject
    private CredentialManager credManager;
    @Inject
    private LoggedUserBean loggedUser;
    @Inject
    private UserTextSearch userTextSearch;
    @Inject
    private UrlIdEncoder idEncoder;
    @Inject
    private AssessmentManager assessmentManager;
    @Inject
    private ThreadPoolTaskExecutor taskExecutor;
    @Inject
    private EventFactory eventFactory;

    private List<UserData> peersForAssessment;
    private String peerSearchTerm;
    private List<Long> peersToExcludeFromSearch;
    private boolean noRandomAssessor = false;
    private AssessmentRequestData assessmentRequestData = new AssessmentRequestData();
    private PaginationData paginationData = new PaginationData();
    private CredentialData credentialData;
    private String credentialId;
    private long decodedId;

    public void init(){
        decodedId = idEncoder.decodeId(credentialId);

        this.credentialData = credManager.getFullTargetCredentialOrCredentialData(
                decodedId, loggedUser.getUserId());
    }

    public void initCredentialId(String encodedId){
        logger.info(encodedId);
        this.credentialId = encodedId;
        init();
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
            if (this.assessmentRequestData.isAssessorSet()) {
                populateAssessmentRequestFields();
                this.assessmentRequestData.setMessageText(this.assessmentRequestData.getMessageText().replace("\r", ""));
                this.assessmentRequestData.setMessageText(this.assessmentRequestData.getMessageText().replace("\n", "<br/>"));
                long assessmentId = assessmentManager.requestAssessment(this.assessmentRequestData, loggedUser.getUserContext());
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
        this.assessmentRequestData.setCredentialTitle(credentialData.getTitle());
        this.assessmentRequestData.setStudentId(loggedUser.getUserId());
        this.assessmentRequestData.setCredentialId(credentialData.getId());
        this.assessmentRequestData.setTargetCredentialId(credentialData.getTargetCredId());
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

    public CredentialManager getCredManager() {
        return credManager;
    }

    public void setCredManager(CredentialManager credManager) {
        this.credManager = credManager;
    }

    public UserTextSearch getUserTextSearch() {
        return userTextSearch;
    }

    public void setUserTextSearch(UserTextSearch userTextSearch) {
        this.userTextSearch = userTextSearch;
    }

    public UrlIdEncoder getIdEncoder() {
        return idEncoder;
    }

    public void setIdEncoder(UrlIdEncoder idEncoder) {
        this.idEncoder = idEncoder;
    }

    public AssessmentManager getAssessmentManager() {
        return assessmentManager;
    }

    public void setAssessmentManager(AssessmentManager assessmentManager) {
        this.assessmentManager = assessmentManager;
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

    public AssessmentRequestData getAssessmentRequestData() {
        return assessmentRequestData;
    }

    public void setAssessmentRequestData(AssessmentRequestData assessmentRequestData) {
        this.assessmentRequestData = assessmentRequestData;
    }

    public PaginationData getPaginationData() {
        return paginationData;
    }

    public void setPaginationData(PaginationData paginationData) {
        this.paginationData = paginationData;
    }

    public CredentialData getCredentialData() {
        return credentialData;
    }

    public void setCredentialData(CredentialData credentialData) {
        this.credentialData = credentialData;
    }

    public String getCredentialId() {
        return credentialId;
    }

    public void setCredentialId(String credentialId) {
        this.credentialId = credentialId;
    }

    public long getDecodedId() {
        return decodedId;
    }

    public void setDecodedId(long decodedId) {
        this.decodedId = decodedId;
    }
}
