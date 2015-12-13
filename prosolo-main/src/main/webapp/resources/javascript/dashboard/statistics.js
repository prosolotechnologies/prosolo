define(['jquery'], function($) {

    function negative(percentage) {
        return percentage.charAt(0) === "-";    
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
            url : "http://" + configuration.host + "/api/users/activity/statistics/sum",
            type : "GET",
            data : {event: "registered"},
            crossDomain: true,
            dataType: 'json'
        }).done(function(data) {
            $(configuration.count).html(data.totalUsers);
            $(configuration.percent + " > span").html(data.totalUsersPercent);
            var classes = trendClasses(data.totalUsersPercent);
            $(configuration.trend).removeClass(classes.removeTrend).addClass(classes.addTrend);
            $(configuration.percent).removeClass(classes.removeColor).addClass(classes.addColor);
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
            $(configuration.percent + " > span").html(data.activeUsersPercent);
            var classes = trendClasses(data.activeUsersPercent);
            $(configuration.trend).removeClass(classes.removeTrend).addClass(classes.addTrend);
            $(configuration.percent).removeClass(classes.removeColor).addClass(classes.addColor);
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
