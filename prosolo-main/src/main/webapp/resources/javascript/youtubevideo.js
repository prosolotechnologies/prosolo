$(function() {
	setTimeout("initializeYouTubeAPI", 3000);
});

var initializeYouTubeAPI = (function () {
	var firstCall = true;
	return function (options) {
		var defaults = {
			container: '.youtubevideodiv',
			page: null,
			learningContext: null,
			service: null,
			height : '270',
			width : '380',
			captions : null
		}
		
		var opts = $.extend({}, defaults, options);
		
		opts.captions = [
		                 {"start": 3.060, "end": 5.820, "caption" : "BADGE ISSUING PLATFORM - PART EIGHT -"},
		                 {"start": 5.820, "end": 8.080, "caption" : "Bojan Tomic, Assistant Professor, University of Belgrade"},
		                 {"start": 8.220, "end": 12.660, "caption" : "We have a WordPress site, and the BadgeOS plugin installed on it."},
		                 {"start": 13.460, "end": 19.500, "caption" : "That way, we have a badge creating platform, badge issuing platform,"},
		                 {"start": 19.520, "end": 21.980, "caption" : "and a platform for grading students."},
		                 {"start": 22.640, "end": 25.360, "caption" : "Students need to register on that the website."},
		                 {"start": 25.360, "end": 27.180, "caption" : "You are registered as an administrator."},
		                 {"start": 27.380, "end": 32.320, "caption" : "When a student completes his task, he submits it on the website."},
		                 {"start": 32.960, "end": 37.180, "caption" : "You, as a site administrator, are notified about the submission."},
		                 {"start": 37.240, "end": 41.460, "caption" : "You check the submitted solution, and if everything is OK, you award him a badge."},
		                 {"start": 41.480, "end": 47.160, "caption" : "Later on, a student can log in on the website and see all the badges he won until that point."},
		                 {"start": 47.400, "end": 52.760, "caption" : "He can publish them to some publicly available platform for badge publishing."},
		                 {"start": 52.980, "end": 55.760, "caption" : "On of them is the Mozilla Backpack."},
		                 {"start": 56.700, "end": 59.380, "caption" : "Another one is the Credly."},
		                 {"start": 61.340, "end": 69.440, "caption" : "All badges our students won are published to some of those badge displaying platforms."},
		                 {"start": 70.040, "end": 74.880, "caption" : "And there, completely independent of our platform, even if we shut down our website,"}
		                 
		               ];
		
		//var players = new Array();
		
		function loadPlayerOnReady() {
	    	if (typeof(YT) == 'undefined' || typeof(YT.Player) == 'undefined') {
		        if (firstCall) {
		            firstCall = false;
		            window.onYouTubeIframeAPIReady = function() {
				        loadPlayer();
				    };
				    
				    $.getScript('//www.youtube.com/iframe_api');
		        } else {
		        	setTimeout(loadPlayerOnReady, 10);
		        }
	    	} else {
	    		loadPlayer();
	    	}
	    };
		
		function loadPlayer() {
			var temp = $(opts.container);
					
			for (var i = 0; i < temp.length; i++) {
				var videoId = $(temp[i]).data('video');
				var divId = $(temp[i]).attr('id');
				
				var player = new YT.Player(divId, {
					height : opts.height,
					width : opts.width,
					videoId : videoId,
					events : {
						'onReady' : onPlayerReady,
						'onStateChange' : onPlayerStateChange
					}
				});
				//players.push(player);
			}
		}
		
		var currentCaptionStartTimeout;
		var currentCaptionEndTimeout;
		
		function setChainedCaptionTimeout(time, i) {
			var startDiff = opts.captions[i].start - time;
			if(startDiff < 0) {
				startDiff = 0;
			}
			var endDiff = opts.captions[i].end - time;
			currentCaptionStartTimeout = setTimeout(function(){ console.log("START: " + opts.captions[i].caption); }, startDiff * 1000);
			currentCaptionEndTimeout = setTimeout(function(){
				console.log("END: " + opts.captions[i].caption);
				if(opts.captions.length > i + 1) {
					setChainedCaptionTimeout(time + endDiff, i + 1);
				}
			}, endDiff * 1000);
		}
		
		function onPlayerReady(e) {
		}
		
		var PAUSE_EVT_STACK = 0;
		
		function onPlayerStateChange(event) {
			var playingState = YT.PlayerState.PLAYING;
			var endedState = YT.PlayerState.ENDED;
			var pausedState = YT.PlayerState.PAUSED;
			var bufferingState = YT.PlayerState.BUFFERING;
			var cuedState = YT.PlayerState.CUED;
			
			var target = event.target;
			var time = target.getCurrentTime();
			var videoUrl = target.getVideoUrl();
			
			if (event.data == playingState) {
				PAUSE_EVT_STACK = 0;
				sendServiceUse("VIDEO", {
					"action" : "PLAYING",
					"time" : time,
					"videoUrl" : videoUrl
				}, opts.page, opts.learningContext, opts.service);
				
				if(opts.captions != null) {
					for(var i = 0; i < opts.captions.length; i++) {
						if(i == 0 && time < opts.captions[i].start || time >= opts.captions[i].start && time < opts.captions[i].end) {
							setChainedCaptionTimeout(time, i);
							break;
						}
					}
				}
			}
			if (event.data == endedState) {
				sendServiceUse("VIDEO", {
					"action" : "ENDED",
					"time" : time,
					"videoUrl" : videoUrl
				}, opts.page, opts.learningContext, opts.service);
				clearTimeout(currentCaptionStartTimeout);
				clearTimeout(currentCaptionEndTimeout);
			}
			if (event.data == pausedState) {
				PAUSE_EVT_STACK++;
				if (PAUSE_EVT_STACK <= 1) {
					sendServiceUse("VIDEO", {
						"action" : "PAUSE",
						"time" : time,
						"videoUrl" : videoUrl
					}, opts.page, opts.learningContext, opts.service);
				} else if (PAUSE_EVT_STACK > 1) {
					sendServiceUse("VIDEO", {
						"action" : "FAST-FORWARD",
						"time" : time,
						"videoUrl" : videoUrl
					}, opts.page, opts.learningContext, opts.service);
				}
				clearTimeout(currentCaptionStartTimeout);
				clearTimeout(currentCaptionEndTimeout);
			}
			if (event.data == bufferingState) {
				sendServiceUse("VIDEO", {
					"action" : "BUFFERING",
					"time" : time,
					"videoUrl" : videoUrl
				}, opts.page, opts.learningContext, opts.service);
			}
			if (event.data == cuedState) {
				sendServiceUse("VIDEO", {
					"action" : "CUED",
					"time" : time,
					"videoUrl" : videoUrl
				}, opts.page, opts.learningContext, opts.service);
			}
		}
	
		loadPlayerOnReady(loadPlayer);
	}
})();

function onYouTubeIframeAPIReady() {
	
}
