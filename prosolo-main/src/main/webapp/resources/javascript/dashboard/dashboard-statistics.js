var statistics = (function () {

	function nondef(percentage) {
		return percentage || percentage === "";
	}

    function negative(percentage) {
        return percentage.charAt(0) === "-";
    }

	function positive(percentage) {
        return percentage.charAt(0) === "+";
    }

	function trendValue(percentage) {
		if (nondef(percentage)) return "";
		if (percentage.length == 1) return "";
		return percentage.slice(1);
	}

    function trendClasses(percentage) {
        return {
            "removeTrend" : negative(percentage) ? "fa-arrow-up" : "fa-arrow-down",
            "addTrend" : negative(percentage) ? "fa-arrow-down" : "fa-arrow-up",
            "removeColor" : negative(percentage) ? "green" : "red",
            "addColor" : negative(percentage) ? "red" : "green"
        };
    }
    
    function totalUsers(configuration) {
        $.ajax({
            url : configuration.host + "/users/activity/statistics/sum",
            type : "GET",
            data : {event: "registered"},
            crossDomain: true,
            dataType: 'json'
        }).done(function(data) {
			$(configuration.count).html(data.totalUsers);
            $(configuration.percent + " > span").html(trendValue(data.totalUsersPercent));
			if (nondef(data.totalUsersPercent)) {
				$(configuration.trend).hide();
				$(configuration.percent).hide();
				return;
			}
			if (data.totalUsersPercent.length == 1) {
				$(configuration.percent).hide();
			}
            var classes = trendClasses(data.totalUsersPercent);
            $(configuration.trend).removeClass(classes.removeTrend).addClass(classes.addTrend);
            $(configuration.percent).removeClass(classes.removeColor).addClass(classes.addColor);
        });
    }
    
    function activeUsers(configuration) {       
        $.ajax({
            url : configuration.host + "/users/activity/statistics/active",
            type : "GET",
            data : {event: "login"},
            crossDomain: true,
            dataType: 'json'
        }).done(function(data) {
			$(configuration.count).html(data.activeUsers);
            $(configuration.percent + " > span").html(trendValue(data.activeUsersPercent));
			if (nondef(data.activeUsersPercent)) {
				$(configuration.trend).hide();
				$(configuration.percent).hide();
				return;
			}
			if (data.activeUsersPercent.length == 1) {
				$(configuration.percent).hide();
			}
            var classes = trendClasses(data.activeUsersPercent);
            $(configuration.trend).removeClass(classes.removeTrend).addClass(classes.addTrend);
            $(configuration.percent).removeClass(classes.removeColor).addClass(classes.addColor);
        });
    }
    
    function session(configuration) {
        $.ajax({
            url : configuration.host + "/users/activity/statistics/session",
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
    
})();
