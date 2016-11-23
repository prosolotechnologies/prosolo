$(function () {
	attachListenersForUpdatingStatus();
});

var containerId;

function updateStatusToDraft() {
	$('#' + containerId + '\\:formMain\\:compSideBar\\:selectStatus').val('DRAFT').change();
}

function attachListenersForUpdatingStatus() {
	$("#" + containerId + "\\:formMain\\:inputKeywords").on('itemAdded itemRemoved', function(event) {
		updateStatusToDraft();
	});
	
	$('.pasteable').on('paste', function() {
		updateStatusToDraft();
	});
}

function onStatusChange() {
	var status = $('#' + containerId + '\\:formMain\\:compSideBar\\:selectStatus').val();
	if(status === "DRAFT") {
		$('#noteDraft').show();
		$('#' + containerId + '\\:formMain\\:compSideBar\\:linkPreview').text('Preview Draft');
	} else {
		$('#noteDraft').hide();
		$('#' + containerId + '\\:formMain\\:compSideBar\\:linkPreview').text('Preview');
	}
}

function onVisibilityChange() {
	var vis = $('#' + containerId + '\\:formMain\\:compSideBar\\:selectVisibility').val();
	//handle publish status
	if(vis === "SCHEDULED") {
		$("[id$=datetimepicker4]").show()
	} else {
		$("[id$=datetimepicker4]").val("")
		$("[id$=datetimepicker4]").hide()
	}
}