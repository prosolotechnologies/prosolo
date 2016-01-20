var socialInteractionGraph = (function () {
	var clusters = {
		"0": "one",
		"1": "two",
		"2": "three",
		"3": "four"
	};
	
	var foci = {
		"one" : {x: 0, y: 0},
		"two" : {x: 500, y: 0},
		"three" : {x: 0, y: 500},
		"four" : {x: 500, y: 500}
	};

	function readClusterInteractions(config) {
		return $.ajax({
			url : "http://" + config.host + "/api/social/interactions/cluster",
			data : {"studentId" : config.studentId, "courseId" : config.courseId},
			type : "GET",
			crossDomain: true,
			dataType: 'json'
		});
	}

	function readOuterInteractions(config) {
		return $.ajax({
			url : "http://" + config.host + "/api/social/interactions/outer",
			data : {"studentId" : config.studentId, "courseId" : config.courseId},
			type : "GET",
			crossDomain: true,
			dataType: 'json'
		});
	}
	
	function dofocus(user, cluster, config) {
		return user == config.studentId ? "focus " + cluster : "" + cluster;
	}

	function run(config, clusterInteractions, outerInteractions) {

		var links = socialInteractionService.denormalize(clusterInteractions, outerInteractions);
		
		var nodes = links.reduce(function(res, link) {
			res[link.source.student] = {
				name: link.source.student,
				cluster: clusters[link.source.cluster]
			};
			res[link.target.student] = {
				name: link.target.student,
				cluster: clusters[link.target.cluster]
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
			var types = [
				{ lower: 0, upper: 33, type: "twofive" },
				{ lower: 33, upper: 66, type: "fivezero" },
				{ lower: 66, upper: 85, type: "sevenfive" },
				{ lower: 85, upper: 100, type: "onezerozero" }
			];
			
			function type(value) {
				var found = types.filter(function(type) {
					return value > type.lower && value <= type.upper;
				});
				return (found.length == 0) ? "" : found[0].type;
			}

			var maxCount = d3.max(links, function(d) {
				return d.count;
			});

			var v = d3.scale.linear().range([0, 100]).domain([0, maxCount]);
			
			return links.map(function(link) {
				return {
					source: nodes[link.source.student],
					target: nodes[link.target.student],
					value: link.count,
					type: type(v(link.count)),
					cluster: clusters[link.source.cluster]
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
			.linkDistance(config.distance)
			.charge(config.charge)
			.on("tick", tick)
			.start();
		
		var svg = d3.select(config.selector).append("svg")
			.attr("width", width)
			.attr("height", height)
			.call(d3.behavior.zoom().scaleExtent([0.5, 4]).on("zoom", zoom));

		svg.on('mousedown.zoom',null);
		
		// build the arrow.
		svg.append("svg:defs").selectAll("marker")
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
			.attr("class", "node")
			.call(force.drag);
		
		node.append("circle").attr("r", 10).attr("class", function(d) {
			return dofocus(d.name, d.cluster, config);
		});

		node.append("image")
			.attr("xlink:href", function(d) {
				return d.avatar;
			})
			.attr("x", -8)
			.attr("y", -8)
			.attr("width", 16)
			.attr("height", 16);

		node.append("svg:title").text(function(d) { return d.name + " : " + d.cluster; });

		
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
				o.y += (foci[o.cluster].y - o.y) * k;
				o.x += (foci[o.cluster].x - o.x) * k;
			});
			
			node.attr("transform", function(d) {
				return "translate(" + d.x + "," + d.y + ")";
			});
		}	   
	}
	
	return {
		load: function (config) {
			$
				.when(
					readClusterInteractions(config),
					readOuterInteractions(config))
				.then(function(clusterInteractions, outerInteractions) {
					run(config, clusterInteractions[0], outerInteractions[0]);
				});
		}
	};
})();
