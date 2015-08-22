function checkedStats() {
	return $("[name='stats']:checked").map(function() { return $(this).val(); }).get();
}

function utc(date) { 
	return new Date(date.getUTCFullYear(), date.getUTCMonth(), date.getUTCDate(), date.getUTCHours(), date.getUTCMinutes(), date.getUTCSeconds()); 
}

function loadChart(dateFrom, dateTo, period, stats){
	var host = $("#dashboard").data("api");
	show("loader");
	$('#chart').html("");
	$.ajax({
		url : "http://" + host + "/api/users/activity/statistics",
		type : "GET",
		data : {dateFrom:dateFrom + " UTC", dateTo:dateTo + " UTC", period:period, stats:stats},
		crossDomain: true,
		dataType: 'json'
	}).done(function(data) {
		if (data.length==0) {
			show("chartMessages");
		} else {
			show("chart");
			new tauCharts.Chart({
			    data: data.map(function(e) { e.date = utc(new Date(e.date * 86400000)); return e; }),
			    type: 'line',
			    x: 'date',
			    y: 'count',
			    color: 'type'
			}).renderTo('#chart');
		}
	});
}

function show(id) {
	$("#" + id).show().siblings().hide();
}

$(function(){
	$.extend($.datepicker,{_checkOffset:function(inst,offset,isFixed){return offset}});
	$( ".dateField" ).datepicker({
		showOn: "both",
		buttonImage: "../resources/css/prosolo-theme/images/calendar18x15.png",
		buttonImageOnly: true,
		dateFormat: "dd.mm.yy.",
		changeMonth: true,
		changeYear: true,
		showOtherMonths: true,
		selectOtherMonths: true,
		onSelect: function(dateText, inst) {
			if ($("[name='stats']:checked").size() == 0) {
				return;
			}
			loadChart($("#dateFrom").val(), $("#dateTo").val(), $("[name='periods']:checked").val(), checkedStats());
	    }
	});
	$( ".dateField" ).datepicker('setDate', new Date());
	
	$("[name='stats']").change(function() {
		if ($("[name='stats']:checked").size() == 0) {
			return;
		}
		loadChart($("#dateFrom").val(), $("#dateTo").val(), $("[name='periods']:checked").val(), checkedStats());
	});
	
	$("[name='periods']").change(function() {
		if ($("[name='stats']:checked").size() == 0) {
			return;
		}
		if ($(this).is(":checked")) {
			loadChart($("#dateFrom").val(), $("#dateTo").val(), $("[name='periods']:checked").val(), checkedStats());
		}
	});
	
	if ($("[name='stats']:checked").size() == 0) {
		return;
	}
	loadChart($("#dateFrom").val(), $("#dateTo").val(), $("[name='periods']:checked").val(), checkedStats());
});