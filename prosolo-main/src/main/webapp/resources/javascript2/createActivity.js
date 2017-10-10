$(function () {
	//bindPasteEventForUpdatingStatus();
});

var containerId;

//function updateStatusToDraft() {
//	$('#' + containerId + '\\:formMain\\:selectStatus').val('UNPUBLISH').change();
//}

//function bindPasteEventForUpdatingStatus() {
//	$('.pasteable').on('paste', function() {
//		updateStatusToDraft();
//	});
//}

function attachListenerForFetchingPageTitle() {
	$(document).on('paste', document.getElementById('formModalAddUrl:inputUrl'),function(e){
    	var pastedData = e.originalEvent.clipboardData.getData('text');
    	var valTitle = document.getElementById(containerId + ':formModalAddUrl:inputUrlTitle').value;
        fetchPageTitle([{name:'link', value: pastedData}, {name:'title', value: valTitle}]);
    });
}

//function onStatusChange() {
//	var status = $('#' + containerId + '\\:formMain\\:selectStatus').val();
//	if(status === "UNPUBLISH") {
//		$('#noteDraft').show();
//		$('#' + containerId + '\\:formMain\\:linkPreview').text('Preview Draft');
//	} else {
//		$('#noteDraft').hide();
//		$('#' + containerId + '\\:formMain\\:linkPreview').text('Preview');
//	}
//}

function disableCheckbox(checkboxId, labelEl) {
	$(document.getElementById(checkboxId)).attr('disabled', true);
	addTooltipToCheckbox(labelEl);
}

function addTooltipToCheckbox(labelEl) {
	labelEl.attr('data-toggle', 'tooltip');
  	labelEl.attr('title', "'Autograding' and 'Accept grades' options are mutually exclusive");
}

function showOrHideSubmissionCheckBoxes(selectedResponseType,
		studentCanSeeOtherResponsesCheckBoxId, studentCanEditTheirResponsesCheckBoxId){
	var selected = $(selectedResponseType).val();

	if(selected == 'NONE'){
        $(studentCanSeeOtherResponsesCheckBoxId).parent().hide();
        $(studentCanEditTheirResponsesCheckBoxId).parent().hide();
        $(studentCanSeeOtherResponsesCheckBoxId).attr('checked',false);
        $(studentCanEditTheirResponsesCheckBoxId).attr('checked',false);
	}else{
        $(studentCanSeeOtherResponsesCheckBoxId).parent().show();
        $(studentCanEditTheirResponsesCheckBoxId).parent().show();
	}
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
    console.log("STEF " + gradingModeEl);
    console.log("STEF: " + gm);
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
            $('.maxPointsSelector, .rubricSelector').show();
            $('.checkAcceptGradesPanelSelector').hide();
            //reset accept grades
            $('.checkAcceptGradesSelector').removeAttr('checked');
            break;
    }
}

function resetRubricInputs() {
    $('.rubricPickerSelector').val(0).change();
}

function determineVisibilityBasedOnGradingMode() {
    setVisibilityBasedOnGradingMode($('select.gradingModeSelector'));
}


