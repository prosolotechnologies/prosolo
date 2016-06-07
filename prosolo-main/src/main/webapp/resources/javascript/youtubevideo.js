$(function() {
	setTimeout("initializeYouTubeAPI", 3000);
});

function initializeYouTubeAPI(options) {
	var defaults = {
		container: '.youtubevideodiv',
		page: null,
		learningContext: null,
		service: null,
		height : '270',
		width : '380'
	}
	
	var opts = $.extend({}, defaults, options);
	
	var players = new Array();
	
	function onYouTubeIframeAPIReady() {
		var temp = $(opts.container);
		
		for (var i = 0; i < temp.length; i++) {
			var videoId = $(temp[i]).attr('id');
			var player = new YT.Player(videoId, {
				height : opts.height,
				width : opts.width,
				videoId : $(temp[i]).attr('id'),
				events : {
					'onReady' : onPlayerReady,
					'onStateChange' : onPlayerStateChange
				}
			});
			players.push(player);
		}
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
		}
		if (event.data == endedState) {
			sendServiceUse("VIDEO", {
				"action" : "ENDED",
				"time" : time,
				"videoUrl" : videoUrl
			}, opts.page, opts.learningContext, opts.service);
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

	onYouTubeIframeAPIReady();
}
