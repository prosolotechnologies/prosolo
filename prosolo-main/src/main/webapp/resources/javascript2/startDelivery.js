var startDeliveryPrependId;


function initializeDatePickers() {
	$(".datePickerSelector").datetimepicker({
    	minDate : new Date()
    });
}

function copyDatesToHiddenFields() {
	var startDateId = startDeliveryPrependId + ":formStartDeliveryConfirm:startDateHidden";
	var endDateId = startDeliveryPrependId + ":formStartDeliveryConfirm:endDateHidden";
	$(document.getElementById(startDateId)).val($('#datetimepicker1').val());
	$(document.getElementById(endDateId)).val($('#datetimepicker2').val());
}