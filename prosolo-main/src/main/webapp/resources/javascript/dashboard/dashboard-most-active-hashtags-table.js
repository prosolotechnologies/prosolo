// dependinces: [ "dashboard/table", "dashboard/callbacks" ]
var mostActiveHashtagsTable = (function () {

	var callbacks = new Callbacks();
	
	var hashtagsTable;
	
	function selectedHashtags() {
		return hashtagsTable.selected().map(function(e) {
			return e.dataset['hashtag'];
		});
	}
	
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
				callbacks.notify({"name" : "disable-clicked", "hashtag" : this.parentElement.parentElement.dataset["hashtag"]});
				return false;
			}
		}, {
			"name" : "show-in-table",
			"title" : "Show",
			"type" : "checkbox",
			"change" : function() {
				hashtagsTable.countSelected() >= 6 ? hashtagsTable.disableDeselected() : hashtagsTable.enableSelectors();
				callbacks.notify({"name" : "hashtags-selected", "selected" : selectedHashtags()});
			}
		} ]
	};
	
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
			hashtagsTable.countSelected() >= 6 ? hashtagsTable.disableDeselected() : hashtagsTable.enableSelectors();
		},
		hashtags : function() {
			return hashtagsTable.rows().map(function(e) {
				return e.dataset['hashtag'];
			});
		},
		selectedHashtags : selectedHashtags
	};
})();