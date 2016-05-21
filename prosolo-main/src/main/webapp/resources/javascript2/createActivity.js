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