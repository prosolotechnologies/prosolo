define([ "dashboard/table", "dashboard/callbacks" ], function(table, Callbacks) {

	var callbacks = new Callbacks();

	var configuration = {
		"container" : "#disabled-twitter-hashtags",
		"rows" : {
			"class" : "hashtag"
		},
		"columns" : [ {
			"name" : "hashtag",
			"title" : "Hashtag",
			"type" : "text",
			"key" : "true"
		}, {
			"name" : "action",
			"title" : "Action",
			"type" : "button",
			"value" : "Enable",
			"click" : function() {
				this.setAttribute('disabled', 'disabled');
				var hashtag = this.parentElement.parentElement.dataset["hashtag"];
				callbacks.notify(hashtag);
				return false;		
			} 
		} ]
	};

	var hashtagsTable;
	return {
		subscribe : callbacks.subscribe,
		create : function() {
			hashtagsTable = table.create(configuration);
		},
		init : function(data) {
			hashtagsTable.init(data);
		}
	}

});