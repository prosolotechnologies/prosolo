define(['jquery', 'jqueryui'], function($) {
	$.extend($.datepicker, {
		_checkOffset : function(inst, offset, isFixed) {
			return offset
		}
	});
	
	return {
		init : function(chartId, onSelect) {
			var selector = "#" + chartId + " .dateField";
			var options = {
				showOn : "both",
				buttonImage : "../resources/css/prosolo-theme/images/calendar18x15.png",
				buttonImageOnly : true,
				dateFormat : "dd.mm.yy.",
				changeMonth : true,
				changeYear : true,
				showOtherMonths : true,
				selectOtherMonths : true,
				onSelect : onSelect
			}
			$(selector).datepicker(options);
			$(selector).datepicker('setDate', new Date());
		},
		align : function (from, to, period) {
			
			function addDays(current, days) {
			    var result = new Date(current.valueOf());
			    result.setDate(result.getDate() + days);
			    return result;
			}
			
			var days = 0;
			if (period == "DAY") {
				days = -2;
			} else if (period == "WEEK") {
				days = -14;
			} else if (period == "MONTH") {
				days = -60;
			}
			var date = $(to).datepicker("getDate");
			$(from).datepicker('option', 'maxDate', addDays(date, days));
		}
	};
});