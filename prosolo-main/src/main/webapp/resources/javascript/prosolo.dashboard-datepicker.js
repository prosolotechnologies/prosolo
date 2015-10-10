// Requires jQuery, jQuery datepicker

$.extend($.datepicker, {
	_checkOffset : function(inst, offset, isFixed) {
		return offset
	}
});

var datepicker = {
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
	}
}