package org.prosolo.web.courses;

import org.prosolo.common.domainmodel.credential.GradingMode;
import org.prosolo.common.domainmodel.rubric.RubricType;
import org.prosolo.services.nodes.RubricManager;
import org.prosolo.services.nodes.data.LearningResourceAssessmentSettings;
import org.prosolo.services.nodes.data.rubrics.RubricData;
import org.prosolo.web.courses.activity.util.GradingModeDescription;
import org.prosolo.web.courses.validator.NumberValidatorUtil;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UISelectOne;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * @author stefanvuckovic
 * @date 2018-01-23
 * @since 1.2.0
 */
public abstract class LearningResourceAssessmentSettingsBean {

    private GradingModeDescription[] gradingModes;
    private List<RubricData> rubrics;

    @Inject private RubricManager rubricManager;

    public abstract boolean isLimitedEdit();
    public abstract LearningResourceAssessmentSettings getAssessmentSettings();
    public abstract List<Long> getAllUnitsResourceIsConnectedTo();
    public abstract boolean isPointBasedResource(GradingMode gradingMode, long rubricId, RubricType rubricType);
    public abstract boolean isPointBasedResource();

    protected void loadAssessmentData() {
        if (isLimitedEdit()) {
            Optional<GradingModeDescription> gradingMode = Arrays.stream(GradingModeDescription.values()).filter(gm -> getAssessmentSettings().getGradingMode() == gm.getGradingMode()).findFirst();
            gradingModes = new GradingModeDescription[] {gradingMode.get()};
        } else {
            gradingModes = GradingModeDescription.values();
            List<Long> unitIds = getAllUnitsResourceIsConnectedTo();
            if (unitIds.isEmpty()) {
                rubrics = new ArrayList<>();
            } else {
                rubrics = rubricManager.getPreparedRubricsFromUnits(unitIds);
            }
        }
    }

    /*
	VALIDATORS
	 */

    public void validateMaxPoints(FacesContext context, UIComponent component, Object value) {
        UIInput input = (UIInput) component.getAttributes().get("gradingModeComp");
        UISelectOne selectRubric = (UISelectOne) component.getAttributes().get("rubricComp");
        if (input != null && selectRubric != null) {
            long rubricId = selectRubric.isLocalValueSet()
                    ? (long) selectRubric.getValue()
                    : Long.parseLong(selectRubric.getSubmittedValue().toString());
            RubricType rubricType = rubricId == 0 ? null : getRubricTypeForId(rubricId);
            if (isPointBasedResource((GradingMode) input.getValue(), rubricId, rubricType)) {
                String validationMsg = null;
                boolean valid = true;
                //we check if value is entered and whether integer is greater than zero, other validator checks if valid number is entered
                if (value == null || value.toString().trim().isEmpty()) {
                    validationMsg = "Maximum number of points must be defined";
                    valid = false;
                } else if (NumberValidatorUtil.isInteger(value.toString())) {
                    int i = Integer.parseInt(value.toString());
                    if (i <= 0) {
                        validationMsg = "Maximum number of points must be greater than zero";
                        valid = false;
                    }
                }
                if (!valid) {
                    FacesMessage msg = new FacesMessage(validationMsg);
                    msg.setSeverity(FacesMessage.SEVERITY_ERROR);
                    throw new ValidatorException(msg);
                }
            }
        }
    }

    private RubricType getRubricTypeForId(long id) {
        Optional<RubricData> rubricData = rubrics.stream().filter(r -> r.getId() == id).findFirst();
        if (rubricData.isPresent()) {
            return rubricData.get().getRubricType();
        }
        return null;
    }

    public List<RubricData> getRubrics() {
        return rubrics;
    }

    public GradingModeDescription[] getGradingModes() {
        return gradingModes;
    }
}
