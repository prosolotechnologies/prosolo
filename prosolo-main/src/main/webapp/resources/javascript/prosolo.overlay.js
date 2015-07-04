var prosolo = $.extend({}, prosolo, prosolo || {});

prosolo.overlay = {
	delay : 2,
	lastCall : null,
	timeout : null,
	activate : function(delay) {
		delay = typeof delay !== 'undefined' ? delay : this.delay;
		
		prosolo.overlay.timeout = window.setTimeout(
			function(){
				prosolo.overlay.show();
			}, delay * 1000);
	}, 
	reset: function() {
		if (prosolo.overlay.lastCall != null) {
			var diff = new Date().getTime() - prosolo.overlay.lastCall.getTime();
			
			if (diff < 1000) {
				window.setTimeout(
					function(){
						prosolo.overlay.hide();
					}, 1000 - diff);
				return;
			}
		}
		
		prosolo.overlay.hide();
	},
	show: function(){
		$('#loaderOverlay').show();
		prosolo.overlay.lastCall = new Date();
	},
	hide: function() {
		$('#loaderOverlay').hide();
		window.clearTimeout(prosolo.overlay.timeout);
	}
};