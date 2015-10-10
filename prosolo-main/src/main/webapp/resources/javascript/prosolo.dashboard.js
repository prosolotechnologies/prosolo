$(function () {

	function utc(date) { 
		return new Date(date.getUTCFullYear(), date.getUTCMonth(), date.getUTCDate(), date.getUTCHours(), date.getUTCMinutes(), date.getUTCSeconds()); 
	}
	
	function dashboard() {
		return document.querySelector("#dashboard");
	}
	
	function host() {
		return dashboard().dataset["api"];
	}
	
	function noResultsMessage() {
		return dashboard().dataset["noResultsFoundMessage"];
	}
		
	function trendClasses(percentage) {
		var negative = percentage.charAt(0) === "-";
		return {
			"remove" : negative ? "trend-up" : "trend-down",
			"add" : negative ? "trend-down" : "trend-up"
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
		var classes = trendClasses(data.totalUsersPercent);
		$("#total-users-trend, #total-users-count-percent").removeClass(classes.remove).addClass(classes.add);
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
		var classes = trendClasses(data.activeUsersPercent);
		$("#active-users-trend, #active-users-count-percent").removeClass(classes.remove).addClass(classes.add);
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
		var agc = chart.create({
			container : "#activityGraphChart",
			x : "date",
			y : "count",
			color : "type",
			tooltip : {
				fields: ["date", "count", "type"]
			}
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
					$("#activityGraph .messages").text(noResultsMessage()).show().siblings().hide();
				} else {
					$("#activityGraph .chart").show().siblings().hide();
					agc.show(data);
				}
			}
		}
	})();
	
	var activityGraphService = service.create({
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
	
	datepicker.init("activityGraph", function(dateText, inst) {
		if ($("#activityGraph [name='stats']:checked").size() == 0) {
			return;
		}
		activityGraph.showLoader();
		activityGraphService.get(activityGraph.onload);
    });
	
	var twitterHashtagsService = service.create({
		url : "http://" + host() + "/api/twitter/hashtag/statistics",
		parameters : function() {
			return {
				dateFrom : twitterHashtags.dateFrom() + " UTC",
				dateTo : twitterHashtags.dateTo() + " UTC",
				period : twitterHashtags.period(),
				hashtags : twitterHashtags.hashtags()
			}
		},
		data : function(e) { 
			e.date = utc(new Date(e.date * 86400000)); return e; 
		}
	});
	
	datepicker.init("twitterHashtagsGraph", function(dateText, inst) {
		twitterHashtags.showLoader();
		twitterHashtagsService.get(twitterHashtags.onload);
    });
	
	$("#twitterHashtagsGraph .period [name='thperiods']").change(function() {
		if ($(this).is(":checked")) {
			twitterHashtags.showLoader();
			twitterHashtagsService.get(twitterHashtags.onload);
		}
	});
	
	var hashtagsInTable = [];
	
	(function () {
		var navigation = document.querySelector("#mostActiveHashtags .navigation");
		var paging = document.querySelector("#mostActiveHashtags .navigation .paging");
		var term = document.querySelector("#mostActiveHashtags [name='hashtags-term']");
		var messages = document.querySelector("#mostActiveHashtags .messages");
		var table = document.querySelector("#mostActiveHashtags table");
		var followers = document.querySelector("#mostActiveHashtags [name='include-hashtags-without-followers']");
		
		function load() {
			
			$.ajax({
				url : "http://" + host() + "/api/twitter/hashtag/average",
				type : "GET",
				data : {page: navigation.dataset.current, paging: paging.value, term: term.dataset.term, includeWithoutFollowers: followers.checked},
				crossDomain: true,
				dataType: 'json'
			}).done(function(data) {
				if (data.results.length == 0) {
					$(messages).html(noResultsMessage());
					$(messages).show();
					$(table).hide();
					$(navigation).hide();
				} else {
					$(messages).hide();
					$(table).show();
					$(navigation).show();
				}
				
				var tbody = document.querySelector("#mostActiveHashtags tbody");
				tbody.innerHTML = "";
				function td(value) {
					var td = document.createElement("td");
					td.innerHTML = value;
					return td;			
				}
				function button(hashtag) {
					return $("<button>Disable</button>").click(function() {
						$(this).attr('disabled', 'disabled');
						document.querySelector("#disable-form\\:hashtag-to-disable").value = hashtag.hashtag;
						document.querySelector("#disable-form\\:disable-form-submit").click();
						return false;
					})[0];
				}
				function tdDisable(button) {
					var td = $("<td></td>")[0];
					td.appendChild(button);
					return td;
				}
				hashtagsInTable = [];
				data.results.map(function(hashtag) {
					hashtagsInTable.push(hashtag.hashtag)
					var tr = document.createElement("tr");
					tr.classList.add("hashtag");
					tr.appendChild(td(hashtag.number));
					tr.appendChild(td(hashtag.hashtag));
					tr.appendChild(td(hashtag.average));
					tr.appendChild(td(hashtag.users));
					tr.appendChild(tdDisable(button(hashtag)));
					tbody.appendChild(tr);
				});
				
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
			navigation.dataset.current=1;
			load();
			return false;
		});
		
		var last = document.querySelector("#mostActiveHashtags .navigation .last");
		last.addEventListener("click", function() {
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
				return false;
			}
		});
		
		followers.addEventListener("change", function(event ) {
			load();
			return false;
		});
		
		load();		
	})();
	
	
	var twitterHashtags = (function() {
		var thc = chart.create({
			container : "#twitterHashtagsChart",
			x : "date",
			y : "count",
			color : "hashtag",
			tooltip : {
				fields: ["date", "count", "hashtag"]
			}
		});

		return {
			dateFrom : function() { return $("#twitterHashtagsGraph .dateFrom").val(); },
			dateTo : function() { return $("#twitterHashtagsGraph .dateTo").val(); },
			period : function() { return $("#twitterHashtagsGraph [name='thperiods']:checked").val(); },
			hashtags : function() { return hashtagsInTable; },
			showLoader : function() {
				$("#twitterHashtagsGraph .loader").show().siblings().hide();
			},
			onload : function(data) {
				if (data.length==0) {
					$("#twitterHashtagsGraph .messages").text(noResultsMessage()).show().siblings().hide();
				} else {
					$("#twitterHashtagsGraph .chart").show().siblings().hide();
					thc.show(data);
				}
			}
		}
	})();
	
	(function () {
    	$.ajax({
    		url : "http://" + host() + "/api/twitter/hashtag/disabled-count",
    		type : "GET",
    		crossDomain : true,
    		dataType : 'json'
    	}).done(function(data) {
    		$("#disabled-hashtags-count").html(data.count);
    	});
	})();
	
	(function () {
	    $("#disabled-hashtags-dialog").dialog({
	    	resizable: false,
	    	title: "Disabled hashtags",
	        width: 'auto',
	        height: 'auto',
		    modal: true,
		    autoOpen: false,
		    close: function() { $("#disabled-hashtags-table tbody").html(""); }
	    });
	    $("#view-disabled-hashtags").click(function() {
	    	$.ajax({
	    		url : "http://" + host() + "/api/twitter/hashtag/disabled",
	    		type : "GET",
	    		crossDomain : true,
	    		dataType : 'json'
	    	}).done(function(data) {
	    		var tbody = document.querySelector("#disabled-hashtags-table tbody");
	    		tbody.innerHTML = "";
	    		function td(value) {
	    			var td = document.createElement("td");
	    			td.innerHTML = value;
	    			return td;			
	    		}
	    		function button(hashtag) {
	    			return $("<button>Enable</button>").click(function() {
	    				$(this).attr('disabled', 'disabled');
	    				document.querySelector("#enable-form\\:hashtag-to-enable").value = hashtag;
	    				document.querySelector("#enable-form\\:enable-form-submit").click();
	    				return false;
	    			})[0];
	    		}
	    		function tdEnable(button) {
	    			var td = $("<td></td>")[0];
	    			td.appendChild(button);
	    			return td;
	    		}
	    		hashtagsInTable = [];
	    		data.map(function(hashtag) {
	    			var tr = document.createElement("tr");
	    			tr.classList.add("hashtag");
	    			tr.appendChild(td(hashtag));
	    			tr.appendChild(tdEnable(button(hashtag)));
	    			tbody.appendChild(tr);
	    		});
	    		$("#disabled-hashtags-count").html(data.length);
	    		$("#disabled-hashtags-dialog").dialog("open");
	    	});
	    });
	})();
	
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