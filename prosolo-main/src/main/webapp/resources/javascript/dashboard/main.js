require(['jquery', 'bootstrap', 'bootstrap-select', 'dashboard/paging', 'dashboard/datepicker', 'dashboard/service', 'dashboard/chart', 'dashboard/most-active-hashtags-table', 'dashboard/disabled-hashtags-table', 'dashboard/statistics'],
        function($, bootstrap, bootstrapSelect, paging, datepicker, service, chart, mostActiveHashtagsTable, disabledHashtagsTable, statistics) {
            $(function () {
                function dashboard() {
                    return document.querySelector("#dashboard");
                }
                
                function host() {
                    return dashboard().dataset["api"];
                }
                
                function noResultsMessage() {
                    return dashboard().dataset["noResultsFoundMessage"];
                }
                
                statistics.totalUsers({
                    host: host(),
                    count: "#total-users-count",
                    percent: "#total-users-count-percent",
                    trend: "#total-users-trend"
                });
                
                statistics.activeUsers({
                    host: host(),
                    count: "#active-users-count",
                    percent: "#active-users-count-percent",
                    trend: "#active-users-trend"
                });
                
                statistics.session({
                    host: host(),
                    count: "#currently-logged-in-count"
                });
                
                var activityGraph = (function() {
                    var loaded = false;
                    
                    var patterns = {
                        registered: "pattern-one",
                        login: "pattern-two",
                        homepagevisited: "pattern-three",
                        goalsviews: "pattern-four",
                        competencesviews: "pattern-five",
                        profileviews: "pattern-six"
                    };
                    
                    var agc = chart.create({
                        container : "#activityGraphChart",
                        x : "date",
                        y : "count",
                        color : "type",
                        tooltip : {
                            fields: ["date", "count", "type"]
                        },
                        brewer: function(data) { return patterns },
                        legend : {
                            selector: "#activityGraph .legend",
                            data: function() { return [{"name" : "Registered", "class" : "pattern-one"},
                                                       {"name" : "Logins", "class" : "pattern-two"},
                                                       {"name" : "Home page visited", "class" : "pattern-three"},
                                                       {"name" : "Goals views", "class" : "pattern-four"},
                                                       {"name" : "Competences views", "class" : "pattern-five"},
                                                       {"name" : "Profile views", "class" : "pattern-six"}]; }
                        }
                    });
                    
                    function displayLines() {
                    	$("#activityGraphChartLegend g").click(function() {
                    		var $g = $("#activityGraphChart g." + $(this).attr("class"));
                    		if ($g.size() == 0) {
                    			return;
                    		}
                            if ($g.css("opacity") == "0") {
                            	$g.css("opacity", "100");
                            	$(this).children("text").get(0).classList.remove("selected");
                            } else {
                            	$g.css("opacity", "0");
                            	$(this).children("text").get(0).classList.add("selected");
                            }
                        });
                    }
                    
                    return {
                        dateFrom : function() { return $("#activityGraph .dateFrom").val(); },
                        dateTo : function() { return $("#activityGraph .dateTo").val(); },
                        period : function() { return $("#activityGraph [name='periods']:checked").val(); },
                        stats : function() {
							return ["registered", "login", "homepagevisited", "goalsviews", "competencesviews", "profileviews"];
                        },
                        showLoader : function() {
                            $("#activityGraph .loader").show().siblings().hide();
                        },
                        onload : function(data) {
                            if (data.length==0) {
                                loaded = false;
                                $("#activityGraph .messages").text(noResultsMessage()).show().siblings().hide();
                            } else {    
                                loaded = true;
                                $("#activityGraph .chart").show().siblings().hide();
                                $("#activityGraph .legend").show();
                                var from = $("#activityGraph .dateFrom").datepicker("getDate");
                                var to = $("#activityGraph .dateTo").datepicker("getDate");
                                agc.show(data, from, to);
                                displayLines();
                            }
                        },
                        isLoaded : function() {
                            return loaded;
                        },
                        displayLines : displayLines
                    };
                })();
                
                var activityGraphService = service.create({
                    url : "http://" + host() + "/api/users/activity/statistics",
                    parameters : function() {
                        return {
                            dateFrom : activityGraph.dateFrom() + " UTC",
                            dateTo : activityGraph.dateTo() + " UTC",
                            period : activityGraph.period(), 
                            stats : activityGraph.stats()
                        };
                    },
                    data : function(e) { 
                        e.date = new Date(e.date); return e; 
                    }
                });
                
                $("#activityGraph .period .btn-primary").click(function() {
                	if ($(this).children("input").is(":checked")) {
                		return false;
                	}
                	$(this).children("input").prop("checked", true);
                    datepicker.align("#activityGraph .dateFrom", "#activityGraph .dateTo", activityGraph.period());
                    activityGraph.showLoader();
                    activityGraphService.get(activityGraph.onload);
                });
                                
                datepicker.init("activityGraph", function(dateText, inst) {
                    datepicker.align("#activityGraph .dateFrom", "#activityGraph .dateTo", activityGraph.period());
                    activityGraph.showLoader();
                    activityGraphService.get(activityGraph.onload);
                });
                
                datepicker.align("#activityGraph .dateFrom", "#activityGraph .dateTo", activityGraph.period());

                if (!activityGraph.isLoaded()) {
            		activityGraph.showLoader();
            		activityGraphService.get(activityGraph.onload);
                }
                
                $("#activityGraph .timeRange .input-group-addon").click(function() {
                	$(this).prev().click();
                });
                
                var twitterHashtagsService = service.create({
                    url : "http://" + host() + "/api/twitter/hashtag/statistics",
                    parameters : function() {
                        return {
                            dateFrom : twitterHashtags.dateFrom() + " UTC",
                            dateTo : twitterHashtags.dateTo() + " UTC",
                            period : twitterHashtags.period(),
                            hashtags : twitterHashtags.hashtags()
                        };
                    },
                    data : function(e) { 
                        e.date = new Date(e.date); return e; 
                    }
                });
                
                datepicker.init("twitterHashtagsGraph", function(dateText, inst) {
                    datepicker.align("#twitterHashtagsGraph .dateFrom", "#twitterHashtagsGraph .dateTo", twitterHashtags.period());
                    
                    twitterHashtags.showLoader();
                    twitterHashtagsService.get(twitterHashtags.onload);
                });
                
                $("#twitterHashtagsGraph .period .btn-primary").click(function() {
                	if ($(this).children("input").is(":checked")) {
                		return false;
                	}
                	$(this).children("input").prop("checked", true);
                    datepicker.align("#twitterHashtagsGraph .dateFrom", "#twitterHashtagsGraph .dateTo", twitterHashtags.period());
                    twitterHashtags.showLoader();
                    twitterHashtagsService.get(twitterHashtags.onload);
                });

                $("#twitterHashtagsGraph .timeRange .input-group-addon").click(function() {
                	$(this).prev().click();
                });
                
                var disabledHashtags = [];
                
                mostActiveHashtagsTable.create();
                mostActiveHashtagsTable.subscribe(function(event) {
                	if (event.name == "disable-clicked") {
	                    document.querySelector("#disable-form\\:hashtag-to-disable").value = event.hashtag;
	                    document.querySelector("#disable-form\\:disable-form-submit").click();
	                    disabledHashtags.push(event.hashtag);
	                    loadDh(disabledHashtags);
                	}
                });
                mostActiveHashtagsTable.subscribe(function(event) {
                	if (event.name == "hashtags-selected") {
                		console.log(event.selected);
                	}
                });

                
                (function () {
                    var navigation = document.querySelector("#mostActiveHashtags .navigation");
                    var paging = document.querySelector("#mostActiveHashtags .navigation .paging");
                    var term = document.querySelector("#mostActiveHashtags [name='hashtags-term']");
                    var messages = document.querySelector("#mostActiveHashtags .messages");
                    var followers = document.querySelector("#mostActiveHashtags [name='include-hashtags-without-followers']");
                    var statisticsPeriod = document.querySelector("#mostActiveHashtags #statisticsPeriod");
                    
                    function format(date) {
                        var day = date.getDate();
                        var month = date.getMonth() + 1;
                        var year = date.getFullYear();
                        
                        return day + "." + month + "." + year;
                    }
                    
                    function load() {
                        $.ajax({
                            url : "http://" + host() + "/api/twitter/hashtag/average",
                            type : "GET",
                            data : {page: navigation.dataset.current, paging: paging.value, term: term.dataset.term, includeWithoutFollowers: followers.checked},
                            crossDomain: true,
                            dataType: 'json'
                        }).done(function(data) {
                            statisticsPeriod.innerHTML = "";
                            if (data.results.length == 0) {
                                $(messages).html(noResultsMessage());
                                $(messages).show();
                                $(document.querySelector("#mostActiveHashtags table")).hide();
                                $(navigation).hide();
                            } else {
                                $(messages).hide();
                                $(document.querySelector("#mostActiveHashtags table")).show();
                                $(navigation).show();
                                mostActiveHashtagsTable.init(data.results);
                                if (data.day > 0) {
                                    var from = new Date(0);
                                    from.setUTCSeconds((data.day - 7) * 24 * 60 * 60);
                                    var to = new Date(0);
                                    to.setUTCSeconds(data.day * 24 * 60 * 60);
                                    statisticsPeriod.innerHTML = "(for period " + format(from) + " - " + format(to) + ")";
                                }
                            }
                            
                            twitterHashtags.showLoader();
                            twitterHashtagsService.get(twitterHashtags.onload);
                            
                            if (data.pages == 0) {
                                navigation.dataset.current = 1;
                            }
                            navigation.dataset.current = data.current;
                            navigation.dataset.pages = data.pages;
                            navigation.dataset.paging = data.paging;
                            
                            var page = document.querySelector("#mostActiveHashtags .navigation .page");
                            page.innerHTML = (data.pages == 0) ? 0 : data.current + "/" + data.pages;
                        });
                    }
                    
                    var previous = document.querySelector("#mostActiveHashtags .navigation .previous");
                    previous.addEventListener("click", function() {
                        if (navigation.dataset.current == 1) {
                            return false;
                        }
                        navigation.dataset.current--;
                        load();
                        return false;
                    });
                    
                    var next = document.querySelector("#mostActiveHashtags .navigation .next");
                    next.addEventListener("click", function() {
                        if (navigation.dataset.pages == navigation.dataset.current) {
                            return false;
                        }
                        navigation.dataset.current++;
                        load();
                        return false;
                    });
                    
                    var first = document.querySelector("#mostActiveHashtags .navigation .first");
                    first.addEventListener("click", function() {
                    	if (navigation.dataset.current == 1) {
                            return false;
                        }
                        navigation.dataset.current=1;
                        load();
                        return false;
                    });
                    
                    var last = document.querySelector("#mostActiveHashtags .navigation .last");
                    last.addEventListener("click", function() {
                    	if (navigation.dataset.current == navigation.dataset.pages) {
                            return false;
                        }
                        navigation.dataset.current=navigation.dataset.pages;
                        load();
                        return false;
                    });
                    
                    var paging = document.querySelector("#mostActiveHashtags .navigation .paging");
                    paging.addEventListener("change", function() { 
                        load();
                        return false;
                    });
                    
                    var filter = document.querySelector("#mostActiveHashtags #filter-most-active-hashtags");
                    var term = document.querySelector("#mostActiveHashtags [name='hashtags-term']");
                    filter.addEventListener("click", function() {
                        term.dataset.term = term.value;
                        load();
                        return false;
                    });
                    term.addEventListener("keypress", function(event ) {
                        if (event.key == 'Enter' || event.keyIdentifier == 'Enter'){
                            this.dataset.term = this.value;
                            load();
                        }
                        return false;
                    });
                    
                    followers.addEventListener("change", function(event ) {
                        load();
                        return false;
                    });
                    
                    load();             
                })();
                
                
                var twitterHashtags = (function() {
                    var patterns = [ "pattern-one", "pattern-two", "pattern-three", "pattern-four", "pattern-five", "pattern-six" ];
                    
                    function cycle() {
                        var index = 0;
                        return function() {
                            var result = patterns[index];
                            index = (index + 1) % patterns.length;
                            return result;
                        };
                    };
                    
                    var twitterHashtagsChart = chart.create({
                        container : "#twitterHashtagsChart",
                        x : "date",
                        y : "count",
                        color : "hashtag",
                        tooltip : {
                            fields: ["date", "count", "hashtag"]
                        },
                        brewer: function(data) {
                            return data.reduce(function(acc, element) {
                                if (!acc.result[element.hashtag]) {
                                    acc.result[element.hashtag] = acc.next() + " " + element.hastag;
                                }
                                return acc;
                            }, { result : {}, next : cycle() });
                        },
                        legend : {
                            selector: "#twitterHashtagsGraph .legend",
                            data: function() { var next = cycle(); return mostActiveHashtagsTable.hashtags().map(function(hashtag) { return {"name" : "#" + hashtag, "class" : next()}  }) }
                        }
                    });
                    
                    return {
                        dateFrom : function() { return $("#twitterHashtagsGraph .dateFrom").val(); },
                        dateTo : function() { return $("#twitterHashtagsGraph .dateTo").val(); },
                        period : function() { return $("#twitterHashtagsGraph [name='thperiods']:checked").val(); },
                        hashtags : mostActiveHashtagsTable.hashtags,
                        showLoader : function() {
                            $("#twitterHashtagsGraph .loader").show().siblings().hide();
                        },
                        onload : function(data) {
                            var from = $("#twitterHashtagsGraph .dateFrom").datepicker("getDate");
                            var to = $("#twitterHashtagsGraph .dateTo").datepicker("getDate");
                            if (data.length==0) {
                                $("#twitterHashtagsGraph .messages").text(noResultsMessage()).show().siblings().hide();
                            } else {
                                mostActiveHashtagsTable.selectFirst(5);
                                $("#twitterHashtagsGraph .chart").show().siblings().hide();
                                $("#twitterHashtagsGraph .legend").show();
                                twitterHashtagsChart.show(data, from, to);
                            }
                        }
                    };
                })();
                
                datepicker.align("#twitterHashtagsGraph .dateFrom", "#twitterHashtagsGraph .dateTo", twitterHashtags.period());
                
                disabledHashtagsTable.create();
                disabledHashtagsTable.subscribe(function(hashtag) {
                    document.querySelector("#enable-form\\:hashtag-to-enable").value = hashtag;
                    document.querySelector("#enable-form\\:enable-form-submit").click();
                    var index = disabledHashtags.indexOf(hashtag);
                    if (index > -1) {
                        disabledHashtags.splice(index, 1);
                    }
                    loadDh(disabledHashtags);
                });
                
                var disabledHashtagsPages = paging.create([], 5);
                
                $.ajax({
                    url : "http://" + host() + "/api/twitter/hashtag/disabled",
                    type : "GET",
                    crossDomain : true,
                    dataType : 'json'
                }).done(function(data) {
                    disabledHashtags = data;
                    loadDh(data);
                });
                
                function init(data) {
                    var page = document.querySelector("#disabled-twitter-hashtags .navigation .page");
                    $("#disabled-hashtags-count").html(data.size);
                    if (data.size == 0) {
                    	$("#disabled-twitter-hashtags .navigation").hide();
                    	$("#disabled-twitter-hashtags #disabled-hashtags-table").hide();
                    } else {
                    	$("#disabled-twitter-hashtags .navigation").show();
                    	$("#disabled-twitter-hashtags #disabled-hashtags-table").show();
	                    disabledHashtagsTable.init(data.result.map(function(hashtag) { return {"hashtag" : hashtag }}));
	                    page.innerHTML = data.page + "/" + data.pages;
                    }
                }
                
                function loadDh(data) {
                    var page = disabledHashtagsPages.current().page;
                    disabledHashtagsPages = paging.create(data, 5);
                    init(disabledHashtagsPages.page(page));
                }
                
                var previous = document.querySelector("#disabled-twitter-hashtags .navigation .previous");
                previous.addEventListener("click", function() {
                    init(disabledHashtagsPages.previous());
                    return false;
                });
                
                var next = document.querySelector("#disabled-twitter-hashtags .navigation .next");
                next.addEventListener("click", function() {
                    init(disabledHashtagsPages.next());
                    return false;
                });
                
                $(document).ajaxError(function() {
                    $("#system-not-available-notification").dialog({
                        resizable: false,
                        title: "Error.",
                        width: 'auto',
                        height: 'auto',
                        modal: true,
                        autoOpen: true,
                        buttons: {
                            "Ok": function() { $(this).dialog("close"); }
                        }
                    });         
                });
            });
        });
