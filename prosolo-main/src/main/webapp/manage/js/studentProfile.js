/* Observation panel */
$('#observationBar').on({
    "shown.bs.dropdown": function() { this.closable = false; },
    "click":             function() { this.closable = false; },
    "hide.bs.dropdown":  function() { return this.closable; }
});

/* stick observation panel to the top */
$(window).bind('scroll', function() {
	 if ($(window).scrollTop() > 100) {
		 $('#observationBar .dropdown-menu').addClass('bar-fixed-top');
	 }
	 else {
		 $('#observationBar .dropdown-menu').removeClass('bar-fixed-top');
	 }
});

function showHistory() {
	$('#learnTabs').hide();
	$('#obsHistory').show();
}

function hideHistory() {
	$('#obsHistory').hide();
	$('#learnTabs').show();
}

function drawTreePath() {
	$('#svgContainer').HTMLSVGconnect({
		strokeWidth: 5,
		stroke: '#dddddd',
		paths: [
			{ start: '#credentials .slick-slide.selected', end: '#competencesBlock .slick-slide.selected'},
			{ start: '#competencesBlock .slick-slide.selected', end: '#activitiesBlock .col-md-6'}
        ]
	});
}

// Slider
function initCredentialsSlider() {
	$('#credentials .slides').slick({
		infinite : false,
		variableWidth: true,
		slidesToShow : 4,
		slidesToScroll : 2,
		prevArrow: '#credentials .sliderIconLeft',
		nextArrow: '#credentials .sliderIconRight',
		responsive: [
		             {
		            	 breakpoint: 1024,
		            	 settings: {
		            		 slidesToShow: 3,
		            		 slidesToScroll: 2
		            	 }
		             },
		             {
		            	 breakpoint: 600,
		            	 settings: {
		            		 slidesToShow: 2,
		            		 slidesToScroll: 1
		            	 }
		             },
		             {
		            	 breakpoint: 480,
		            	 settings: {
		            		 slidesToShow: 1,
		            		 slidesToScroll: 1
		            	 }
		             }
		     		]
	}).on('afterChange', function(event, slick, currentSlide, nextSlide){
		drawTreePath();
	});
}

function initCompetenceSlider() {
	$('#competencesBlock .slides').slick({
		infinite : false,
		variableWidth: true,
		slidesToShow : 4,
		slidesToScroll : 2,
		prevArrow: '#competencesBlock .sliderIconLeftCred',
		nextArrow: '#competencesBlock .sliderIconRightCred',
		responsive: [
		             {
		            	 breakpoint: 1024,
		            	 settings: {
		            		 slidesToShow: 3,
		            		 slidesToScroll: 2
		            	 }
		             },
		             {
		            	 breakpoint: 600,
		            	 settings: {
		            		 slidesToShow: 2,
		            		 slidesToScroll: 1
		            	 }
		             },
		             {
		            	 breakpoint: 480,
		            	 settings: {
		            		 slidesToShow: 1,
		            		 slidesToScroll: 1
		            	 }
		             }
		]
	}).on('afterChange', function(event, slick, currentSlide, nextSlide){
		drawTreePath();
	});
}

function showObservationPanel() {
	$('.observationBar.observationBox').show();
}

function hideObservationPanel() {
	$('.observationBar.observationBox').hide();
}

$(function() {
	drawTreePath();
	
	$("#activitiesBlock .panel.panel-default").on("mouseover", function(e) {
		var id = $(this).find('input[type="hidden"]').val();
		hoverActivity(id);
	});
})
