$(function () {
	//attachListenersForUpdatingStatus();
	
	//setInitialValues();
	initializeDeliveryDatePickers();
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
var containerId;

//function updateStatusToDraft() {
//	$('#' + containerId + '\\:formMain\\:credSidebar\\:selectStatus').val('UNPUBLISH').change();
//}
//
//function attachListenersForUpdatingStatus() {
//	$("#" + containerId + "\\:formMain\\:inputKeywords").on('itemAdded itemRemoved', function(event) {
//		updateStatusToDraft();
//	});
//	
//	$("#formMain\\:inputHashtags").on('itemAdded itemRemoved', function(event) {
//		updateStatusToDraft();
//	});
//	
//	$('.pasteable').on('paste', function() {
//		updateStatusToDraft();
//	});
//}

function showOrHideMandatoryArrows() {
	if($('#' + containerId + '\\:formMain\\:credSidebar\\:checkMandatory').is(':checked')) {
	    $(".mandatoryArrow").show();
		addClassToElement("competences", "mandatoryFlow");
	} else {
	    $(".mandatoryArrow").hide(); 
		removeCssClassesFromElement("competences", "mandatoryFlow");
	}
	
}

function initializeDeliveryDatePickers() {
	$(".datePickerSelector").datetimepicker({
    	minDate : new Date(),
    	useCurrent: false
    });
}

function toggleBlindAssessmentPanel() {
	var panel = $('#blindAssessmentPanel');
    panel.toggle();
    if (!panel.is(":visible")) {
        $("#blindAssessmentPanel select.selectBlindAssessmentModeSelector").val($("#blindAssessmentPanel select.selectBlindAssessmentModeSelector option:first").val()).change();
	}
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