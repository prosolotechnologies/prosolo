var charts = [];

function destroyCharts() {
	charts.map(function(chart) { chart.destroy(); });
	charts = [];
}

function checkedStats() {
	return $("[name='stats']:checked").map(function() { return $(this).val(); }).get();
}

function utc(date) { 
	return new Date(date.getUTCFullYear(), date.getUTCMonth(), date.getUTCDate(), date.getUTCHours(), date.getUTCMinutes(), date.getUTCSeconds()); 
}

function loadChart(dateFrom, dateTo, period, stats){
	var host = $("#dashboard").data("api");
	show("loader");
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
			$('#chart').html("");
			destroyCharts();
			show("chart");
			var chart = new tauCharts.Chart({
			    data: data.map(function(e) { e.date = utc(new Date(e.date * 86400000)); return e; }),
			    type: 'line',
			    x: 'date',
			    y: 'count',
			    color: 'type'
			});
			chart.renderTo('#chart');
			charts.push(chart);
		}
	});
}

function show(id) {
	$("#" + id).show().siblings().hide();
}

function addTrendClassForPercent(selectors, percentage) {
	if (isNegativePercentage(percentage)) {
		for (var i=0; i<selectors.length; i++) {
			$(selectors[i]).removeClass("trend-up").addClass("trend-down");
		}
	}else{
		for (var i=0; i<selectors.length; i++) {
			$(selectors[i]).removeClass("trend-down").addClass("trend-up");
		}
	}
}

function isNegativePercentage(percentage){
	return percentage.charAt(0) === '-';
}

$(function(){
	var host = $("#dashboard").data("api");
	$.ajax({
		url : "http://" + host + "/api/users/activity/statistics/sum",
		type : "GET",
		data : {event: "registered"},
		crossDomain: true,
		dataType: 'json'
	}).done(function(data) {
		$("#total-users-count").html(data.totalUsers);
		$("#total-users-count-percent").html(data.totalUsersPercent);
		addTrendClassForPercent(["#total-users-trend","#total-users-count-percent"], data.totalUsersPercent );
	});
	
	$.ajax({
		url : "http://" + host + "/api/users/activity/statistics/active",
		type : "GET",
		data : {event: "login"},
		crossDomain: true,
		dataType: 'json'
	}).done(function(data) {
		$("#active-users-count").html(data.activeUsers);
		$("#active-users-count-percent").html(data.activeUsersPercent);
		addTrendClassForPercent(["#active-users-trend","#active-users-count-percent"], data.activeUsersPercent );
	});
	
	$.ajax({
		url : "http://" + host + "/api/users/activity/statistics/session",
		type : "GET",
		crossDomain: true,
		dataType: 'json'
	}).done(function(data) {
		$("#currently-logged-in-count").html(data.loggedIn);
	});
	
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