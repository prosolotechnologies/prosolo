$(function () {
	attachListenersForUpdatingStatus();
});

function updateStatusToDraft() {
	$('#formMain\\:compSideBar\\:selectStatus').val('DRAFT').change();
}

function attachListenersForUpdatingStatus() {
	$("#formMain\\:inputKeywords").on('itemAdded itemRemoved', function(event) {
		updateStatusToDraft();
	});
	
	$('.pasteable').on('paste', function() {
		updateStatusToDraft();
	});
}