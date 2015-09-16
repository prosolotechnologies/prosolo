/*
* jQuery PropagateHover v0.1

* Licensed under the MIT license.
* Copyright 2012 Milikic Nikola
* http://nikola.milikic.info
*/

(function($) {
	$.fn.propagateHover = function(options) {
	
		var opts = $.extend({}, $.fn.propagateHover.defaults, options);

		return this.each(function() {
			var $this = $(this);
			
			$this.hover(
				function () {
					$(this).addClass(opts.hoverClass);
					$(this).children().addClass(opts.hoverClass);
				},
				function () {
					$(this).removeClass(opts.hoverClass);
					$(this).children().removeClass(opts.hoverClass);
				}
			);
		});
	};
	
	$.fn.propagateHover.defaults = {
			'hoverClass': 'hover' //default
	};
})(jQuery);