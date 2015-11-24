/* Observation panel */
$('#observationBar').on({
    "shown.bs.dropdown": function() { this.closable = false; },
    "click":             function() { this.closable = false; },
    "hide.bs.dropdown":  function() { return this.closable; }
});

$('.slick-prev').click(function(){
	drawTreePath();
});

$('.slick-next').click(function(){
	drawTreePath();
});

$('#credentials').on('afterChange', function(event, slick, currentSlide){
	  drawTreePath();
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

function showHistory() {
	$('#learnTabs').hide();
	$('#obsHistory').show();
}

function hideHistory() {
	$('#obsHistory').hide();
	$('#learnTabs').show();
}

$(function() {
	drawTreePath();
})