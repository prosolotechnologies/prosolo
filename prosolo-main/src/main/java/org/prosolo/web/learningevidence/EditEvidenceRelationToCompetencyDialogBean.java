package org.prosolo.web.learningevidence;

import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.Logger;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import org.prosolo.bigdata.common.exceptions.DbConnectionException;
import org.prosolo.common.domainmodel.credential.LearningEvidenceType;
import org.prosolo.common.domainmodel.organization.settings.EvidenceRepositoryPlugin;
import org.prosolo.services.nodes.LearningEvidenceManager;
import org.prosolo.services.nodes.OrganizationManager;
import org.prosolo.services.nodes.data.CompetencyBasicObjectInfo;
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
import java.util.stream.Collectors;

/**
 * @author Nikola Milikic
 * @date 2019-07-06
 * @since 1.3.2
 */
@ManagedBean(name = "editEvidenceRelationToCompetencyDialogBean")
@Component("editEvidenceRelationToCompetencyDialogBean")
@Scope("view")
public class EditEvidenceRelationToCompetencyDialogBean implements Serializable {

    private static final long serialVersionUID = -5386977520658560291L;

    private static Logger logger = Logger.getLogger(EditEvidenceRelationToCompetencyDialogBean.class);

    @Inject private LearningEvidenceManager learningEvidenceManager;
    @Inject private LoggedUserBean loggedUser;

    private LearningEvidenceData evidenceToEditRelation;
    private CompetencyBasicObjectInfo evidenceToEditRelationCompetency;
    @Getter
    @Setter
    private String evidenceToEditRelationText;

    /*
        ACTIONS
     */

    public void setEvidenceToEditRelation(LearningEvidenceData evidenceToEditRelation, CompetencyBasicObjectInfo competency) {
        this.evidenceToEditRelation = evidenceToEditRelation;
        this.evidenceToEditRelationCompetency = competency;
        this.evidenceToEditRelationText = competency.getDescription();
    }

    public void saveEvidenceRelationToCompetence() {
        try {
            if (evidenceToEditRelation != null) {
                learningEvidenceManager.updateRelationToCompetency(evidenceToEditRelation.getId(), evidenceToEditRelationCompetency.getId(), loggedUser.getUserId(), evidenceToEditRelationText);

                // update data object to render on the interface. Since BasicObjectInfo does not have setters, a new object is created and swapped with the original.
                CompetencyBasicObjectInfo updatedCompBasicInfo = new CompetencyBasicObjectInfo(evidenceToEditRelationCompetency.getId(), evidenceToEditRelationCompetency.getTitle(), evidenceToEditRelationText, evidenceToEditRelationCompetency.getCredentialId());

                evidenceToEditRelation.setCompetences(evidenceToEditRelation.getCompetences().stream()
                        .map(c -> (c.getId() == updatedCompBasicInfo.getId()) ? updatedCompBasicInfo : c)
                        .collect(Collectors.toList()));

                evidenceToEditRelation = null;
                evidenceToEditRelationCompetency = null;
                PageUtil.fireSuccessfulInfoMessage("Relation to " + ResourceBundleUtil.getLabel("competence").toLowerCase() + " is updated");
            } else {
                logger.debug("Evidence whose relation should be edited is null which means that user double-clicked the Save Changes button");
            }
        } catch (DbConnectionException e) {
            logger.error("Error", e);
            PageUtil.fireErrorMessage("Error updating relation to competency");
        }
    }

}
