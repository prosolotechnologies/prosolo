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
	var hoveredId;
	
	var width = 430;
	var height = 330;
	var widthLeftMargin = 60;
	var widthRightMargin = 50;
	var heightBottomMargin = 50;
	var heightTopMargin = 30;
	var svg = dimple.newSvg("#quadrantchart", width, height);
	
	var ch = new dimple.chart(svg, data);
	ch.setBounds(widthLeftMargin, heightTopMargin, width - widthLeftMargin - widthRightMargin,
			height - heightTopMargin - heightBottomMargin);
	var xAxis = ch.addMeasureAxis("x", "Complexity");
	var yAxis = ch.addMeasureAxis("y", "Time");

	xAxis.overrideMin = 0;
	xAxis.overrideMax = +10;

	yAxis.overrideMin = 0;
	yAxis.overrideMax = +10;

	xAxis.title = "";
	yAxis.title = "";

	// 1 is added as a last parameter to avoid different tooltip color for every
	// shape on a chart
	// because it's determined by the last parameter added here
	var series = ch.addSeries([ "id", "name", "Activity Complexity", "Time Spent Learning", "1" ],
			dimple.plot.custom);

	var iconWidth = 20;
	var iconHeight = 20;
	series.afterDraw = function(shp, d) {
		var shape = d3.select(shp);
		svg.append("image").attr("x",
				parseFloat(shape.attr("cx")) - iconWidth / 2).attr("y",
				parseFloat(shape.attr("cy")) - iconHeight / 2).attr(
				"xlink:href", context + "/resources/images/ico-activities.svg")
				.attr("width", iconWidth)
				.attr("height", iconHeight)
				.on('click', function(d, i) {
							// console.log(shape);
							// console.log(shape[0][0].id);
							var e = document.createEvent('UIEvents');
							e.initUIEvent('click', true, true, window, 1);
							shape.node().dispatchEvent(e);
				})
				.on('mouseover', function(d, i) {
					
							// console.log(shape);
							// console.log(shape[0][0].id);
							var e = document.createEvent('UIEvents');
							e.initUIEvent('mouseover', true, true, window, 1);
							shape.node().dispatchEvent(e);
				})
				.on('mouseleave', function(d, i) {
					
							// console.log(shape);
							// console.log(shape[0][0].id);
							var e = document.createEvent('UIEvents');
							e.initUIEvent('mouseleave', true, true, window, 1);
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

	svg.append("line")
    .attr("x1", xAxis._scale(5))
    .attr("x2", xAxis._scale(5))
    .attr("y1", ch._yPixels())
    .attr("y2", ch._yPixels() + ch._heightPixels())
    .style("stroke", "#000")
    .style("stroke", "3")
    .moveToBack();
	
	svg.append("line")
    .attr("x1", ch._xPixels())
    .attr("x2", ch._xPixels() + ch._widthPixels())
    .attr("y1", yAxis._scale(5))
    .attr("y2", yAxis._scale(5))
    .style("stroke", "#000")
    .style("stroke", "3")
    .moveToBack();
	
	/*svg.append("text")      
    .attr("x", width - widthRightMargin - 25 )
    .attr("y", height - heightBottomMargin + 35 )
    .style("text-anchor", "middle")
    .text("Complexity");
	
	svg.append("text")      
    .attr("x", widthLeftMargin - 15 )
    .attr("y", heightTopMargin - 10 )
    .style("text-anchor", "middle")
    .text("Time needed");*/
	
	svg.append("text")      
    .attr("x", (width - widthLeftMargin - widthRightMargin)/2 + widthLeftMargin)
    .attr("y", heightTopMargin - 10 )
    .style("text-anchor", "middle")
    .text("A lot of time");
	
	svg.append("text")      
    .attr("x", (width - widthLeftMargin - widthRightMargin)/2 + widthLeftMargin)
    .attr("y", height - heightBottomMargin + 35 )
    .style("text-anchor", "middle")
    .text("Little time");
	
	svg.append("text")      
    .attr("x", widthLeftMargin - 35 )
    .attr("y", (height - heightTopMargin - heightBottomMargin)/2 + heightTopMargin )
    .style("text-anchor", "middle")
    .text("Simple");
	
	svg.append("text")      
    .attr("x", width - widthRightMargin + 25 )
    .attr("y", (height - heightTopMargin - heightBottomMargin)/2 + heightTopMargin )
    .style("text-anchor", "middle")
    .text("Complex");

	// var circles = svg.selectAll("circle");

	// circles.attr("r", 10);
	// console.log(circles);

	// "mouseover" or "click"
	series.shapes.on("mouseover", function(e) {
		console.log(e);
		
		hoveredId = e.aggField[0];
		dimple._showPointTooltip(e, this, ch, series);
		$("#activity"+hoveredId).toggleClass("bgRed");
	});
	series.shapes.on("click", function(e) {
		console.log(e);
		$("#act" + selectedId).toggle();
		if(selectedId == e.aggField[0]) {
			//dimple._removeTooltip(e,this,ch,series);
			selectedId = null;
		} else {
			selectedId = e.aggField[0];
			$("#act" + selectedId).toggle();
			//dimple._showPointTooltip(e, this, ch, series);
		}
	});

	
	  series.shapes.on("mouseleave", function (e) {
		  //$("#act"+selectedId).toggle(); 
		  dimple._removeTooltip(e,this,ch,series);
		  $("#activity"+hoveredId).toggleClass("bgRed");
		  hoveredId = null;
	  });
	
	
	

};

d3.selection.prototype.moveToBack = function() { 
    return this.each(function() { 
        var firstChild = this.parentNode.firstChild; 
        if (firstChild) { 
            this.parentNode.insertBefore(this, firstChild); 
        } 
    }); 
};

