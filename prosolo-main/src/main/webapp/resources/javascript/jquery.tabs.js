/*
* jQuery Tabs v0.1

* Licensed under the MIT license.
* Copyright 2012 Milikic Nikola
* http://nikola.milikic.info
*/

(function($) {
	$.fn.tabs = function(options) {
		return this.each(function() {
			var $this = $(this);
			
			var opts = $.extend({}, $.fn.tabs.defaults, options || {});
			
			var tabLinks = $this.find('.tabLink');
			var tabContent = $this.children('.tabContent');
			
			// hide all tab contents
			tabContent.hide();
			
			// show only first one
			$(tabContent.get(0)).show();
			$(tabLinks.get(0)).addClass('selected');
			
			$(tabLinks).on('click', function(){
				var tabToShowId = $(this).attr("for-tab");
				
				$(tabContent).hide();
				if (opts.mode == 'fade'){
					$('div[tab-id="'+tabToShowId+'"]', $this).fadeIn();
				} else {
					$('div[tab-id="'+tabToShowId+'"]', $this).show();
				}
				
				$(tabLinks).removeClass('selected');
				$(this).addClass('selected');
				return false;
			});
		});
	};
	
	$.fn.tabs.defaults = {
		mode: null, // null, fade
	};
})(jQuery);