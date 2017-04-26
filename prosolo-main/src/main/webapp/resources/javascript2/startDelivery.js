var startDeliveryPrependId;

function initializeDatePickers() {
	var start = $(".datePickerSelector.deliveryStartSelector");
	start.datetimepicker({
    	minDate : moment(),
    	useCurrent: false
    });
	setStartMaxDateIfNeeded();
	
	var end = $(".datePickerSelector.deliveryEndSelector");
	end.datetimepicker({
    	useCurrent: false
    });
	setEndMinDateIfNeeded();
	start.on('dp.change', function (e) {
		setEndMinDateIfNeeded();
	});
	end.on('dp.change', function (e) {
		setStartMaxDateIfNeeded();
	});
}

function setStartMaxDateIfNeeded() {
	var start = $('.datePickerSelector.deliveryStartSelector');
	if (start.length) {
		var maxDateStr = $('.deliveryEndSelector').val();
		if (maxDateStr) {
			start.data("DateTimePicker").maxDate(moment(maxDateStr, 'MM/DD/YYYY hh:mm a'));
		} else {
			start.data("DateTimePicker").maxDate(false);
		}
	}
}
	
function setEndMinDateIfNeeded() {
	var end = $('.datePickerSelector.deliveryEndSelector');
	if (end.length) {
		var minDateStr = $('.deliveryStartSelector').val();
		var now = new Date();
		if (minDateStr) {
			var nowMoment = moment(now);
			var minDateMoment = moment(minDateStr, 'MM/DD/YYYY hh:mm a');
			var minDate = minDateMoment.toDate() > nowMoment.toDate() ? minDateMoment : nowMoment;
			end.data("DateTimePicker").minDate(minDate);
		} else {
			end.data("DateTimePicker").minDate(false);
		}
	}
}




function copyDatesToHiddenFields() {
	var startDateId = startDeliveryPrependId + ":formStartDeliveryConfirm:startDateHidden";
	var endDateId = startDeliveryPrependId + ":formStartDeliveryConfirm:endDateHidden";
	$(document.getElementById(startDateId)).val($('#datetimepicker1').val());
	$(document.getElementById(endDateId)).val($('#datetimepicker2').val());
}