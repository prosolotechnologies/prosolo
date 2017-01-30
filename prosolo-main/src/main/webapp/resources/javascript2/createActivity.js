$(function () {
	bindPasteEventForUpdatingStatus();
});

var containerId;

function updateStatusToDraft() {
	$('#' + containerId + '\\:formMain\\:selectStatus').val('UNPUBLISH').change();
}

function bindPasteEventForUpdatingStatus() {
	$('.pasteable').on('paste', function() {
		updateStatusToDraft();
	});
}

function attachListenerForFetchingPageTitle() {
	$(document).on('paste', document.getElementById('formModalAddUrl:inputUrl'),function(e){
    	var pastedData = e.originalEvent.clipboardData.getData('text');
    	var valTitle = document.getElementById(containerId + ':formModalAddUrl:inputUrlTitle').value;
        fetchPageTitle([{name:'link', value: pastedData}, {name:'title', value: valTitle}]);
    });
}

function onStatusChange() {
	var status = $('#' + containerId + '\\:formMain\\:selectStatus').val();
	if(status === "UNPUBLISH") {
		$('#noteDraft').show();
		$('#' + containerId + '\\:formMain\\:linkPreview').text('Preview Draft');
	} else {
		$('#noteDraft').hide();
		$('#' + containerId + '\\:formMain\\:linkPreview').text('Preview');
	}
}

function acceptGradesChanged(acceptGradeCheckboxId, scoreCalcTypeId, autogradeCheckboxId, 
		autogradeCheckboxLabelId) {
	var checked = $(document.getElementById(acceptGradeCheckboxId)).is(":checked");
	var label = $(document.getElementById(autogradeCheckboxLabelId));
	if(checked) {
		$(document.getElementById(scoreCalcTypeId)).show();
		disableCheckbox(autogradeCheckboxId, label);
	} else {
		$(document.getElementById(scoreCalcTypeId)).hide();
		$(document.getElementById(autogradeCheckboxId)).removeAttr('disabled');
		label.removeAttr('data-toggle');
  	  	label.removeAttr('title');
	}
}

function autogradeChanged(autogradeCheckboxId, acceptGradeCheckboxId, acceptGradeCheckboxLabelId) {
	var checked = $(document.getElementById(autogradeCheckboxId)).is(":checked");
	var label = $(document.getElementById(acceptGradeCheckboxLabelId));
	if(checked) {
		disableCheckbox(acceptGradeCheckboxId, label);
	} else {
		$(document.getElementById(acceptGradeCheckboxId)).removeAttr('disabled');
		label.removeAttr('data-toggle');
  	  	label.removeAttr('title');
	}
}

function disableCheckbox(checkboxId, labelEl) {
	$(document.getElementById(checkboxId)).attr('disabled', true);
	addTooltipToCheckbox(labelEl);
}

function addTooltipToCheckbox(labelEl) {
	labelEl.attr('data-toggle', 'tooltip');
  	labelEl.attr('title', "'Autograding' and 'Accept grades' options are mutually exclusive");
}

function disableAcceptGradesIfAutogradeChecked(autogradeCheckboxId, acceptGradeCheckboxId, 
		acceptGradeCheckboxLabelId) {
	var checked = $(document.getElementById(autogradeCheckboxId)).is(":checked");
	var label = $(document.getElementById(acceptGradeCheckboxLabelId));
	if(checked) {
		disableCheckbox(acceptGradeCheckboxId, label);
	}
}

function disableAutogradeIfAcceptGradesChecked(acceptGradeCheckboxId, 
		autogradeCheckboxId, autogradeCheckboxLabelId) {
	var checked = $(document.getElementById(acceptGradeCheckboxId)).is(":checked");
	var label = $(document.getElementById(autogradeCheckboxLabelId));
	if(checked) {
		disableCheckbox(autogradeCheckboxId, label);
	}
}

