require([ 'social-interaction/social-interaction-graph' ], function(graph) {
	var root = document.getElementById("social-interaction");

	graph.load({
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
