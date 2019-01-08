$(function () {
	//attachListenersForUpdatingStatus();
});

var containerId;

//function updateStatusToDraft() {
//	$('#' + containerId + '\\:formMain\\:compSideBar\\:selectStatus').val('UNPUBLISH').change();
//}

//function attachListenersForUpdatingStatus() {
//	$("#" + containerId + "\\:formMain\\:inputKeywords").on('itemAdded itemRemoved', function(event) {
//		updateStatusToDraft();
//	});
//	
//	$('.pasteable').on('paste', function() {
//		updateStatusToDraft();
//	});
//}

function onStatusChange() {
	var status = $('#' + containerId + '\\:formMain\\:compSideBar\\:selectStatus').val();
	if(status === "UNPUBLISH") {
		$('#noteDraft').show();
		$('#' + containerId + '\\:formMain\\:compSideBar\\:linkPreview').text('Preview Draft');
	} else {
		$('#noteDraft').hide();
		$('#' + containerId + '\\:formMain\\:compSideBar\\:linkPreview').text('Preview');
	}
}

function getStatus() {
	return $('#' + containerId + '\\:formMain\\:compSideBar\\:selectStatus').val();
}

function bindBlindAssessmentOnChange(checkboxId) {
	$('#' + checkboxId).on('change', function() {
		var panel = $('#blindAssessmentPanel');
		panel.toggle();
		if (!panel.is(":visible")) {
			$("#blindAssessmentPanel select.selectBlindAssessmentModeSelector").val($("#blindAssessmentPanel select.selectBlindAssessmentModeSelector option:first").val()).change();
		}
	});
}