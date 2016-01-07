define([ "dashboard/table", "dashboard/callbacks" ], function(table, Callbacks) {

	var callbacks = new Callbacks();
	
	var hashtagsTable;

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
				hashtagsTable.countSelected() >= 5 ? hashtagsTable.disableDeselected() : hashtagsTable.enableSelectors()
				// $(this).parent().parent().parent().find("tr > td.selector > input:not(:checked)").prop('disabled', true);
				// $(this).parent().parent().parent().find("tr > td.selector > input").prop('disabled', false);
				// TODO raise event.
			}
		} ]
	}
	
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
			hashtagsTable.countSelected() >= 5 ? hashtagsTable.disableDeselected() : hashtagsTable.enableSelectors()
		},
		hashtags : function() {
			return hashtagsTable.rows().map(function(e) {
				return e.dataset['hashtag'];
			});
		}
	}

});