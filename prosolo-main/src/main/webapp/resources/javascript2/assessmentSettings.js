function rubricChanged(rubricElem, visibilityContainerSelector) {
    setGradingModeVisibilityBasedOnRubricType($(rubricElem), $('select.gradingModeSelector').val());
    showOrHideRubricVisibilityRadioButtons(rubricElem, visibilityContainerSelector);
}

function showOrHideRubricVisibilityRadioButtons(rubricElem, visibilityContainerSelector){
    var rubric = $(rubricElem).val() * 1;

    if (rubric > 0) {
        $(visibilityContainerSelector).show();
    } else {
        $(visibilityContainerSelector).hide();
        $(visibilityContainerSelector + " input:radio:first").click();
    }
}

function gradingModeChanged(gradingModeEl) {
    setVisibilityBasedOnGradingMode($(gradingModeEl));
}

function setVisibilityBasedOnGradingMode(gradingModeEl) {
    var gm = gradingModeEl.val();
    switch (gm) {
        case 'NONGRADED' :
            $('.maxPointsSelector, .rubricSelector').hide();
            //reset max points
            $('.maxPointsInputSelector').val('');
            //reset rubric inputs
            resetRubricInputs();
            //reset accept grades
            $('.checkAcceptGradesSelector').removeAttr('checked');
            break;
        case 'AUTOMATIC' :
            $('.rubricSelector').hide();
            $('.checkAcceptGradesPanelSelector, .maxPointsSelector').show();
            //reset rubric inputs
            resetRubricInputs();
            break;
        case 'MANUAL' :
            $('.rubricSelector').show();
            setGradingModeVisibilityBasedOnRubricType($('.rubricPickerSelector'), gm);
            $('.checkAcceptGradesPanelSelector').hide();
            //reset accept grades
            $('.checkAcceptGradesSelector').removeAttr('checked');
            break;
    }
}

function setGradingModeVisibilityBasedOnRubricType(rubricSelectEl, gradingMode) {
    //this check is needed because we do change rubric programmatically to reset it when other grading modes are selected
    //and in those cases we should not show/hide controls based on rubric type
    if (gradingMode === 'MANUAL') {
        var selectedRubricType = rubricSelectEl.find(":selected").data("rubric-type");
        if (selectedRubricType) {
            switch (selectedRubricType) {
                case 'DESCRIPTIVE' :
                    $('.maxPointsSelector').hide();
                    break;
                default:
                    $('.maxPointsSelector').show();
                    break;
            }
        }
    }
}

function resetRubricInputs() {
    $('.rubricPickerSelector').val(0).change();
}

function determineVisibilityBasedOnGradingMode() {
    setVisibilityBasedOnGradingMode($('select.gradingModeSelector'));
}


