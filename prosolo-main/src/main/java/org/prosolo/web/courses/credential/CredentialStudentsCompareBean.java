package org.prosolo.web.courses.credential;

import org.apache.log4j.Logger;
import org.prosolo.common.domainmodel.user.UserGroupPrivilege;
import org.prosolo.services.nodes.Competence1Manager;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.UserManager;
import org.prosolo.services.nodes.data.CredentialData;
import org.prosolo.services.nodes.data.UserData;
import org.prosolo.services.nodes.data.resourceAccess.AccessMode;
import org.prosolo.services.nodes.data.resourceAccess.ResourceAccessRequirements;
import org.prosolo.services.nodes.data.resourceAccess.RestrictedAccessResult;
import org.prosolo.services.urlencoding.UrlIdEncoder;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import java.io.Serializable;


/**
 * @author Bojan Trifkovic
 * @date 2017-09-23
 * @since 1.0.0
 */

@ManagedBean(name = "credentialStudentsCompareBean")
@Component("credentialStudentsCompareBean")
@Scope("view")
public class CredentialStudentsCompareBean implements Serializable {

    private static Logger logger = Logger.getLogger(CredentialStudentsCompareBean.class);

    @Inject
    private UrlIdEncoder idEncoder;
    @Inject
    private CredentialManager credentialManager;
    @Inject
    private LoggedUserBean loggedUser;
    @Inject
    private UserManager userManager;

    private String id;
    private long decodedId;
    private String studentId;
    private long decodedStudentId;
    private CredentialData credentialData;
    private CredentialData credentialDataStudent;
    private UserData userData;

    public void init() {
        decodedId = idEncoder.decodeId(id);
        decodedStudentId = idEncoder.decodeId(studentId);
        if (decodedId > 0 && decodedStudentId > 0) {
            try {
                this.userData = userManager.getUserData(decodedStudentId);
                this.credentialData = credentialManager.getCredentialDataAndCompetenceData(decodedId, loggedUser.getUserId());
                if (userData == null) {
                    PageUtil.notFound();
                } else {
                    if (this.credentialData == null) {
                        PageUtil.accessDenied();
                    }else {
                        this.credentialDataStudent = credentialManager.getCredentialDataAndCompetenceData(decodedId, decodedStudentId);
                    }
                }
            } catch (Exception e) {
                PageUtil.fireErrorMessage(e.getMessage());
            }
        } else {
            PageUtil.notFound();
        }
    }

    public UserData getUserData() {
        return userData;
    }

    public void setUserData(UserData userData) {
        this.userData = userData;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public CredentialData getCredentialDataStudent() {
        return credentialDataStudent;
    }

    public CredentialData getCredentialData() {
        return credentialData;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getDecodedId() {
        return decodedId;
    }

    public void setDecodedId(long decodedId) {
        this.decodedId = decodedId;
    }

}
