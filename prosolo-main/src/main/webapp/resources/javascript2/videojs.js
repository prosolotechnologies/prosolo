var initializeVideo = (function () {
	return function (options) {
		var defaults = {
			page: null,
			learningContext: null,
			service: null
		}
		
		var opts = $.extend({}, defaults, options);
		var lastEvent = -1;
		$('video').mediaelementplayer({
		    youtubeIframeVars: {
		        controls: 0,
		        iv_load_policy: 3,
		        modestbranding: 1,
		        rel: 0,
		        showinfo: 0,
		    },
		    success: function(media, node, player) {
		        $('#' + node.id + '-mode').html('mode: ' + media.pluginType);
		        
		        media.addEventListener('play', function(e) {
		        	lastEvent = 1;
	                sendServiceUse("VIDEO", {
						"action" : "PLAYING",
						"time" : e.currentTime
					}, opts.page, opts.learningContext, opts.service);
	            });
		        
		        media.addEventListener('pause', function(e) {
		        	if(lastEvent != 2) { 
			        	sendServiceUse("VIDEO", {
							"action" : "PAUSE",
							"time" : e.currentTime
						}, opts.page, opts.learningContext, opts.service);
		        	}
		        	lastEvent = 2;
	            });
		        
		        media.addEventListener('ended', function(e) {
		        	lastEvent = 3;
	                sendServiceUse("VIDEO", {
						"action" : "ENDED",
						"time" : e.currentTime
					}, opts.page, opts.learningContext, opts.service);
	            });
		    }
		});
	}
})();