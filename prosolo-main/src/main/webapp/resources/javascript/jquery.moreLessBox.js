(function($) {
	$.fn.moreLessBox = function(options) {
	
		var opts = $.extend({}, $.fn.moreLessBox.defaults, options);

		return this.each(function() {
			var $this = $(this);
			var expanded = false;
			
			var div = $this.attr('href');
			if(div.indexOf(';') != -1) {
				var divs = div.split(';');
				
				$.each(divs, function(index, value) { 
					$(value).css('display', 'none');
				});
			} else {
				$(div).css('display', 'none');
			}
			
			$this.on('click',function(){
				expanded = !expanded;
				
				if (expanded) {
					if (typeof opts.moreCallback === 'function') opts.moreCallback.call(this);
				} else {
					if (typeof opts.lessCallback === 'function') opts.lessCallback.call(this);
				}
				
				var div = $(this).attr('href');
				
				if(div.indexOf(';') != -1) {
					var divs = div.split(';');
					
					$.each(divs, function(index, value) { 
						$(value).slideToggle();
					});
				} else {
					$(div).slideToggle();
				}
				
				$(this).toggleClass(opts.lessClass).toggleClass(opts.moreClass);
				
				if (opts.lessText != "" && opts.moreText != "") {
					if ($(this).text() == opts.lessText) {
						$(this).text(opts.moreText);
					} else {
						$(this).text(opts.lessText);
					}
				}
				return false;
			});
		});
	};
	
	$.fn.moreLessBox.defaults = {
		lessClass: "boxLess",
		lessText: null,
		moreClass: "boxMore",
		moreText: null,
		moreCallback: $.noop,
		lessCallback: $.noop,
	};
})(jQuery);