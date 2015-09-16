(function($) {
	var ProsoloSearchPlugin = function (element, options) {
		// Merge options with defaults
		var opts = $.extend({}, $.fn.prosolosearch.defaults, options || {});
		var obj = this;
		
		var init = function() {
			$(opts.searchInput).attr('placeholder', opts.watermark);
			
			if (opts.hideSearchResultsOnOutsideClick) {
				//add a listener to document to hide search results
				$(document).on('click', function(e){
					$(opts.resultContainer).fadeOut();
				});
			}	
			
			//add a listener to search result container to prevent bubling click event to the document
			$(opts.resultContainer).on('click', function(e){
				e.stopPropagation();
			});
			
			$(opts.searchInput).on('click', function(){
				$(this).select();
			}).on('keyup', function(){
				obj.lookup(this.value);
			});
		};
		
		this.lookup = function(inputString) {
			obj.addLoader();
			if (inputString.length == 0) {
				$(opts.resultContainer).fadeOut();
			} else {
				obj.clearTimeout();
				opts.timeoutObj = setTimeout(function(){
					if (opts.searchAction)
						opts.searchAction();
				}, opts.timeout);
				
				$(opts.resultContainer).fadeIn();
			}
		};
		
		this.lookupInput = function() {
			this.lookup($(opts.searchInput).val());
		};
		
		this.addLoader = function() {
			$(opts.loaderContainer).css('text-align','center').html('<img class="loader" src="'+opts.loaderImage+'"/>');
		};
		
		this.clearTimeout = function() {
			clearTimeout(opts.timeoutObj);
		};
		
		this.resetSearch = function() {
			$(opts.searchInput).val('');
			this.hideResults();
		};
		
		this.showResults = function() {
			$(opts.resultContainer).fadeIn();
		};
		
		this.hideResults = function() {
			$(opts.resultContainer).fadeOut();
		};
		
		// actual code
		init();
   };
	
	$.fn.prosolosearch = function(options) {

		return this.each(function() {
			var element = $(this);
	          
			// Return early if this element already has a plugin instance
			if (element.data('prosolo.search')) return;
			
			// pass options to plugin constructor
			var prosoloSearchPlugin = new ProsoloSearchPlugin(this, options);
			
			// Store plugin object in this element's data
			element.data('prosolo.search', prosoloSearchPlugin);
		});
	};
	
	$.fn.prosolosearch.defaults = {
		resultContainer: null,
		loaderContainer: null,
		searchInput: null,
		seeAllLinkClass: null,
		watermark: 'Search',
		loaderImage: 'resources/images/style/ajax-loader-white.gif',
		searchAction: null,
		timeout: 200,
		hideSearchResultsOnOutsideClick: true,
	};
})(jQuery);
