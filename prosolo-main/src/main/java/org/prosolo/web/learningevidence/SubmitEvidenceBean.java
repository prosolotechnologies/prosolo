package org.prosolo.web.learningevidence;

import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.Logger;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import org.prosolo.common.domainmodel.credential.LearningEvidenceType;
import org.prosolo.common.domainmodel.organization.settings.EvidenceRepositoryPlugin;
import org.prosolo.services.nodes.OrganizationManager;
import org.prosolo.services.nodes.data.evidence.LearningEvidenceData;
import org.prosolo.services.nodes.data.organization.EvidenceRepositoryPluginData;
import org.prosolo.services.upload.UploadManager;
import org.prosolo.web.LoggedUserBean;
import org.prosolo.web.util.ResourceBundleUtil;
import org.prosolo.web.util.page.PageUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import javax.inject.Inject;
import java.io.Serializable;

/**
 * @author stefanvuckovic
 * @date 2017-12-14
 * @since 1.2.0
 */
@ManagedBean(name = "submitEvidenceBean")
@Component("submitEvidenceBean")
@Scope("view")
public class SubmitEvidenceBean implements Serializable {

    private static final long serialVersionUID = -5386977520658560291L;

    private static Logger logger = Logger.getLogger(SubmitEvidenceBean.class);

    @Inject private UploadManager uploadManager;
    @Inject private LoggedUserBean loggedUserBean;
    @Inject private OrganizationManager organizationManager;

    private static final int MAX_FILE_NAME_LENGTH = 200;

    @Getter @Setter
    private LearningEvidenceData evidence;
    @Getter
    private EvidenceRepositoryPluginData evidenceRepositoryPluginData;


    public void init(LearningEvidenceData evidence) {
        this.evidence = evidence;

        // load evidence repository plugin data
        EvidenceRepositoryPlugin evidenceRepositoryPlugin = organizationManager.getOrganizationPlugin(EvidenceRepositoryPlugin.class, loggedUserBean.getOrganizationId());
        evidenceRepositoryPluginData = new EvidenceRepositoryPluginData(evidenceRepositoryPlugin);
    }

    /*
    ACTIONS
     */

    public void resetEvidence() {
        evidence = new LearningEvidenceData();
    }

    public void removeUploadedEvidence() {
        evidence.setUrl(null);
    }

    public void handleFileUpload(FileUploadEvent event) {
        UploadedFile uploadedFile = event.getFile();
        try {
            String fileName = uploadedFile.getFileName();
            String fullPath = uploadManager.storeFile(uploadedFile, fileName);
            evidence.setUrl(fullPath);
        } catch (Exception e) {
            logger.error("Error", e);
            PageUtil.fireErrorMessage("Error uploading the file");
        }
    }

    public void preparePostFileEvidence() {
        evidence.setType(LearningEvidenceType.FILE);
    }

    public void preparePostUrlEvidence() {
        evidence.setType(LearningEvidenceType.LINK);
    }

    public void preparePostTextEvidence() {
        evidence.setUrl(null);
        evidence.setType(LearningEvidenceType.TEXT);
    }

    /*
	VALIDATORS
	 */
    public void validateFileEvidence(FacesContext context, UIComponent component, Object value) {
        String msg = null;
        if (evidence.getUrl() == null || evidence.getUrl().isEmpty()) {
            msg = "File must be uploaded";
        } else if (evidence.getFileName().length() > MAX_FILE_NAME_LENGTH) {
            msg = ResourceBundleUtil.getJSFMessage(FacesContext.getCurrentInstance(), "javax.faces.validator.LengthValidator.MAXIMUM",MAX_FILE_NAME_LENGTH, "File name");
        }
        if (msg != null) {
            FacesMessage fm = new FacesMessage(msg);
            fm.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ValidatorException(fm);
        }
    }

}
