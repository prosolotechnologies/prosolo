package org.prosolo.web.courses.credential.announcements;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.credential.Announcement;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.credential.CredentialType;
import org.prosolo.common.domainmodel.events.EventType;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.search.UserTextSearch;
import org.prosolo.services.event.EventFactory;
import org.prosolo.services.nodes.AnnouncementManager;
import org.prosolo.services.nodes.AssessmentManager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.AnnouncementData;
import org.prosolo.services.nodes.data.CredentialData;
import org.prosolo.services.nodes.data.resourceAccess.AccessMode;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessData;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessRequirements;
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
    private CredentialData credentialData;

    //new announcement related data
    private String newAnnouncementTitle;
    private String newAnnouncementText;
    private AnnouncementPublishMode newAnouncementPublishMode = AnnouncementPublishMode.ALL_STUDENTS;

    private PaginationData paginationData = new PaginationData();

    private ResourceAccessData access;

    public void init() {
        if (StringUtils.isNotBlank(credentialId)) {
            try {
                boolean userEnrolled = false;
                decodedId = idEncoder.decodeId(credentialId);

                this.credentialData = credManager
                        .getFullTargetCredentialOrCredentialData(decodedId, loggedUser.getUserId());
                userEnrolled = credManager.isUserEnrolled(decodedId, loggedUser.getUserId());

                if (credentialData == null) {
                    PageUtil.notFound();
                } else {
                    credentialTitle = credentialData.getTitle();
                    credentialMandatoryFlow = credentialData.isMandatoryFlow();
                    credentialDurationString = credentialData.getDurationString();

                    access = credManager.getResourceAccessData(decodedId, loggedUser.getUserId(),
                            ResourceAccessRequirements.of(AccessMode.MANAGER)
                                    .addPrivilege(UserGroupPrivilege.Instruct)
                                    .addPrivilege(UserGroupPrivilege.Edit));

                    if (loggedUser.hasCapability("course.announcements.view")) {
                        if (!access.isCanAccess()) {
                            PageUtil.accessDenied();
                        } else {
                            credentialAnnouncements = announcementManager
                                    .getAllAnnouncementsForCredential(decodedId, paginationData.getPage() - 1, paginationData.getLimit());
                            paginationData.update(announcementManager.numberOfAnnouncementsForCredential(
                                    idEncoder.decodeId(credentialId)));
                        }
                    } else {
                        if (!userEnrolled) {
                            PageUtil.accessDenied();
                        } else {
                            credentialAnnouncements = announcementManager
                                    .getAllAnnouncementsForCredential(decodedId, paginationData.getPage() - 1, paginationData.getLimit());
                            paginationData.update(announcementManager.numberOfAnnouncementsForCredential(
                                    idEncoder.decodeId(credentialId)));
                        }
                    }
                }
            } catch (ResourceCouldNotBeLoadedException e) {
                logger.error("Could not initialize list of announcements", e);
                PageUtil.fireErrorMessage("Error loading announcements");
            }
        } else {
            logger.error("Could not initialize list of announcements, credentialId is null");
            PageUtil.fireErrorMessage("Error loading announcements");
        }
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
        resetNewAnnouncementValues();
        init();
    }

    public void setPublishMode() {
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String publishModeValue = params.get("value");
        if (StringUtils.isBlank(publishModeValue)) {
            logger.error("User " + loggedUser.getUserId() + " has sent blank publish value");
        } else {
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

    public String getCredentialId() {
        return credentialId;
    }

    public void setCredentialId(String credentialId) {
        this.credentialId = credentialId;
    }

    public List<AnnouncementData> getCredentialAnnouncements() {
        return credentialAnnouncements;
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
            init();
        }

    }

    public PaginationData getPaginationData() {
        return paginationData;
    }

    public String getCredentialTitle() {
        return credentialTitle;
    }

    public boolean isCredentialMandatoryFlow() {
        return credentialMandatoryFlow;
    }

    public String getCredentialDurationString() {
        return credentialDurationString;
    }

    public long getDecodedId() {
        return decodedId;
    }
}
