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
	$("#svgContainer").HTMLSVGconnect({
		strokeWidth: 5,
		stroke: "#dddddd",
		paths: [
			{ start: "#credentials .selected", end: "#competences .selected"},
			{ start: "#competences .selected", end: "#activitiesBlock .col-md-6"}
        ]
	});
}

// Slider
function initCredentialsSlider() {
	$('#credentials .slides').slick({
		infinite : false,
		slidesToShow : 3,
		slidesToScroll : 2,
		prevArrow: '#credentials .sliderIconLeft',
		nextArrow: '#credentials .sliderIconRight'
	});
}

function initCompetenceSlider() {
	$('#competences .slides').slick({
		infinite : false,
		slidesToShow : 3,
		slidesToScroll : 2,
		prevArrow: '#competences .sliderIconLeft',
		nextArrow: '#competences .sliderIconRight'
	});
}

function initProgressionTabScripts() {
	initCredentialsSlider();
	initCompetenceSlider();
	drawTreePath();
}

function showObservationPanel() {
	$('.observationBar.observationBox').show();
}

function hideObservationPanel() {
	$('.observationBar.observationBox').hide();
}

$(function() {
	initProgressionTabScripts();
})