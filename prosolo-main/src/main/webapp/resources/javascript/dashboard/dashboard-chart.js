var chart = (function() {

	var patterns = ["pattern-one", "pattern-two", "pattern-three", "pattern-four", "pattern-five", "pattern-six"];

	function destroy(container, charts) {
		container.innerHTML = "";
		charts.forEach(function(chart) { chart.destroy(); });
		charts = [];		
	}

	function setPositions(objects, config) {
		objects.reduce(function(y, e) { e.y = y;  return y + config.step; }, config.start);
	}

	function setPatterns(objects, patterns) {
		objects.reduce(function(index, e) { e.pattern = patterns[index];  return index + 1; }, 0);
	}

	function renderLegendTo(selector, data) {
		document.querySelector(selector).innerHTML = "";

		setPositions(data, {start: 40, step: 20});
		setPatterns(data, patterns);
		
		var svg = d3.select(selector).append("svg");

		svg.append("text")
			.text("Click on legend item to show/hide statistic.")
			.attr("x", "0")
			.attr("y", "20");

		var paths = svg.selectAll("path")
			.data(data)
			.enter();
		
		var node = paths.append("g").attr("class", function(d) { return d["class"]; });
		
		node.append("svg:path")
			.attr("d", function(d) { return "M0 " + d.y + " l36 0"; })
			.attr("class", function(d) { return d["pattern"]; });
		
		node.append("text")
			.text(function(d) { return d.name; })
			.attr("x", "50")
			.attr("y", function(d) { return d.y + 4; })
			.attr("class", function(d) { return d["class"]; });
	}

	function displayLines(container, legend) {
		patterns.forEach(function(pattern) {
			$(container + " g." + pattern).each(function() {
				this.classList.remove(pattern);
			});
		});
		$(legend + " svg > g").each(function() {
			var elementClass = $(this).attr("class");
			var pattern = $(this).children("path").attr("class");
			$(container + " g." + elementClass).each(function() {
				this.classList.add(pattern);
			});
		});
	}

	return {
		create : function(configuration) {
			var charts = [];
			var container = document.querySelector(configuration.container);

			return {
				show : function(data, from, to) {
					destroy(container, charts);
					var chart = new tauCharts.Chart({
						autoResize : false,
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
							})					]
					});
					chart.renderTo(configuration.container);
					charts.push(chart);
				},
				renderLegend : function(data) {
					renderLegendTo(configuration.legend.selector, data);
					displayLines(configuration.container, configuration.legend.selector);
					$(configuration.legend.selector + " g").click(function() {
						var $g = $(configuration.container + " g." + $(this).attr("class"));
						if ($g.size() == 0) {
							return;
						}
						if ($g.css("opacity") == "0") {
							$g.get(0).classList.add($(this).children("path").attr("class"));
							$g.css("opacity", "100");
							$(this).children("text").get(0).classList.remove("selected");
						} else {
							$g.removeClass(patterns);
							$g.css("opacity", "0");
							$(this).children("text").get(0).classList.add("selected");
						}
					});
				}
			};
		}
	};
})();