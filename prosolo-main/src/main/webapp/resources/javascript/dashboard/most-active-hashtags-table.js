define([ "dashboard/table", "dashboard/callbacks" ], function(table, Callbacks) {

	var callbacks = new Callbacks();

	var configuration = {
		"container" : "#mostActiveHashtags",
		"rows" : {
			"class" : "hashtag"
		},
		"columns" : [ {
			"name" : "number",
			"title" : "Number",
			"type" : "text"
		}, {
			"name" : "hashtag",
			"title" : "Hashtag",
			"type" : "text",
			"key" : "true"
		}, {
			"name" : "average",
			"title" : "Daily avg. (last week)",
			"type" : "text"
		}, {
			"name" : "users",
			"title" : "Users using it",
			"type" : "text"
		}, {
			"name" : "action",
			"title" : "Action",
			"type" : "button",
			"value" : "Disable",
			"click" : function() {
				this.setAttribute('disabled', 'disabled');
				callbacks.notify(this.parentElement.parentElement.dataset["hashtag"]);
				return false;
			}
//		}, {
//			"name" : "show-in-table",
//			"title" : "Show",
//			"type" : "checkbox",
//			"change" : function() {
//				var hashtag = $(this).parent().parent().data("hashtag");
//				if($(this).is(":checked")) {
//					$("#twitterHashtagsChart g." + hashtag).show();
//					//$("g ." + hashtag).removeClass(Array.prototype.slice.call($("g ." + hashtag)[0].classList).filter(function(c) { return c.indexOf("pattern") > -1;}));
//				} else {
//					//$("g ." + hashtag).removeClass(Array.prototype.slice.call($("g ." + hashtag)[0].classList).filter(function(c) { return c.indexOf("pattern") > -1;}));
//					$("#twitterHashtagsChart g." + hashtag).hide();
//				}
//			}
		} ]
	}

	var hashtagsTable;
	return {
		subscribe : callbacks.subscribe,
		create : function() {
			hashtagsTable = table.create(configuration);
		},
		init : function(data) {
			hashtagsTable.init(data);
		},
		selectFirst : function(count) {
			hashtagsTable.selectFirst(count);
		},
		hashtags : function() {
			return hashtagsTable.rows().map(function(e) {
				return e.dataset['hashtag'];
			});
		}
	}

});