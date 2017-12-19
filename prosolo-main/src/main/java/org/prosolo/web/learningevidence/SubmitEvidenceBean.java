package org.prosolo.web.learningevidence;

import org.apache.log4j.Logger;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import org.prosolo.common.domainmodel.credential.LearningEvidenceType;
import org.prosolo.services.nodes.data.evidence.LearningEvidenceData;
import org.prosolo.services.upload.UploadManager;
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

    private LearningEvidenceData evidence;

    public void init(LearningEvidenceData evidence) {
        this.evidence = evidence;
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

    public void preparePostExistingEvidence(LearningEvidenceData evidence) {
        this.evidence = evidence;
    }

    /*
	VALIDATORS
	 */

    public void validateFileEvidence(FacesContext context, UIComponent component, Object value) {
        if (evidence.getUrl() == null || evidence.getUrl().isEmpty()) {
            FacesMessage msg = new FacesMessage("File must be uploaded");
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ValidatorException(msg);
        }
    }

    /*
    GETTERS AND SETTERS
     */
    public LearningEvidenceData getEvidence() {
        return evidence;
    }

}
