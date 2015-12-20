define(["jquery"], function($) {
	return {
		create : function(configuration) {
			var charts = [];
	
			function container() {
				return document.querySelector(configuration.container);
			}
	
			function destroy() {
				container().innerHTML = "";
				charts.map(function(chart) {
					chart.destroy();
				});
				charts = [];
			}
			
			function renderLegendTo(configuration) {
				
				var gen = 40;
				var data = configuration.data();
				data.map(function(e) { e.y = gen; gen += 20; });
				
				d3.select(configuration.selector).selectAll("svg").remove();
				var paths = d3.select(configuration.selector).append("svg")
	        				  .selectAll("path")
	        				  .data(data)
	        				  .enter();
				
				d3.select(configuration.selector).selectAll("svg")
				  .append("text")
				  .text("Click on legend item to show/hide statistic.")
				  .attr("x", "0")
				  .attr("y", "20");
				
				var node = paths.append("g").attr("class", function(d) { return d["class"]; });
				
				node.append("svg:path")
					 .attr("d", function(d) { return "M0 " + d.y + " l36 0"; })
					 .attr("class", function(d) { return d["class"]; });
	        				
				node.append("text")
					 .text(function(d) { return d.name; })
					 .attr("x", "50")
					 .attr("y", function(d) { return d.y + 4; })
					 .attr("class", function(d) { return d["class"]; });
			}
	
			return {
				show : function(data, from, to) {
					destroy();
					var chart = new tauCharts.Chart({
						guide : {
							x : { 
								label : { padding : 33 },
								autoScale: false,
								min: from,
								max: to
							},
							y : {
								label : { padding : 33 }
							},
							color:{
				                brewer: configuration.brewer(data)
				            }
						},
						data : data,
						type : 'line',
						x : configuration.x,
						y : configuration.y,
						color : configuration.color,
						plugins : [ 
				            tauCharts.api.plugins.get('tooltip')({
				            	fields : configuration.tooltip.fields
				            })		            ]
					});
					chart.renderTo(configuration.container);
					renderLegendTo(configuration.legend);
					charts.push(chart);
				}
			};
		}
	};
});