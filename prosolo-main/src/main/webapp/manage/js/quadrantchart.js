window.onload = function() {
     var selectedId;
	 var data = [
	             {
	               id: 12,
	               x:2, 
	               y:-2.5,
	               type:"completed"
	             }, {
	               id: 13,
	               x:2, 
	               y:-1.5,
	               type:"not completed"
	             }, {
	               id: 16,
	               x:5, 
	               y:2.5,
	               type:"completed"
	             }, {
	               id: 18,
	               x:-10, 
	               y:3.5,
	               type:"completed"
	             },
	             {
	               id: 2,
		           x: 5,
		           y: -2,
		           type: "not completed"
		         },
		         {
		           id: 3,
		           x: -4,
		           y: -6,
		           type: "completed"
		         }
	           ];
	           
  
      var svg = dimple.newSvg("#quadrantchart", 390, 330);
    
      var ch = new dimple.chart(svg, data);
      ch.setBounds(50, 30, 330, 250);
      var xAxis = ch.addMeasureAxis("x", "x");
      var yAxis = ch.addMeasureAxis("y", "y");
      
      xAxis.overrideMin = -10;  
      xAxis.overrideMax = +10;  
      
      yAxis.overrideMin = -10;  
      yAxis.overrideMax = +10;  
      
      xAxis.title = "Time needed";
      yAxis.title = "Complexity";
      
      var series = ch.addSeries(["id", "x", "type"], dimple.plot.bubble);
     
      series.addEventHandler("mouseover", function(e){
    	 
      });
      series.addEventHandler("mouseleave", function(e){
    	  
      });
      
     /* series.getTooltipText = function (e) {
          return [
				"Stefn"
			]};*/
     
   // Override the standard tooltip behaviour
      
      
      ch.addLegend(40, 10, 360, 10, "left");
    	
      ch.draw();
      
      var circles = svg.selectAll("circle");
      console.log(circles);
      
      circles.attr('class', "hexagon done");
      
      
      series.shapes.on("mouseover", function (e) {
    	  console.log(e);
    	  $("#act"+selectedId).toggle();
    	  selectedId = e.aggField[0];
    	  $("#act"+selectedId).toggle();
    	  dimple._showPointTooltip(e, this, ch, series);
      });
      
     /* series.shapes.on("mouseleave", function (e) {
    	  //$("#act"+selectedId).toggle();
    	  //selectedId = null;
    	  //dimple._removeTooltip(e,this,ch,series);
     });*/
      
};


