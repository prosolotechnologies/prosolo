function utc(date) { 
	return new Date(date.getUTCFullYear(), date.getUTCMonth(), date.getUTCDate(), date.getUTCHours(), date.getUTCMinutes(), date.getUTCSeconds()); 
}

function host() {
	return $("#dashboard").data("api");
}

function chart(id, container, service, data, datamap) {
	var charts = [];

	function destroyCharts() {
		charts.map(function(chart) {
			chart.destroy();
		});
		charts = [];
	}
	
	function show(element) {
		$("#" + id + " " + element).show().siblings().hide();
	}
	
	function load() {
		show(".loader");
		$.ajax({
			url : "http://" + host() + service,
			type : "GET",
			data : data(),
			crossDomain: true,
			dataType: 'json'
		}).done(function(data) {
			if (data.length==0) {
				show(".chartMessages");
			} else {
				$("#" + container).html("");
				destroyCharts();
				show(".chart");
				var chart = new tauCharts.Chart({
				    data: data.map(datamap),
				    type: 'line',
				    x: 'date',
				    y: 'count',
				    color: 'type'
				});
				chart.renderTo("#" + container);
				charts.push(chart);
			}
		});
	}
	
	return {
		load : load
	};
}

function checkedStats() {
	return $("#activityGraph [name='stats']:checked").map(function() { return $(this).val(); }).get();
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

$.extend($.datepicker,{_checkOffset:function(inst,offset,isFixed){return offset}});

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
	
	function datepicker(chartId, onSelect) {
		$( "#" + chartId + " .dateField" ).datepicker({
			showOn: "both",
			buttonImage: "../resources/css/prosolo-theme/images/calendar18x15.png",
			buttonImageOnly: true,
			dateFormat: "dd.mm.yy.",
			changeMonth: true,
			changeYear: true,
			showOtherMonths: true,
			selectOtherMonths: true,
			onSelect: onSelect
		});
		$( "#" + chartId + " .dateField" ).datepicker('setDate', new Date());
	}
	
	var activityGraphChart = chart("activityGraph", "activityGraphChart", "/api/users/activity/statistics", function() {
		var dateFrom = $("#activityGraph .dateFrom").val();
		var dateTo = $("#activityGraph .dateTo").val();
		var period = $("#activityGraph [name='periods']:checked").val()
		var stats = checkedStats();
		return {dateFrom:dateFrom + " UTC", dateTo:dateTo + " UTC", period:period, stats:stats};
	}, function(e) { 
		e.date = utc(new Date(e.date * 86400000)); return e; 
	});
	
	datepicker("activityGraph", function(dateText, inst) {
		if ($("#activityGraph [name='stats']:checked").size() == 0) {
			return;
		}
		activityGraphChart.load();
    });
	
	$("#activityGraph [name='stats']").change(function() {
		if ($("#activityGraph [name='stats']:checked").size() == 0) {
			return;
		}
		activityGraphChart.load();
	});
	
	$("#activityGraph .period [name='periods']").change(function() {
		if ($("#activityGraph [name='stats']:checked").size() == 0) {
			return;
		}
		if ($(this).is(":checked")) {
			activityGraphChart.load();
		}
	});
	
	if ($("#activityGraph [name='stats']:checked").size() != 0) {
		activityGraphChart.load();
	}
	
	var twitterHashtagsChart = chart("twitterHashtags", "twitterHashtagsChart", "/api/twitter/hashtag/statistics", function() {
		var dateFrom = $("#twitterHashtags .dateFrom").val();
		var dateTo = $("#twitterHashtags .dateTo").val();
		var period = $("#twitterHashtags [name='thperiods']:checked").val()
		var stats = checkedStats();
		return {dateFrom:dateFrom + " UTC", dateTo:dateTo + " UTC", period:period};
	}, function(e) { 
		e.type = e.hashtag; e.date = utc(new Date(e.date * 86400000)); return e; 
	});
	
	datepicker("twitterHashtags", function(dateText, inst) {
		twitterHashtagsChart.load();
    });
	
	$("#twitterHashtags .period [name='thperiods']").change(function() {
		if ($(this).is(":checked")) {
			twitterHashtagsChart.load();
		}
	});

	twitterHashtagsChart.load();
	
});