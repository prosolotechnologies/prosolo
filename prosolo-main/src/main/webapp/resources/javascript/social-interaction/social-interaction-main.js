$(function () {
	// dependencies: [ 'social-interaction/social-interaction-graph' ]
	var root = document.getElementById("social-interaction");
	
	socialInteractionGraph.load({
		host: root.dataset.api,
		courseId : root.dataset.courseId,
		studentId : root.dataset.studentId,
		width : 1024,
		height : 768,
		selector : "#social-interaction #graph",
		charge : -60,
		distance : 150,
		clusterMain : "main",
		clusters : ["one", "two", "three", "four", "five", "six"],
		focusMain : {x: 0, y: 0},
		focusPoints : [
			{x: 500, y: 0},
			{x: 0, y: 500},
			{x: 500, y: 500}
		],
		relations : [
			{ lower: 0, upper: 33, type: "twofive" },
			{ lower: 33, upper: 66, type: "fivezero" },
			{ lower: 66, upper: 85, type: "sevenfive" },
			{ lower: 85, upper: 100, type: "onezerozero" }	
		]
	});

});
