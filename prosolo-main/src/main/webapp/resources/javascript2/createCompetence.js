$(function () {
	$("#formMain\\:inputKeywords").on('itemAdded itemRemoved', function(event) {
		updateStatusToDraft();
	});
	
	$('.pasteable').on('paste', function() {
		updateStatusToDraft();
	});
});

function updateStatusToDraft() {
	$('#formMain\\:compSideBar\\:selectStatus').val('DRAFT').change();
}