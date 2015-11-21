define([ "dashboard/table" ], function(table) {

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
				notify(this.parentElement.parentElement.dataset["hashtag"]);
				return false;
			}
		}, {
			"name" : "show-in-table",
			"title" : "Show",
			"type" : "checkbox",
			"change" : function() {
			}
		} ]
	}

	// TODO separate module? inheritance?
	var callbacks = [];

	function notify(changes) {
		callbacks.map(function(callback) {
			callback(changes);
		});
	}

	function subscribe(callback) {
		callbacks.push(callback);
	}
	// TODO END

	var hashtagsTable;
	return {
		subscribe : subscribe,
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