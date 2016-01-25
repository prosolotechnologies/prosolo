/* jQuery start */
$(function () {
	//-- call jRespond and add breakpoints
	var jRes = jRespond([
		{
			label: 'handheld',
			enter: 0,
			exit: 767
		},{
			label: 'tablet',
			enter: 768,
			exit: 991
		},{
			label: 'desktopSmall',
			enter: 992,
			exit: 1199
		},{
			label: 'desktopLarge',
			enter: 1200,
			exit: 10000
		}
	]);
	
	
	//-- initialize jPanelMenu 
	var jPM = $.jPanelMenu({
	    menu: '#mainMenu',
	    trigger: '#menu-trigger',
		duration: 100,
		closeOnContentClick: true
	});	
	
	
	// register enter and exit functions for multiple breakpoints and functions
	jRes.addFunc({
		breakpoint: 'desktopLarge',
		enter: function() {

		},
		exit: function() {

		}
	});		
	jRes.addFunc({
		breakpoint: 'desktopSmall',
		enter: function() {

		},
		exit: function() {

		}
	});	
	jRes.addFunc({
		breakpoint: 'tablet',
		enter: function() {

		},
		exit: function() {

		}
	});
	jRes.addFunc({
		breakpoint: 'handheld',
		enter: function() {
			jPM.on();
		},
		exit: function() {
			jPM.off();
		}
	});		

	// ADD SLIDEDOWN ANIMATION TO DROPDOWN /
   $('.dropdown').on('show.bs.dropdown', function(e){
   	$(this).find('.dropdown-menu').first().stop(true, true).slideDown(100);
		/* set focus on input field when search icon is clicked */
		$(".searchPanel input.form-control").focus();
		setTimeout(function(){
        $('.searchPanel input.form-control')[0].focus();
    	}, 1000);
  	});
	
	$('.search .dropdown-menu').click(function(e) {
		 e.stopPropagation();
	});

	// ADD SLIDEUP ANIMATION TO DROPDOWN //
	$('.dropdown').on('hide.bs.dropdown', function(e){
		$(this).find('.dropdown-menu').first().stop(true, true).slideUp(100);
	});


	// CLOSE SEARCH PANEL BUTTON
	$( ".closeSearch" ).click(function() {
	  $( ".searchPanel" ).first().stop(true, true).slideUp(100);
	  $(".dropdown.search").removeClass("open");
	});	


	/* Return Search icon if clicked outside
	$(document).click(function(event) { 
    if(!$(event.target).closest('.search a').length) {
		$(".search i").addClass("fa-search");
		$(".search i").removeClass("fa-close");	
    }        
	})*/
	
	/* show search results if something is typed in search field */
	$('.searchPanel input.form-control').on('keyup', function(event) {
		 $('.searchResults').first().stop(true, true).slideDown(10);
	});

	/* hide search results if search field is empty */
	$('.searchPanel input.form-control').on('keyup', function(event) {
		 if( $(this).val().length === 0 ) {
			  $('.searchResults').first().stop(true, true).slideUp(10);
		 }
	});

	/* tooltip initialization */
	$('[data-toggle="tooltip"]').tooltip();
});

/*  show and hide search results if something is typed in search field */
function showInstructorSearchResults() {
//	var length = $('.acField').val().length;
//	//alert(length);
//	if( (length > 0 && instructorSearchPreviousLength == 0) 
//			|| (length === 0 && instructorSearchPreviousLength != 0)) {
//		$('#instructorSearchResultsDropdown').dropdown("toggle");
//	}
//	instructorSearchPreviousLength = $('.acField').val().length;
	if( $('.acField').val().length === 0 ) {
		$('.acPanel').first().stop(true, true).slideUp(10);
	} else if($('.acField').val().length >= 1) {
		$('.acPanel').first().stop(true, true).slideDown(10);
	}
}

//added from custom.js
var context = '';

var custom = {
	setContext: function(ctx) {
		context = ctx;
	}
}

function addLoaderWithClass(div, message, withLoader, onlyRemoveContent, loaderClass){
	if (withLoader) {
		$(div).html('<div class="'+loaderClass+'">'+message+'&nbsp;&nbsp;<img src="'+context+'/resources/images/style/ajax-loader-black.gif"/></div>').show();
	} else if (onlyRemoveContent) {
		$(div).html('').show();
	}
}

function setQueryParamOfUri(uri, key, value) {
	var re = new RegExp("([?&])" + key + "=.*?(&|$)", "i");
	var separator = uri.indexOf('?') !== -1 ? "&" : "?";
	if (uri.match(re)) {
		return uri.replace(re, '$1' + key + "=" + value + '$2');
	} else {
		return uri + separator + key + "=" + value;
	}
}