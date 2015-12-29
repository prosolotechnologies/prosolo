define(['jquery', 'd3'], function($, d3) {

	var clusters = {
		"0": "one",
		"1": "two",
		"2": "three",
		"3": "four"
	};
	
	var foci = {
		"one" : {x: 0, y: 0},
		"two" : {x: 400, y: 0},
		"three" : {x: 0, y: 400},
		"four" : {x: 400, y: 400}
	};

	function readInteractions(config) {
		$.ajax({
			url : "http://" + config.host + "/api/social/interactions/",
			data : {"studentId" : config.studentId, "courseId" : config.courseId},
			type : "GET",
			crossDomain: true,
			dataType: 'json'
		}).done(function(data) {
			return run(config, data);
		});
	}

	function dofocus(user, cluster, config) {
		return user == config.studentId ? "focus " + cluster : "" + cluster;
	}
	
	function run(config, links) {

		var nodes = links.reduce(function(res, link) {
			res[link.source] = { name: link.source, cluster: clusters[link.cluster] };
			res[link.target] = { name: link.target, cluster: clusters[link.cluster] };
			return res;
		}, {});

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
					source: nodes[link.source],
					target: nodes[link.target],
					value: link.count,
					type: type(v(link.count)),
					cluster: clusters[link.cluster]
				};
			});
		}
		
		var width = config.width,
			height = config.height;

		var d3nodes = d3.values(nodes);
		var n = d3nodes.length;
		var m = Math.round(Math.sqrt(n));
		d3nodes.forEach(function(d, i) {
			d.x = width / m * (i % m);
			d.y = height / m * Math.round(i / m);
		});
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
			.call(d3.behavior.zoom().scaleExtent([0.5, 8]).on("zoom", zoom));
		
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
			.enter().append("g")
			.attr("class", "node");

		node.append("circle").attr("r", 10).attr("class", function(d) {
			return dofocus(d.name, d.cluster, config);
		});
		
		node.append("image")
			.attr("xlink:href", "http://code-bude.net/wp-content/uploads/2013/10/1372714624_github_circle_black.png")
			.attr("x", -8)
			.attr("y", -8)
			.attr("width", 16)
			.attr("height", 16);
		d3.selectAll(".node image").attr("style", "display: none");

		function zoom() {
			if (d3.event.scale >= 4) {
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
			readInteractions(config);
		}
	};
});
