$(function () {
	bindPasteEventForUpdatingStatus();
});

var containerId;

function updateStatusToDraft() {
	$('#' + containerId + '\\:formMain\\:selectStatus').val('DRAFT').change();
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
	if(status === "DRAFT") {
		$('#noteDraft').show();
		$('#' + containerId + '\\:formMain\\:linkPreview').text('Preview Draft');
	} else {
		$('#noteDraft').hide();
		$('#' + containerId + '\\:formMain\\:linkPreview').text('Preview');
	}
}

function acceptGradesChanged(acceptGradeCheckboxId, scoreCalcTypeId) {
	var checked = $(document.getElementById(acceptGradeCheckboxId)).is(":checked");
	if(checked) {
		$(document.getElementById(scoreCalcTypeId)).show();
	} else {
		$(document.getElementById(scoreCalcTypeId)).hide();
	}
}