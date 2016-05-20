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