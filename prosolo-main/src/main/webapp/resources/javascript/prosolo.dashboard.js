$.extend($.datepicker,{_checkOffset:function(inst,offset,isFixed){return offset}});

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

$(function () {
	function utc(date) { 
		return new Date(date.getUTCFullYear(), date.getUTCMonth(), date.getUTCDate(), date.getUTCHours(), date.getUTCMinutes(), date.getUTCSeconds()); 
	}
	
	function host() {
		return $("#dashboard").data("api");
	}
	
	function chart(configuration) {
		var charts = [];

		function destroyCharts() {
			charts.map(function(chart) {
				chart.destroy();
			});
			charts = [];
		}
		
		function show(data) {
			$("#" + configuration.container).html("");
			destroyCharts();
			var chart = new tauCharts.Chart({
			    data: data,
			    type: 'line',
			    x: configuration.x,
			    y: configuration.y,
			    color: configuration.color
			});
			chart.renderTo("#" + configuration.container);
			charts.push(chart);
		}
		
		return {
			show : show
		};
	}
	

	function service(configuration) {
		function get(callback) {
			$.ajax({
				url : configuration.url,
				type : "GET",
				data : configuration.parameters(),
				crossDomain	: true,
				dataType: 'json'
			}).done(function(data) {
				callback(data.map(configuration.data));
			});
		}
		return {
			get : get
		}
	}
	
	$.ajax({
		url : "http://" + host() + "/api/users/activity/statistics/sum",
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
		url : "http://" + host() + "/api/users/activity/statistics/active",
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
		url : "http://" + host() + "/api/users/activity/statistics/session",
		type : "GET",
		crossDomain: true,
		dataType: 'json'
	}).done(function(data) {
		$("#currently-logged-in-count").html(data.loggedIn);
	});
	
	var activityGraph = (function() {
		var agc = chart({
			container : "activityGraphChart",
			x : "date",
			y : "count",
			color : "type"
		});

		return {
			dateFrom : function() { return $("#activityGraph .dateFrom").val(); },
			dateTo : function() { return $("#activityGraph .dateTo").val(); },
			period : function() { return $("#activityGraph [name='periods']:checked").val(); },
			stats : function() {
				return $("#activityGraph [name='stats']:checked").map(function() { return $(this).val(); }).get();
			},
			showLoader : function() {
				$("#activityGraph .loader").show().siblings().hide();
			},
			onload : function(data) {
				if (data.length==0) {
					$("#activityGraph .chartMessages").show().siblings().hide();
				} else {
					$("#activityGraph .chart").show().siblings().hide();
					agc.show(data);
				}
			}
		}
	})();
	
	var activityGraphService = service({
		url : "http://" + host() + "/api/users/activity/statistics",
		parameters : function() {
			return {
				dateFrom : activityGraph.dateFrom() + " UTC",
				dateTo : activityGraph.dateTo() + " UTC",
				period : activityGraph.period(), 
				stats : activityGraph.stats()
			}
		},
		data : function(e) { 
			e.date = utc(new Date(e.date * 86400000)); return e; 
		}
	});
	
	$("#activityGraph [name='stats']").change(function() {
		if ($("#activityGraph [name='stats']:checked").size() == 0) {
			return;
		}
		activityGraph.showLoader();
		activityGraphService.get(activityGraph.onload);
	});
	
	$("#activityGraph .period [name='periods']").change(function() {
		if ($("#activityGraph [name='stats']:checked").size() == 0) {
			return;
		}
		if ($(this).is(":checked")) {
			activityGraph.showLoader();
			activityGraphService.get(activityGraph.onload);
		}
	});
	
	if ($("#activityGraph [name='stats']:checked").size() != 0) {
		activityGraph.showLoader();
		activityGraphService.get(activityGraph.onload);
	}
	
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
	
	datepicker("activityGraph", function(dateText, inst) {
		if ($("#activityGraph [name='stats']:checked").size() == 0) {
			return;
		}
		activityGraph.showLoader();
		activityGraphService.get(activityGraph.onload);
    });
	
	var twitterHashtags = (function() {
		var thc = chart({
			container : "twitterHashtagsChart",
			x : "date",
			y : "count",
			color : "hashtag"
		});

		return {
			dateFrom : function() { return $("#twitterHashtagsGraph .dateFrom").val(); },
			dateTo : function() { return $("#twitterHashtagsGraph .dateTo").val(); },
			period : function() { return $("#twitterHashtagsGraph [name='thperiods']:checked").val(); },
			showLoader : function() {
				$("#twitterHashtagsGraph .loader").show().siblings().hide();
			},
			onload : function(data) {
				if (data.length==0) {
					$("#twitterHashtagsGraph .chartMessages").show().siblings().hide();
				} else {
					$("#twitterHashtagsGraph .chart").show().siblings().hide();
					thc.show(data);
				}
			}
		}
	})();
	
	var twitterHashtagsService = service({
		url : "http://" + host() + "/api/twitter/hashtag/statistics",
		parameters : function() {
			return {
				dateFrom : twitterHashtags.dateFrom() + " UTC",
				dateTo : twitterHashtags.dateTo() + " UTC",
				period : twitterHashtags.period()
			}
		},
		data : function(e) { 
			e.date = utc(new Date(e.date * 86400000)); return e; 
		}
	});

	datepicker("twitterHashtagsGraph", function(dateText, inst) {
		twitterHashtags.showLoader();
		twitterHashtagsService.get(twitterHashtags.onload);
    });
	
	$("#twitterHashtagsGraph .period [name='thperiods']").change(function() {
		if ($(this).is(":checked")) {
			twitterHashtags.showLoader();
			twitterHashtagsService.get(twitterHashtags.onload);
		}
	});

	twitterHashtags.showLoader();
	twitterHashtagsService.get(twitterHashtags.onload);
	
	
	(function () {
		var currentPage = 1;
		var pages = 0;
		
		function load() {
			$.ajax({
				url : "http://" + host() + "/api/twitter/hashtag/average",
				type : "GET",
				data : {page: currentPage},
				crossDomain: true,
				dataType: 'json'
			}).done(function(data) {
				var tbody = document.querySelector("#mostActiveHashtags tbody");
				tbody.innerHTML = "";
				function td(value) {
					var td = document.createElement("td");
					td.innerHTML = value;
					return td;			
				}
				
				data.results.map(function(hashtag) {
					var tr = document.createElement("tr");
					tr.classList.add("hashtag");
					tr.appendChild(td(hashtag.number));
					tr.appendChild(td(hashtag.hashtag));
					tr.appendChild(td(hashtag.average));
					tr.appendChild(td(0));
					tr.appendChild(td(0));
					tbody.appendChild(tr);
				});
				
				var page = document.querySelector("#mostActiveHashtags .navigation .page");
				if (data.pages == 0) {
					currentPage = 1;
				}
				pages = data.pages;
				page.innerHTML = (pages == 0) ? 0 : currentPage + "/" + pages;
			});
		}
		
		var previous = document.querySelector("#mostActiveHashtags .navigation .previous");
		previous.addEventListener("click", function() {
			if (currentPage == 1) {
				return false;
			}
			currentPage--;
			load();
			return false;
		});
		
		var next = document.querySelector("#mostActiveHashtags .navigation .next");
		next.addEventListener("click", function() {
			if (pages == currentPage) {
				return false;
			}
			currentPage++;
			load();
			return false;
		});
		
		load();
	})();

});