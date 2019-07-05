package org.prosolo.web.courses.credential.announcements;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.credential.AnnouncementPublishMode;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.common.event.context.data.UserContextData;
import org.prosolo.services.nodes.AnnouncementManager;
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
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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

    //credential related data
    @Getter @Setter
    private String credentialId;
    @Getter
    private long decodedCredentialId;
    @Getter
    private AnnouncementData newAnnouncement;

    @Getter
    private List<AnnouncementData> announcements = new ArrayList<>();
    @Getter
    private CredentialData credentialData;

    @Getter
    private AnnouncementPublishModeDescription[] allPublishModes = AnnouncementPublishModeDescription.values();
    @Getter
    private PaginationData paginationData = new PaginationData();

    @Getter
    private ResourceAccessData access;

    @Getter
    @Setter
    private int page;

    public void init() {
        decodedCredentialId = idEncoder.decodeId(credentialId);

        if (decodedCredentialId > 0) {
            if (page > 0) {
                paginationData.setPage(page);
            }
            try {
                access = credManager.getResourceAccessData(decodedCredentialId, loggedUser.getUserId(),
                        ResourceAccessRequirements.of(AccessMode.MANAGER)
                                .addPrivilege(UserGroupPrivilege.Instruct)
                                .addPrivilege(UserGroupPrivilege.Edit));

                if (!access.isCanAccess()) {
                    PageUtil.accessDenied();
                } else {
                    this.credentialData = credManager.getFullTargetCredentialOrCredentialData(decodedCredentialId, loggedUser.getUserId());

                    resetNewAnnouncementValues();

                    announcements = announcementManager.getAllAnnouncementsForCredential(decodedCredentialId, paginationData.getPage() - 1, paginationData.getLimit());
                    paginationData.update(announcementManager.numberOfAnnouncementsForCredential(idEncoder.decodeId(credentialId)));
                }
            } catch (Exception e) {
                PageUtil.fireErrorMessage("Error loading credential data");
            }
        } else {
            PageUtil.notFound();
        }
    }

    private void resetNewAnnouncementValues() {
        this.newAnnouncement = new AnnouncementData();
        newAnnouncement.setPublishMode(AnnouncementPublishModeDescription.values()[0].getAnnouncementPublishMode());
    }

    public void publishAnnouncement() {
        try {
            UserContextData context = loggedUser.getUserContext();
            announcementManager.createAnnouncement(idEncoder.decodeId(credentialId), newAnnouncement.getTitle(),
                    newAnnouncement.getText(), loggedUser.getUserId(), newAnnouncement.getPublishMode(), context);

            PageUtil.fireSuccessfulInfoMessage("The announcement has been published");
            resetNewAnnouncementValues();
            init();
        } catch (Exception e) {
            logger.error(e);
            PageUtil.fireErrorMessage("Error publishing announcement");
        }
    }

    public boolean canEdit() {
        return access != null && access.isCanEdit();
    }

    @Override
    public void changePage(int page) {
        if (paginationData.getPage() != page) {
            paginationData.setPage(page);
            init();
        }
    }

    public void updatePublishMode() {
        String publishModeValue = PageUtil.getPostParameter("value");

        if (StringUtils.isBlank(publishModeValue)) {
            logger.error("User " + loggedUser.getUserId() + " has sent blank publish value");
        } else {
            newAnnouncement.setPublishMode(AnnouncementPublishMode.valueOf(publishModeValue));
        }
    }

}
