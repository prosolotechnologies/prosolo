$(function () {
	var root = document.getElementById("social-interaction");

	var graphWidth = $(".tab-content").width() / 12 * 9 - 50;
	
	socialInteractionGraph.load({
		host: root.dataset.api,
		courseId : root.dataset.courseId,
		studentId : root.dataset.studentId,
		width : graphWidth,
		height : 640,
		selector : "#social-interaction #graph",
		charge : -60,
		distance : 260,
		clusterMain : "main",
		clusters : ["one", "two", "three", "four", "five", "six"],
		focusMain : {x: $("#social .col-md-9").width() / 2, y: 320},
		focusPoints : [
			{x: 0, y: 0},
			{x: graphWidth, y: 0},
			{x: 0, y: 640},
			{x: graphWidth, y: 640}
		],
		relations : [
			{ lower: 0, upper: 33, type: "twofive" },
			{ lower: 33, upper: 66, type: "fivezero" },
			{ lower: 66, upper: 85, type: "sevenfive" },
			{ lower: 85, upper: 100, type: "onezerozero" }	
		],
		onNodeClick : function(student) {
			$("#social-interactions-selected-id").text(student.id);
			$("#social-interactions-selected-name").text(student.name);
			$("#social-interactions-selected-cluster").text(student.cluster);
			$("#social-interactions-selected-avatar").attr("src", student.avatar);
			$("#social-interactions-selected-avatar").show();
		},
		noResultsMessage: "No results found for given parameters.",
		systemNotAvailableMessage: "System is not available."
	});

});
