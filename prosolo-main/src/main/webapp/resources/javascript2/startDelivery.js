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
	setEndMinDate();
	start.on('dp.change', function (e) {
		setEndMinDate();
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
	
function setEndMinDate() {
	var end = $('.datePickerSelector.deliveryEndSelector');
	if (end.length) {
		var minDateStr = $('.deliveryStartSelector').val();
        var nowMoment = moment(new Date());
		if (minDateStr) {
			var minDateMoment = moment(minDateStr, 'MM/DD/YYYY hh:mm a');
			var minDate = minDateMoment.toDate() > nowMoment.toDate() ? minDateMoment : nowMoment;
			end.data("DateTimePicker").minDate(minDate);
		} else {
			end.data("DateTimePicker").minDate(nowMoment);
		}
	}
}

function disableInputAfterSpecifiedTime(inputClass, time) {
    if(time > 0) {
        setTimeout(function() {
            $("." + inputClass).prop('disabled', true);
        }, time);
    }
}