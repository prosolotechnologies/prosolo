package org.prosolo.web.courses.credential.announcements;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.nodes.AnnouncementManager;
import org.prosolo.services.assessment.AssessmentManager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.AnnouncementData;
import org.prosolo.services.nodes.data.credential.CredentialData;
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
import java.util.List;
import java.util.Map;

@ManagedBean(name = "announcementBeanManager")
@Component("announcementBeanManager")
@Scope("view")
public class AnnouncementBeanManager implements Serializable, Paginable {

    private static final long serialVersionUID = -4020351250960511686L;

    private static Logger logger = Logger.getLogger(AnnouncementBeanManager.class);

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
    private AssessmentManager assessmentManager;

    //credential related data
    private String credentialId;
    private long decodedCredentialId;

    private List<AnnouncementData> announcements = new ArrayList<>();
    private CredentialData credentialData;

    //new announcement related data
    private String newAnnouncementTitle;
    private String newAnnouncementText;
    private AnnouncementPublishMode newAnouncementPublishMode = AnnouncementPublishMode.ACTIVE_STUDENTS;

    private PaginationData paginationData = new PaginationData();

    private ResourceAccessData access;

    public void init() {
        decodedCredentialId = idEncoder.decodeId(credentialId);

        if (decodedCredentialId > 0) {
            try {
                access = credManager.getResourceAccessData(decodedCredentialId, loggedUser.getUserId(),
                        ResourceAccessRequirements.of(AccessMode.MANAGER)
                                .addPrivilege(UserGroupPrivilege.Instruct)
                                .addPrivilege(UserGroupPrivilege.Edit));

                if (!access.isCanAccess()) {
                    PageUtil.accessDenied();
                } else {
                    this.credentialData = credManager.getFullTargetCredentialOrCredentialData(decodedCredentialId, loggedUser.getUserId());
                    initializeCredentialData(this.credentialData);
                }
            } catch (Exception e) {
                PageUtil.fireErrorMessage("Error loading credential data");
            }
        } else {
            PageUtil.notFound();
        }
    }

    private void initializeCredentialData(CredentialData credentialData) throws ResourceCouldNotBeLoadedException {
        announcements = announcementManager
                .getAllAnnouncementsForCredential(decodedCredentialId, paginationData.getPage() - 1, paginationData.getLimit());
        paginationData.update(announcementManager.numberOfAnnouncementsForCredential(
                idEncoder.decodeId(credentialId)));
    }

    private void resetNewAnnouncementValues() {
        newAnnouncementTitle = "";
        newAnnouncementText = "";
    }

    public void publishAnnouncement() {
        try {
            UserContextData context = loggedUser.getUserContext();
            announcementManager.createAnnouncement(idEncoder.decodeId(credentialId), newAnnouncementTitle,
                    newAnnouncementText, loggedUser.getUserId(), newAnouncementPublishMode, context);

            PageUtil.fireSuccessfulInfoMessage("The announcement has been published");
            resetNewAnnouncementValues();
            init();
        } catch (Exception e) {
            logger.error(e);
            PageUtil.fireErrorMessage("Error publishing announcement");
        }
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

    public List<AnnouncementData> getAnnouncements() {
        return announcements;
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

    public long getDecodedCredentialId() {
        return decodedCredentialId;
    }
}
