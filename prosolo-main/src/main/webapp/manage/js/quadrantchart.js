function drawQuadrantChart(servicePath) {
	$.ajax({
		url : servicePath
	}).then(function(data) {
		draw(data);
	});
	
	/*
	 * var data = [ { id: 12, Complexity:2, Time:-2.5, name:"Statistical
	 * analysis 1" }, { id: 13, Complexity:2, Time:-1.5, name:"Statistical
	 * analysis 2" }, { id: 16, Complexity:5, Time:2.5, name:"Statistical
	 * analysis 3" }, { id: 18, Complexity:-10, Time:3.5, name:"Statistical
	 * analysis 4" }, { id: 2, Complexity: 5, Time: -2, name: "Statistical
	 * analysis 5" }, { id: 3, Complexity: -4, Time: -6, name: "Statistical
	 * analysis 6" } ];
	 */

	
	
};

function draw(data) {
	var selectedId;
	var svg = dimple.newSvg("#quadrantchart", 390, 330);

	var ch = new dimple.chart(svg, data);
	ch.setBounds(50, 30, 330, 250);
	var xAxis = ch.addMeasureAxis("x", "Complexity");
	var yAxis = ch.addMeasureAxis("y", "Time");

	xAxis.overrideMin = -10;
	xAxis.overrideMax = +10;

	yAxis.overrideMin = -10;
	yAxis.overrideMax = +10;

	xAxis.title = "Time needed";
	yAxis.title = "Complexity";

	// 1 is added as a last parameter to avoid different tooltip color for every
	// shape on a chart
	// because it's determined by the last parameter added here
	var series = ch.addSeries([ "id", "name", "Complexity", "Time", "1" ],
			dimple.plot.custom);

	var iconWidth = 20;
	var iconHeight = 20;
	series.afterDraw = function(shp, d) {
		var shape = d3.select(shp);
		svg.append("image").attr("x",
				parseFloat(shape.attr("cx")) - iconWidth / 2).attr("y",
				parseFloat(shape.attr("cy")) - iconHeight / 2).attr(
				"xlink:href", context + "/resources/images/ico-activities.svg")
				.attr("width", iconWidth).attr("height", iconHeight).on(
						'click', function(d, i) {
							// console.log(shape);
							// console.log(shape[0][0].id);
							var e = document.createEvent('UIEvents');
							e.initUIEvent('click', true, true, window, 1);
							shape.node().dispatchEvent(e);
						});
	};

	series.addEventHandler("mouseover", function(e) {

	});
	series.addEventHandler("mouseleave", function(e) {

	});

	series.getTooltipText = function(e) {
		return [ e.aggField[1], "\n", "Complexity: " + e.aggField[2],
				"Time :" + e.aggField[3] ]
	};

	// Override the standard tooltip behaviour

	// for showing legend based on last parameter added in ch.addSeries
	// ch.addLegend(40, 10, 360, 10, "left");

	ch.draw();

	// var circles = svg.selectAll("circle");

	// circles.attr("r", 10);
	// console.log(circles);

	// "mouseover" or "click"
	series.shapes.on("click", function(e) {
		console.log(e);
		$("#act" + selectedId).toggle();
		if(selectedId == e.aggField[0]) {
			dimple._removeTooltip(e,this,ch,series);
			selectedId = 0;
		} else {
			selectedId = e.aggField[0];
			$("#act" + selectedId).toggle();
			dimple._showPointTooltip(e, this, ch, series);
		}
	});

	/*
	 * series.shapes.on("mouseleave", function (e) {
	 * //$("#act"+selectedId).toggle(); //selectedId = null;
	 * //dimple._removeTooltip(e,this,ch,series); });
	 */

};

