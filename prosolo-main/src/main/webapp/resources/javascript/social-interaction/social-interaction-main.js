require([ 'social-interaction/social-interaction-graph' ], function(graph) {
	var root = document.getElementById("social-interaction");

	graph.load({
		host: root.dataset.api,
		studentId : root.dataset.studentId,
		width : 800,
		height : 400,
		links : 100,
		selector : "#social-interaction #graph",
		charge : -300,
		distance : 50
	});
});
