define(['jquery'], function($) {

	function trendClasses(percentage) {
		var negative = percentage.charAt(0) === "-";
		return {
			"remove" : negative ? "trend-up" : "trend-down",
			"add" : negative ? "trend-down" : "trend-up"
		}
	}
	
	function totalUsers(configuration) {
		$.ajax({
			url : "http://" + configuration.host + "/api/users/activity/statistics/sum",
			type : "GET",
			data : {event: "registered"},
			crossDomain: true,
			dataType: 'json'
		}).done(function(data) {
			$(configuration.count).html(data.totalUsers);
			$(configuration.percent).html(data.totalUsersPercent);
			var classes = trendClasses(data.totalUsersPercent);
			$(configuration.trend + ", " + configuration.percent).removeClass(classes.remove).addClass(classes.add);
		});
	}
	
	function activeUsers(configuration) {	
		$.ajax({
			url : "http://" + configuration.host + "/api/users/activity/statistics/active",
			type : "GET",
			data : {event: "login"},
			crossDomain: true,
			dataType: 'json'
		}).done(function(data) {
			$(configuration.count).html(data.activeUsers);
			$(configuration.percent).html(data.activeUsersPercent);
			var classes = trendClasses(data.activeUsersPercent);
			$(configuration.trend + ", " + configuration.percent).removeClass(classes.remove).addClass(classes.add);
		});
	}
	
	function session(configuration) {
		$.ajax({
			url : "http://" + configuration.host + "/api/users/activity/statistics/session",
			type : "GET",
			crossDomain: true,
			dataType: 'json'
		}).done(function(data) {
			$(configuration.count).html(data.loggedIn);
		});
	}
	
	return {
		totalUsers : totalUsers,
		activeUsers : activeUsers,
		session : session
	};
	
});