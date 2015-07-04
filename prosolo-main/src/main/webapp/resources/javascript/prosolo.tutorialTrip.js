var prosolo = $.extend({}, prosolo, prosolo || {});

prosolo.tutorialTrip = {
	createTutorial : function (text, page){
		var steps = String(text).split('|');
		var stepsData = [];
		
		$.each(steps, function(i,step) {
			var sel = null;
			var elem = $('.'+page+'TripStep'+(i+1));
			var position = elem.attr('position');
			
			if (!position) {
				position = 'n';
			}
			
			var content = elem.attr('title');
			if (!content) {
				sel = elem;
			}
			
			stepsData.push({
				sel : $('.'+page+'TripStep'+(i+1)),
				position: position,
				content : step, 
				expose : false,
			});
		});
	
		return new Trip( stepsData, {
			showNavigation : true,
		    showCloseBox : true,
		    delay : -1,
		    tripTheme: 'black',
		    backToTopWhenEnded: true,
		    enableAnimation: true,
		    showCloseBox: true,
		    showNavigation: true,
		    finishLabel: 'Finish',
		});
	}
};