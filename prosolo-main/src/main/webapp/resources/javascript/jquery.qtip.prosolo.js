/*
* jQuery Prosolo qTip v0.1

* Licensed under the MIT license.
* Copyright 2012 Milikic Nikola
* http://nikola.milikic.info
*/

(function($) {
	$.fn.tooltip = function(options) {
		
		var opts = $.extend({}, $.fn.tooltip.defaults, options);
		
		// reconfiguring
		if (opts.show == 'click') {
			opts.show = {
				event: 'click',
				solo: true // Only show one tooltip at a time
			};
		} else if (opts.show == 'hover') {
			opts.show = {
				event: 'hover',
				solo: true // Only show one tooltip at a time
			};
		}
		
		if (opts.hide == 'click') {
			opts.hide = {
				event: 'unfocus'
			};
		} else if (opts.hide == 'mouseout') {
			opts.hide = {
				when: {
					event:'mouseout unfocus'
				}, 
				fixed: true, 
				delay: 100
			};
		}

		return this.each(function() {
			var $this = $(this);
			
			// prevent from following the link
			$this.click(function(event){
				event.preventDefault();
			});
			
			$this.css('cursor', 'pointer');

			var tooltipElement = opts.content.target;
			
			if (tooltipElement.length > 0) {
				var tooltipElementClone = tooltipElement.clone();
				
				if (opts.content.copyContentTarget != null) {
					tooltipElementClone.find(opts.content.copyContentTarget).on('keyup', function(){
					    var input = $(this).val();
					    tooltipElement.find(opts.content.copyContentTarget).val(input);
					});
				}
				
				$this.qtip({
					content: {
						text: tooltipElementClone,
						title: opts.title
					},
					position: {
						at: 'bottom center', // Position the tooltip above the link
						my: 'top center',
						viewport: $(window), // Keep the tooltip on-screen at all times
					},
					show: opts.show,
					hide: opts.hide,
					style: {
						classes: 'ui-tooltip-wiki ui-tooltip-light ui-tooltip-rounded ui-tooltip-shadow ui-tooltip-width-unbounded'
					},
					events: {
						show: opts.callback.show,
						hide: opts.callback.hide
					}
				});
			}
		});
	};
	
	$.fn.tooltip.defaults = {
		callback: {
			show: null,
			hide: null
		},
		title: {
			text: null,
			button: true
		},
		show: 'click', // 'click', 'hover'
		hide: 'click', // 'click', 'mouseout'
		content: {
			copyContentTarget: null,
			target: null,
		}
	};
})(jQuery);