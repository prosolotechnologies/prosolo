var socialInteractionGraph = (function () {

	function readClusterInteractions(config) {
		return $.ajax({
			url : config.host + "/social/interactions/cluster",
			data : {"studentId" : config.studentId, "courseId" : config.courseId},
			type : "GET",
			crossDomain: true,
			dataType: 'json'
		});
	}

	function readOuterInteractions(config) {
		return $.ajax({
			url : config.host + "/social/interactions/outer",
			data : {"studentId" : config.studentId, "courseId" : config.courseId},
			type : "GET",
			crossDomain: true,
			dataType: 'json'
		});
	}

	function partition(items, size) {
		var result = _.groupBy(items, function(item, i) {
			return Math.floor(i/size);
		});
		return _.values(result);
	}

	function readStudentData(config, students) {
		return partition(students, 50).map(function(part) {
			return $.ajax({
				url : config.host + "/social/interactions/data",
				data : {"students" : part},
				type : "GET",
				crossDomain: true,
				dataType: 'json'
			});
		});
	}

	function run(config, clusterInteractions, outerInteractions, studentData) {

		var links = socialInteractionService.denormalize(clusterInteractions, outerInteractions);

		var students = links.filter(function(link) { return link.source.student == config.studentId; });
		if (students.length == 0) return;
		var mainCluster = students[0].source.cluster;

		function cluster(cluster) {
			return mainCluster == cluster ? config.clusterMain : config.clusters[cluster % config.clusters.length];
		}

		function foci(cluster) {
			return mainCluster == cluster ? config.focusMain : config.focusPoints[cluster % config.focusPoints.length]; 
		}
		
		var nodes = links.reduce(function(res, link) {
			res[link.source.student] = {
				name: link.source.student,
				cluster: link.source.cluster,
				clusterClass: cluster(link.source.cluster), 
				foci: foci(link.source.cluster)
			};
			res[link.target.student] = {
				name: link.target.student,
				cluster: link.target.cluster,
				clusterClass: cluster(link.target.cluster),
				foci: foci(link.target.cluster)				
			};
			return res;
		}, {});

		var positionNode = (function (width, height, length) {	
			var n = length;
			var m = Math.round(Math.sqrt(length));
			return function(node, index) {
				node.x = width / m * (index % m);
				node.y = height / m * Math.round(index / m);
			};
		})(width, height, nodes.length);

		function relations(links) {
			
			function type(value) {
				var found = config.relations.filter(function(type) {
					return value > type.lower && value <= type.upper;
				});
				return (found.length == 0) ? "" : found[0].type;
			}

			var maxCount = d3.max(links, function(d) {
				return d.count;
			});

			var v = d3.scale.linear().range([0, 100]).domain([0, maxCount]);
			var dv = d3.scale.linear().range([1.5, 0.5]).domain([0, maxCount]);
			
			return links.map(function(link) {
				return {
					source: nodes[link.source.student],
					target: nodes[link.target.student],
					value: link.count,
					type: type(v(link.count)),
					distanceFactor: dv(link.count), 
					cluster: link.source.cluster
				};
			});
		}
		
		var width = config.width,
			height = config.height;

		var d3nodes = d3.values(nodes);
		d3nodes.forEach(positionNode);
		var force = d3.layout.force()
			.nodes(d3nodes)
			.links(relations(links))
			.size([width, height])
			.linkDistance(function(d) { return config.distance * d.distanceFactor; })
			.charge(config.charge)
			.on("tick", tick)
			.start();

		/*var drag = force.drag()
			.on("dragstart", dragstart);*/

        var svg = d3.select(config.selector).append("svg")
			 .attr("width", width)
			 .attr("height", height)
			 .append("g")
				//.call(d3.behavior.zoom().scaleExtent([0.5, 4]).on("zoom", zoom));
			 .call(d3.behavior.zoom().scaleExtent([0.5, 2]).on("zoom", zoom));

		svg.on('mousedown.zoom',null);

		var svgdefs = svg.append("svg:defs");
		// build the arrow.
		svgdefs.selectAll("marker")
			.data(["end"]) // Different link/path types can be defined here
			.enter().append("svg:marker") // This section adds in the arrows
			.attr("id", String)
			.attr("viewBox", "0 -5 10 10")
			.attr("refX", 16)
			.attr("refY", 0)
			.attr("markerWidth", 13)
			.attr("markerHeight", 13)
			.attr("orient", "auto")
			.attr("markerUnits", "userSpaceOnUse")
			.append("svg:path")
			.attr("d", "M0,-5L10,0L0,5");

		svgdefs.append("svg:clipPath")
			.attr("id", "circle-clip")
			.append("svg:circle")
			.attr("r", "10")
			.attr("cx", "0")
			.attr("cy", "0");
		
		// add the links and the arrows
		var path = svg.append("svg:g").selectAll("path")
			.data(force.links())
			.enter().append("svg:path")
			.attr("class", function(d) {
				return "link " + d.type;
			}).attr("marker-end", "url(#end)");

		// define the nodes
		var node = svg.selectAll(".node")
			.data(force.nodes())
			.enter()
			.append("g")
			.on("click", function(d) {
				config.onNodeClick({
					id: d.name,
					cluster: d.cluster,
					name: studentData[d.name] ? studentData[d.name].name : "",
					avatar: studentData[d.name] ? studentData[d.name].avatar : ""
				});
			})
			.attr("class", "node")
			.call(drag);

		node.append("circle").attr("r", 10).attr("class", function(d) {
			return (d.name == config.studentId ? "focus " : "") + d.clusterClass;
		});

		node.append("image")
			.attr("xlink:href", function(d) {
				// TODO Default avatar?
				return studentData[d.name] ? studentData[d.name].avatar : "";
			})
			.attr("x", -14)
			.attr("y", -14)
			.attr("width", 28)
			.attr("height", 28)
			.attr("clip-path", "url(#circle-clip)");

		node.append("svg:title").text(function(d) { return studentData[d.name] ? studentData[d.name].name : ""; });
		
		d3.selectAll(".node image").attr("style", "display: none");

		function zoom() {
			if (d3.event.scale >= 3) {
				d3.selectAll(".node circle").attr("style", "display: none");
				d3.selectAll(".node image").attr("style", "display: block");
			} else {
				d3.selectAll(".node image").attr("style", "display: none");
				d3.selectAll(".node circle").attr("style", "display: block");
			};
			svg.attr("transform", "translate(" + d3.event.translate + ")scale(" + d3.event.scale + ")");
		}
		
		function tick(e) {
			path.attr("d", function(d) {
				var dx = d.target.x - d.source.x,
					dy = d.target.y - d.source.y,
					dr = Math.sqrt(dx * dx + dy * dy) * 2;
				return "M" + d.source.x + "," + d.source.y + "A" + dr + "," + dr + " 0 0,1 " + d.target.x + "," + d.target.y;
			});

			var k = .05 * e.alpha;
			d3nodes.forEach(function(o, i) {
				o.y += (o.foci.y - o.y) * k;
				o.x += (o.foci.x - o.x) * k;
			});
			
			node.attr("transform", function(d) {
				return "translate(" + d.x + "," + d.y + ")";
			});
		}

		function dragstart(d) {
			d3.select(this).classed("fixed", d.fixed = true);
		}

	}

	function parse(collection) {
		collection.forEach(function(item) {
			for(var i = 0; i < item.interactions.length; i++) {
				item.interactions[i] = JSON.parse(item.interactions[i]);
			}
		});
	}

	return {
		load: function (config) {
			$
				.when(
					readClusterInteractions(config),
					readOuterInteractions(config))
				.then(function(ci, oi) {
					parse(ci[0]);
					parse(oi[0]);
					if ((ci[0].length + oi[0].length) == 0) {
						$("#social-interaction").text(config.noResultsMessage);
						return;
					}

					var students = socialInteractionService.students(ci[0], oi[0]);
					$.when.apply($, readStudentData(config, students))
					 .then(function() {
						 var merge = {};
						 for(var i = 0; i < arguments.length; i++) {
							 $.extend(true, merge, arguments[i].responseJSON);
						 }
						 run(config, ci[0], oi[0], merge);
					 });
				}).fail(function() {
					$("#social-interaction").text(config.systemNotAvailableMessage);
				});
		}
	};
})();
