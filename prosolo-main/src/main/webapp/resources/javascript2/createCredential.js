$(function () {
	$("#formMain\\:inputKeywords").on('itemAdded itemRemoved', function(event) {
		updateStatusToDraft();
	});
	
	$("#formMain\\:inputHashtags").on('itemAdded itemRemoved', function(event) {
		updateStatusToDraft();
	});

	$('.pasteable').on('paste', function() {
		updateStatusToDraft();
	});
	
	//setInitialValues();
});

//var data = null;
//
//function setInitialValues() {
//	data = {"title" : getFieldValue('title'),
//			"desc" : getFieldValue('desc'),
//			"keywords" : getFieldValue('keywords'),
//			"hashtags" : getFieldValue('hashtags'),
//			"mandatory" : getFieldValue('mandatory')};
//}

function updateStatusToDraft() {
	$('#formMain\\:credSidebar\\:selectStatus').val('DRAFT').change();
}

//function getFieldValue(label) {
//	switch(label) {
//		case 'title':
//			return $('#formMain\\:inputTitle').val();
//		case 'desc':
//			return $('#formMain\\:inputDescription').val();
//		case 'keywords':
//			return $('#formMain\\:inputKeywords').val();
//		case 'hashtags':
//			return $('#formMain\\:inputHashtags').val();
//		case 'mandatory':
//			return $('#formMain\\:credSidebar\\:checkMandatory').is(':checked');
//		default:
//			return '';
//	}
//}