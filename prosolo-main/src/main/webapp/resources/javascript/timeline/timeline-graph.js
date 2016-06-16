var timelineGraph = (function () {
	
	Date.prototype.ddmmyyyy = function() {
		var yyyy = this.getFullYear().toString();
		var mm = (this.getMonth()+1).toString(); 
		var dd  = this.getDate().toString();
		return (dd[1]?dd:"0"+dd[0]) + "." + (mm[1]?mm:"0"+mm[0]) + "." + yyyy; 
	};
	 /* As described in http://bl.ocks.org/eesur/4e0a69d57d3bfc8a82c2
	  */
	 d3.selection.prototype.timelineTooltipMoveToFront = function() {  
	      return this.each(function(){
	        this.parentNode.parentNode.appendChild(this.parentNode);
	      });
	    };
	 d3.selection.prototype.timelineTooltipMoveToBack = function() {  
	        return this.each(function() { 
	            var firstChild = this.parentNode.firstChild; 
	            if (firstChild) { 
	                this.parentNode.insertBefore(this, firstChild); 
	            } 
	        });
	    };
	
	function getTimelineData(config) {
		var element = config.element;
		
		var margin = {top: 10, right: 10, bottom: 100, left: 40},
	    margin2 = {top: 220, right: 10, bottom: 20, left: 40},
	    width = 860 - margin.left - margin.right,
	    tooltipMainRectHeight = 45,
	    tooltipTitleRectHeight = 15,
	    tooltipWidth = 120,
	    height = 300 - margin.top - margin.bottom,
	    height2 = 300 - margin2.top - margin2.bottom,
		totalDaysZoomTreshold = (typeof config.totalDaysZoomTreshold === 'undefined') 
			? 30 : config.totalDaysZoomTreshold,
		daysToZoom = (typeof config.daysToZoom === 'undefined') 
			? 30 : config.daysToZoom;

		var parseDate = d3.time.format("%Y-%m-%d").parse;
	
		var x = d3.time.scale().range([0, width]),
		    x2 = d3.time.scale().range([0, width]),
		    y = d3.scale.linear().range([height, 0]),
		    y2 = d3.scale.linear().range([height2, 0]);
	
		var xAxis = d3.svg.axis().scale(x).orient("bottom"),
		    xAxis2 = d3.svg.axis().scale(x2).orient("bottom"),
		    yAxis = d3.svg.axis().scale(y).orient("left");
	
		var brush = d3.svg.brush()
		    .x(x2)
		    .on("brush", brushed);
	
		var line1 = d3.svg.line()
		    .x(function(d) { return x(d.date); })
		    .y(function(d) { return y(d.value); });
	
		var line2 = d3.svg.line()
		    .x(function(d) { return x2(d.date); })
		    .y(function(d) { return y2(d.value); });
	
		var svg = d3.select("#"+element.id).append("svg")
		    .attr("width", width + margin.left + margin.right)
		    .attr("height", height + margin.top + margin.bottom);
		
		var defs = svg.append("defs");
		
		defs.append("clipPath")
		    .attr("id", "clip")
		    .append("rect")
		    .attr("width", width)
		    .attr("height", height);
		// create filter with id #drop-shadow
		// height=130% so that the shadow is not clipped
		var filter = defs.append("filter")
		    .attr("id", "drop-shadow")
		    .attr("height", "115%");

		// SourceAlpha refers to opacity of graphic that this filter will be applied to
		// convolve that with a Gaussian with standard deviation 3 and store result
		// in blur
		filter.append("feGaussianBlur")
		    .attr("in", "SourceAlpha")
		    .attr("stdDeviation", 3)
		    .attr("result", "blur");

		// translate output of Gaussian blur to the right and downwards with 2px
		// store result in offsetBlur
		filter.append("feOffset")
		    .attr("in", "blur")
		    .attr("dx", 1)
		    .attr("dy", -5)
		    .attr("result", "offsetBlur");

		// overlay original SourceGraphic over translated blurred opacity by using
		// feMerge filter. Order of specifying inputs is important!
		var feMerge = filter.append("feMerge");

		feMerge.append("feMergeNode")
		    .attr("in", "offsetBlur")
		feMerge.append("feMergeNode")
		    .attr("in", "SourceGraphic");
	
		var focus = svg.append("g")
		    .attr("class", "focus")
		    .attr("transform", "translate(" + margin.left + "," + margin.top + ")");
	
		var context = svg.append("g")
		    .attr("class", "context")
		    .attr("transform", "translate(" + margin2.left + "," + margin2.top + ")");
		
		var lineGroup = focus.append("g");
		lineGroup.attr("clip-path", "url(#clip)");
		
		var tooltipGroup = focus.append("g");
		tooltipGroup.attr("clip-path", "url(#clip)");
		
		var milestoneDotGroup = context.append("g");
		
		var dateTo = new Date().ddmmyyyy()+". UTC";
		var dateFrom = new Date(new Date().setDate(new Date().getDate()-30)).ddmmyyyy() + ". UTC"
	
		d3.json("http://" + config.apiHost + "/learning/activity/student/" + config.studentId + "?dateFrom="+dateFrom+"&dateTo="+dateTo, function(error, data) {
			data = data.sort(function(a,b){
				  return new Date(b.date) - new Date(a.date);
			})
			.filter(function(item, pos, ary) {
		        return !pos || item.date != ary[pos - 1].date;
		    })
		    .map(function(value,index,array){
		    	value.date = parseDate(value.date);
		    	return value;
		    });
			
		  x.domain(d3.extent(data.map(function(d) { 
			  return d.date; 
			  })));
		  var maximumValue = d3.max(data.map(function(d) { return d.value; }));
		  y.domain([0, maximumValue*1.5]);
		  x2.domain(x.domain());
		  y2.domain(y.domain());
	
		  focus.append("path")
		      .datum(data, function(d) { return d.date; })
		      .attr("class", "timeline-line")
		      .attr("clip-path", "url(#clip)")
		      .attr("d", line1);
	
		  focus.append("g")
		      .attr("class", "x timeline-axis")
		      .attr("transform", "translate(0," + height + ")")
		      .call(xAxis);
	
		  focus.append("g")
		      .attr("class", "y timeline-axis")
		      .call(yAxis);
	
		  context.append("path")
		      .datum(data, function(d) { return d.date; })
		      .attr("class", "timeline-line")
		      .attr("d", line2);
	
		  context.append("g")
		      .attr("class", "x timeline-axis")
		      .attr("transform", "translate(0," + height2 + ")")
		      .call(xAxis2);
	
		  context.append("g")
		      .attr("class", "x timeline-brush")
		      .call(brush)
		    .selectAll("rect")
		      .attr("y", -6)
		      .attr("height", height2 + 7);
		  
		 
		 var singleTooltipGroup = tooltipGroup.selectAll("g")                                   
	        .data(data.filter(function(d) { return d.milestones; }), function(d) { return d.date; })                                            
		  	.enter()
		  	.append("g")
		  	.attr("event-type",function(d){return d.milestones[0].type})
		  	
		  
		  //create main text rectangle			
		  singleTooltipGroup.append("rect")
		  			.attr("class","timeline-tooltip")
					.attr("x", function(d) { return x(d.date) - (tooltipWidth/2) })
				  	.attr("y", function(d) { return tooltipTitleRectHeight  })       
				  	.attr("width", tooltipWidth)
				  	.attr("height", tooltipMainRectHeight)
				  	.attr("tooltip-index", function (d,i) { return i })
				  	.attr("id",function(d,i){return "tooltip-"+i+"-"+d.value})
				  	.style("stroke",function(d){
				  		return eventColor(d.milestones[0].type)
				  		})
				  	.on('mouseover', function(d) {
				  		d3.select(this).timelineTooltipMoveToFront();
				  		})
				  	.on('mouseout', function(d) {
				  		d3.select(this).timelineTooltipMoveToBack();
				  	});
		 //create title rectangle
		 singleTooltipGroup.append("rect")
			.attr("class","timeline-tooltip-title")
			.attr("x", function(d) { return x(d.date) - (tooltipWidth/2) })
		  	.attr("y", function(d) { return 0  })     
		  	.attr("width", tooltipWidth)
		  	.attr("height", tooltipTitleRectHeight)
		  	.attr("tooltip-index", function (d,i) { return i })
		  	.attr("id",function(d,i){return "tooltip-title-"+i+"-"+d.value})
		  	.style("fill",function(d){return eventColor(d.milestones[0].type)})
		  	.style("stroke",function(d){return eventColor(d.milestones[0].type)})
		  	.on('mouseover', function(d) {findTooltipTextRectangle(this.getAttribute("tooltip-index")).timelineTooltipMoveToFront()})
		  	.on('mouseout', function(d) {findTooltipTextRectangle(this.getAttribute("tooltip-index")).timelineTooltipMoveToBack()}) 
		  	
		  //create tooltop title text
		  singleTooltipGroup.append("text")
		  		.attr("class","timeline-tooltip-title-text")
		  		.attr("x", function(d) { return x(d.date) - (tooltipWidth/2) + 5 })
		  		.attr("y", function(d) { return 11  })  
		  		.text(function(d){return d.milestones[0].name})
		  //create description text and add it after rectangle
		  singleTooltipGroup.append("text")
			.attr("class","tooltip-text")
			.attr("x",function(d){return x(d.date) - (tooltipWidth/2) + 5})
			.attr("y",function(d){return (tooltipMainRectHeight/2 - 12 + tooltipTitleRectHeight)})
			.text(function(d){return d.milestones[0].description})
			.call(wrap)
		  //create link text
//		  singleTooltipGroup.append("a")
//		     .attr("xlink:href", function (d) { return d.milestones[0].link; })
//		  singleTooltipGroup.selectAll("a").append("text")
//		  		 .attr("class", "timeline-link")
//		  		 .text(function (d) { return d.milestones[0].linkText; })
//		  		 .attr("y", function(d){return (tooltipMainRectHeight + tooltipTitleRectHeight - 5)})
//		         .attr("x", function(d){return x(d.date) + tooltipWidth/4 - 5})
//		  singleTooltipGroup.selectAll("a").append("rect")
//		         .attr("class", "timeline-link-rect")
//		         .attr("y", function(d){return (tooltipMainRectHeight + tooltipTitleRectHeight - 15)})
//		         .attr("x", function(d){return x(d.date) + tooltipWidth/4 - 5})
//		         .attr("width", function(d){return d.milestones[0].link.length})
//		         .attr("height", 12)
//		         .style("fill-opacity", 0);
			
		  lineGroup.selectAll("line")
		    .data(data.filter(function(d) { 
	    	  return d.milestones; 
	    	  }), function(d) { return d.date; })
			    .enter()
			    .append("line")
			    .attr("class", function(d){
				    var lineClass;
				    if(d.milestones[0].type == "Credentials") 
				    	lineClass = "timeline-cred-ann-line"//"#0E9C57"
				    else if(d.milestones[0].type == "Competences")
				    	lineClass = "timeline-comp-ann-line"//"#428BCA"
				    else if(d.milestones[0].type == "Activities")
				    	lineClass = "timeline-acti-ann-line"//"#5BC0DE"
			    	return "timeline-milestone-line "+lineClass; 
			    })
			    .attr("event-type",function(d){return d.milestones[0].type})
			    .attr("tooltip-index", function (d,i) { return i })
			    .attr("x1", function (d) { return x(d.date); })
			    .attr("y1", function (d) { return 51; })
			    .attr("x2", function (d) { return x(d.date); })
			    .attr("y2", function (d) { return height; })
			    .on('mouseover', function(d) {
			    	findTooltipTextRectangle(this.getAttribute("tooltip-index")).timelineTooltipMoveToBack();
			  		})
			  	.on('mouseout', function(d) {
			  		findTooltipTextRectangle(this.getAttribute("tooltip-index")).timelineTooltipMoveToFront();
			  	});

		milestoneDotGroup.selectAll("circle").data(data.filter(function(d) { 
							return d.milestones; 
						}), function(d) { return d.date; })
					.enter()
					.append("circle")
					.attr("class", function(d){
						if(d.milestones[0].type == "Credentials") 
							return "timeline-cred-ann-dot"
						else if(d.milestones[0].type == "Competences")
							return "timeline-comp-ann-dot"
						else if(d.milestones[0].type == "Activities")
							return "timeline-acti-ann-dot"
					})
					.attr("cx",function(d){return x2(d.date);})
					.attr("cy",function(d){return y2(d.value);})
					.attr("event-type",function(d){ return d.milestones[0].type; })
					.attr("r",3);
					
		var dataExtendInDays = getDataExtendInDays(data);
		if(dataExtendInDays >= totalDaysZoomTreshold) {
			brush.extent([new Date(data[daysToZoom].date), new Date(data[0].date)]);
			context.select('.timeline-brush').call(brush);
			brushed();
		}
		});
		


		
		function brushed() {
			x.domain(brush.empty() ? x2.domain() : brush.extent());
			focus.select(".timeline-line").attr("d", line1);
			focus.select(".x.timeline-axis").call(xAxis);
			lineGroup.selectAll(".timeline-milestone-line")
				.attr("x1", function (d) { return x(d.date); })
			    .attr("y1", function (d) { return 51; })
			    .attr("x2", function (d) { return x(d.date); })
			    .attr("y2", function (d) { return height; })
			tooltipGroup.selectAll(".timeline-tooltip")
					.attr("x", function(d) { return x(d.date) - (tooltipWidth/2) })
				  	.attr("y", function(d) { return tooltipTitleRectHeight  })       
			tooltipGroup.selectAll(".timeline-tooltip-title")
					.attr("x", function(d) { return x(d.date) - (tooltipWidth/2) })
					.attr("y", function(d) { return 0  }) 
			tooltipGroup.selectAll(".tooltip-text")
								.attr("x",function(d){return x(d.date) - (tooltipWidth/2) + 5})
			.attr("y",function(d){return (tooltipMainRectHeight/2 - 12 + tooltipTitleRectHeight)})
					.text(function(d){return d.milestones[0].description})
					.call(wrap)
			tooltipGroup.selectAll(".tooltip-text")
					.attr("x",function(d){return x(d.date) - (tooltipWidth/2) + 5})
					.attr("y",function(d){return (tooltipMainRectHeight/2 + tooltipTitleRectHeight)})
			tooltipGroup.selectAll(".timeline-link")
			  		 .attr("y", function(d){return (tooltipMainRectHeight + tooltipTitleRectHeight - 5)})
			         .attr("x", function(d){return x(d.date) + tooltipWidth/4 - 5})
			 tooltipGroup.selectAll(".timeline-link-rect")
			         .attr("y", function(d){return (tooltipMainRectHeight + tooltipTitleRectHeight - 15)})
			         .attr("x", function(d){return x(d.date) + tooltipWidth/4 - 5})
			 tooltipGroup.selectAll(".timeline-tooltip-title-text")
		  		.attr("x", function(d) { return x(d.date) - (tooltipWidth/2) + 5 })
		  		.attr("y", function(d) { return 11  })  
		}

	}
	
	function findTooltipTextRectangle(index) {
		return d3.select("rect.timeline-tooltip[tooltip-index='"+index+"']")
	}
	
	function wrap(text, width) {
		  text.each(function() {
		    var text = d3.select(this),
		        words = text.text().split(/\s+/).reverse(),
		        word,
		        line = [],
		        lineNumber = 0,
		        lineHeight = 1.1, // ems
		        y = text.attr("y"),
		        dy = parseFloat(text.attr("dy")),
		        counter = 0,
		        tspan = text.text(null).append("tspan").attr("x", text.attr("x")).attr("y", y);
		    while (word = words.pop()) {
		      line.push(word);
		      tspan.text(line.join(" "));
		      if (line.length > 3) {
		        line.pop();
		        tspan.text(line.join(" "));
		        line = [word];
		        counter = counter + 10;
		        tspan = text.append("tspan").attr("x", text.attr("x")).attr("y", parseInt(y) + counter).text(word);
		      }
		    }
		  });
		}

	function eventColor(eventType) {
	    if(eventType == "Credentials") 
	    	return "#0E9C57"
	    else if(eventType == "Competences")
	    	return "#428BCA"
	    else if(eventType == "Activities")
	    	return  "#D9534F"
    	return "#D9534F"
	}
	
	function getDataExtendInDays(data) {
		if(data || data.length > 0) {
			var days = Math.round(new Date(data[0].date) - new Date(data[data.length-1].date))/(1000*60*60*24);
			return days;
		}
		return 0;
		
	}
	

	return {
		
		allEventTypes : ["Credentials","Competences","Activities"],
		
		load : function(config) {
			activeEvents = config.events
			getTimelineData(config)
		},
		
		recalculateMilestones : function(events) {
			//hide tooltips, given an array of "active" events
			var hiddenTooltips = $.grep(this.allEventTypes, function(el){return $.inArray(el, events) == -1});
			$.each(hiddenTooltips, function( index, value ) {
				 $("line[event-type="+value+"], g[event-type="+value+"], circle[event-type="+value+"]").fadeOut("fast")
			});
			//show tooltips, given a array of "active" events
			$.each(events, function( index, value ) {
					 $("line[event-type="+value+"], g[event-type="+value+"], circle[event-type="+value+"]").fadeIn("fast")
				});
				
		}
		
	};
})();