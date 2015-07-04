$(document).ready(function() {
	
	$('.ballonMessage').on( {
        mouseenter:
            function() {
        		$(this).find('.hide').fadeIn();
            },
        mouseleave:
            function() {
        	 	$(this).find('.hide').fadeOut(600);
            },
        pulsate : 
        	function() {
	        	if ($(this).css('display') != 'none')
	        		$(this).effect( 'pulsate', 500);
        	}
	});
	
	$('.ballonMessage .hide').on('click', function() {
		$($(this).parents('.ballonMessage').parent()).fadeOut();
	});
	
});