$(function () {
	// dependencies: [ 'social-interaction/social-interaction-graph' ]
	var root = document.getElementById("social-interaction");
	
	socialInteractionGraph.load({
		host: root.dataset.api,
		courseId : root.dataset.courseId,
		studentId : root.dataset.studentId,
		width : 800,
		height : 500,
		selector : "#social-interaction #graph",
		charge : -75,
		distance : 150
	});
});