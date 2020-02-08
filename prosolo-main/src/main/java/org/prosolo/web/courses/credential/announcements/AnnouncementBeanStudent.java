package org.prosolo.web.courses.credential.announcements;

import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.Logger;
import org.prosolo.common.exceptions.ResourceCouldNotBeLoadedException;
import org.prosolo.services.nodes.AnnouncementManager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.data.AnnouncementData;
import org.prosolo.services.nodes.data.credential.CredentialData;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.page.PageUtil;
import org.prosolo.web.util.pagination.Paginable;
import org.prosolo.web.util.pagination.PaginationData;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ManagedBean(name = "announcementBeanStudent")
@Component("announcementBeanStudent")
@Scope("view")
public class AnnouncementBeanStudent implements Serializable, Paginable {

    private static final long serialVersionUID = 6993318770664557315L;
    private static Logger logger = Logger.getLogger(AnnouncementBeanStudent.class);

    @Inject
    private AnnouncementManager announcementManager;
    @Inject
    private UrlIdEncoder idEncoder;
    @Inject
    private LoggedUserBean loggedUser;
    @Inject
    private CredentialManager credManager;

    private String credentialId;
    private long decodedCredentialId;
    @Getter
    @Setter
    private String announcementId;
    private AnnouncementData announcementData;
    private CredentialData credentialData;


    private List<AnnouncementData> announcements = new ArrayList<>();

    private PaginationData paginationData = new PaginationData();

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
                boolean userEnrolled = credManager.isUserEnrolled(decodedCredentialId, loggedUser.getUserId());

                if (!userEnrolled) {
                    PageUtil.accessDenied();
                } else {
                    this.credentialData = credManager
                            .getFullTargetCredentialOrCredentialData(decodedCredentialId, loggedUser.getUserId());
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

    /**
     * Invoked when user navigates to credential/{id}/announcements/{announcementsId} page
     */
    public void initAnnouncement() {
        decodedCredentialId = idEncoder.decodeId(credentialId);

        if (decodedCredentialId > 0) {
            try {

                boolean userEnrolled = credManager.isUserEnrolled(decodedCredentialId, loggedUser.getUserId());

                if (!userEnrolled) {
                    PageUtil.accessDenied();
                } else {
                    // mark announcement as read
                    announcementManager.readAnnouncement(idEncoder.decodeId(announcementId), loggedUser.getUserId());
                    announcementData = announcementManager.getAnnouncement(idEncoder.decodeId(announcementId));

                    credentialData = credManager.getFullTargetCredentialOrCredentialData(decodedCredentialId, loggedUser.getUserId());
                }
            } catch (Exception e) {
                PageUtil.fireErrorMessage("Error loading announcement");
            }
        } else {
            PageUtil.notFound();
        }
    }

    /**
     * This method is called when rendering announcement info balloon on credentials page
     * In order to create links etc, some fields are initialized
     *
     * @param credentialId
     * @return
     */
    public boolean userDidNotReadLastAnnouncement(String credentialId) {
        try {
            Long lastAnnouncementSeenId = announcementManager
                    .getLastAnnouncementIdIfNotSeen(idEncoder.decodeId(credentialId), loggedUser.getUserId());
            //we could not find SeenAnnouncement for last announcement for this credential - user did not see it, or there are no announcements
            if (lastAnnouncementSeenId == null) {
                announcementData = announcementManager.getLastAnnouncementForCredential(idEncoder.decodeId(credentialId));
                if (announcementData != null) {
                    //initialize data so we can use it for components
                    this.credentialId = credentialId;
                    return true;
                } else {
                    //there are no announcements for this credential
                    return false;
                }

            }
        } catch (Exception e) {
            logger.error("Error fetching las announcement data", e);
        }
        return false;
    }

    /**
     * Created so we can handle 'x' button closing (button with a span does not get rendered well, so we use JS)
     */
    public void markAnnouncementReadFromAjax() {
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String id = params.get("announcementId");
        try {
            announcementManager.readAnnouncement(idEncoder.decodeId(id), loggedUser.getUserId());
            announcementData = announcementManager.getAnnouncement(idEncoder.decodeId(id));
        } catch (Exception e) {
            logger.error("Could not mark announcement as read", e);
        }
    }

    @Override
    public void changePage(int page) {
        if (paginationData.getPage() != page) {
            paginationData.setPage(page);
            init();
        }
    }

    public String getCredentialId() {
        return credentialId;
    }

    public void setCredentialId(String credentialId) {
        this.credentialId = credentialId;
    }

    public AnnouncementData getAnnouncementData() {
        return announcementData;
    }

    public CredentialData getCredentialData() {
        return credentialData;
    }

    public List<AnnouncementData> getAnnouncements() {
        return announcements;
    }

    public PaginationData getPaginationData() {
        return paginationData;
    }

    public void setDecodedCredentialId(long decodedCredentialId) {
        this.decodedCredentialId = decodedCredentialId;
    }

    public long getDecodedCredentialId() {
        return decodedCredentialId;
    }

}
