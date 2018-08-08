package org.prosolo.web.courses.credential;

import org.apache.log4j.Logger;
import org.prosolo.services.nodes.CredentialManager;
import org.prosolo.services.nodes.UserManager;
import org.prosolo.services.nodes.data.credential.CredentialData;
import org.prosolo.services.nodes.data.UserData;
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
    private String studentToCompareId;
    private long decodedStudentToCompareId;
    private CredentialData credentialData;
    private CredentialData studentToCompareCredentialData;
    private UserData studentToCompareUserData;

    public void init() {
        decodedId = idEncoder.decodeId(id);
        decodedStudentToCompareId = idEncoder.decodeId(studentToCompareId);
        if (decodedId > 0 && decodedStudentToCompareId > 0) {
            try {
                this.studentToCompareUserData = userManager.getUserData(decodedStudentToCompareId);
                if (studentToCompareUserData == null) {
                    PageUtil.notFound();
                } else {
                    this.credentialData = credentialManager
                            .getTargetCredentialDataAndTargetCompetencesData(decodedId, loggedUser.getUserId());
                    
                    if (this.credentialData == null) {
                        PageUtil.accessDenied();
                    } else {
                        this.studentToCompareCredentialData = credentialManager
                                .getTargetCredentialDataAndTargetCompetencesData(decodedId, decodedStudentToCompareId);
                    }
                }
            } catch (Exception e) {
                PageUtil.fireErrorMessage(e.getMessage());
            }
        } else {
            PageUtil.notFound();
        }
    }

    public UserData getStudentToCompareUserData() {
        return studentToCompareUserData;
    }

    public void setStudentToCompareUserData(UserData studentToCompareUserData) {
        this.studentToCompareUserData = studentToCompareUserData;
    }

    public String getStudentToCompareId() {
        return studentToCompareId;
    }

    public void setStudentToCompareId(String studentToCompareId) {
        this.studentToCompareId = studentToCompareId;
    }

    public CredentialData getStudentToCompareCredentialData() {
        return studentToCompareCredentialData;
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
