/*
* jQuery ShortenedText v0.1

* Licensed under the MIT license.
* Copyright 2012 Milikic Nikola
* http://nikola.milikic.info
*/

(function($) {
	$.fn.shortenedText = function(options) {
		
		var opts = $.extend({}, $.fn.shortenedText.defaults, options);

		return this.each(function() {
			var $this = $(this);

			var getTextLength = function(text) {
				text = text.trim();
				var temp = jQuery('<div/>', {
			        style: 'display: none;',
			        text: text
			    });
			    $this.append(temp);
			    var tempWidth = temp.width();
			    temp.remove();
			    return tempWidth;
			};
			
  			var content = $(this).html();
  			
  			// stupid hack
  			// if there are HTML tags, than return
  			//if (content.match(/<(\w+)((?:\s+\w+(?:\s*=\s*(?:(?:"[^"]*")|(?:'[^']*')|[^>\s]+))?)*)\s*(\/?)>/)) {
  			//	return;
  			//}
			
			if (opts.mode == 'max-width') {
				var elemWidth = $this.width();
				
				if (elemWidth > opts.maxWidth) {
					var ratio = (elemWidth - opts.maxWidth) / elemWidth;
					opts.showChar = Math.round($this.text().length * (1-ratio));
				} else {
					return;
				}
			}
			
			if (opts.mode == 'csv-max-width') {
				// not completed
				var elemWidth = getTextLength($this.text());
				
				if (elemWidth > opts.maxWidth) {
					var parts = $this.text().split(",");
					parts = jQuery.map(parts, function (a) {
						return a.trim();
					});
					
					var first = parts[0];
					var rest = new Array();
					var output = first;
					var index = 0;
					
					while (getTextLength(first + (rest.length>0 ? ', '+rest : '')) < opts.maxWidth) {
						output = first + (rest.length>0 ? ', '+rest : '');
						
						var joined = rest.join(', ');
					    
					    if (getTextLength(first+','+joined) < opts.maxWidth) {
					    	return;
					    }
					    index++;
					}

				    if (getTextLength(output) < opts.maxWidth) {
				    	return;
				    }
					
					for (var i = parts.length-1; i > 0; i--) {
					    if (i > 0) {
					    	rest[parts.length-2] = parts[parts.length-1];
					    }

					    var joined = rest.join(', ');
					    
					    if (getTextLength(first+','+joined) < opts.maxWidth) {
					    	return;
					    }
					}
					return;
				} else {
					return;
				}
			}
			
			var shortWord = function (text, suffix) {
				var lastSpaceIndex = text.lastIndexOf(' ');
				
				if (lastSpaceIndex > 0) {
					var begin = text.substr(0, lastSpaceIndex);
					var last = text.substr(lastSpaceIndex+1, content.length);
					
					var newSuffix = last.charAt(0) +'. '+suffix;
					var newText = begin + ' ' + newSuffix;
					
					if (newText.length > opts.showChar) {
						return shortWord(begin, newSuffix);
					} else {
						return newText;
					}
				} else {
					// there is only one word
					return text.substr(0, opts.showChar-3)+'...'; // -3 because of ... added at the end
				}
			};
			
			if (opts.mode == 'show-first-word') {
				if (content.length > opts.showChar) {
					var lastSpaceIndex = content.lastIndexOf(' ');
					
					$(this).html(content.substr(0, lastSpaceIndex));
					return;
				}
			}
			
			// this is the same for all other modes
			if (content.length > opts.showChar) {
	            var c = content.substr(0, opts.showChar);
	            var h = content.substr(opts.showChar, content.length - opts.showChar);
	 
	            var html = null;
	            
	            html = c + '<span class="endText">' + opts.endText+ '&nbsp;</span><span class="hiddenText" style="display: none;">' + h + '</span>';
	            if (opts.mode == 'link') {
	            	html = html + '&nbsp<a href="#" class="morelink">' + opts.moretext + '</a>';
	        	}
	            
	            $(this).html(html);
	        }
		 
	        if (opts.mode == 'link') {
	        	$this.find('.morelink').on('click', function(){
			        if($(this).hasClass('less')) {
			            $(this).removeClass('less');
			            $(this).html(opts.moretext);
			            
			            if (typeof opts.lessCallback === 'function') opts.lessCallback.call(this);
			        } else {
			            $(this).addClass('less');
			            $(this).html(opts.lesstext);
			            
			            if (typeof opts.moreCallback === 'function') opts.moreCallback.call(this);
			        }
			        $this.find('.endText').toggle();
			        
			        var hiddenText = $this.find('.hiddenText');
			        
			        if (opts.toggleStyle) {
			        	if (opts.toggleStyle == 'fade') {
			        		hiddenText.fadeToggle('slow', 'linear');
			        	} else if (opts.toggleStyle == 'slide') {
			        		hiddenText.slideToggle('slow');
			        	} else
			        		hiddenText.toggle(opts.toggleStyle);
			        } else {
			        	hiddenText.toggle();
			        }
			        
			        return false;
			    });
			   
	        } else if (opts.mode == 'hover') {
	        	$this.on('hover', function(){
	        		 var hiddenText = $(this).find('.hiddentText');
	        		 
	        		 hiddenText.prev().toggle();
	        		 if (opts.toggleStyle) {
	        			 if (opts.toggleStyle == 'fade') {
	        				 hiddenText.fadeToggle('slow', 'linear');
	        			 } else if (opts.toggleStyle == 'slide') {
	        				 hiddenText.slideToggle('slow');
	        			 } else {
	        				 hiddenText.toggle(opts.toggleStyle);
	        			 }
	        		 } else {
	        			 hiddenText.toggle();
	        		 }
	        		 
	        		 return false;
	        	 });
	        } else if (opts.mode == 'static') {
	        	// do nothing
	        }
		});
	};
	
	$.fn.shortenedText.defaults = {
		showChar: 40,
		maxWidth: 0,
		mode: 'link', // link, hover, static, max-width, show-first-word
		endText: '...', // if mode is link
		moretext: 'More', // if mode is link
		lesstext: 'Less', // if mode is link
		toggleStyle: 'fade', // available: null, slow, fade, slide
		moreCallback: $.noop,
		lessCallback: $.noop,
	};
})(jQuery);