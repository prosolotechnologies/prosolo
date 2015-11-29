define(function(require) {
	var $ = require('jquery'), d3 = require('d3');
	
	function socialInteraction() {
		return document.querySelector("#social-interaction");
	}
	
	function host() {
		return socialInteraction().dataset["api"];
	}

	function distinct(array) {
		return array.reduce(function(acc, current) {
			if (acc.indexOf(current) == -1) {
				acc.push(current);
			}
			return acc;
		}, []);
	}

	Array.prototype.distinct = function() {
		return distinct(this);
	}

	function range(from, to) {
		var result = [];
		for (var i = from; i < to; i++) {
			result.push(i);
		}
		return result;
	}

	function random(from, to) {
		return from + Math.floor(Math.random() * (to - from));
	}

	function genLinks(count) {
		return range(1, count).map(function() {
			return [ random(1, 100) + "", random(1, 100) + "", random(1, 10) ];
		}).map(function(e) {
			return {
				"source" : e[0],
				"target" : e[1],
				"count" : e[2]
			}
		});
	}
	
	function getSocialInteractions(config){
		$.ajax({
			url : "http://" + host() + "/api/social/interactions/all",
			type : "GET",
			crossDomain: true,
			dataType: 'json'
		}).done(function(data) {
			return run(config, data);
		});
	}

	function has(array, value) {
		return array.filter(function(e) { return e == value }).length > 0;
	}
	
	function dofocus(user) {
		if (user == "1") {
			return "focus " + cluster(user); 
		}
		return cluster(user);
	}
	
    var clusters = {"A" : range(1,75),
    		"B" : range(76,90),
    		"C" : range(91,99)}
	
	function cluster(user) {
		if (has(clusters.A, user)) { return "A" };
		if (has(clusters.B, user)) { return "B" };
		if (has(clusters.C, user)) { return "C" };
		return "D";
	}
    
    function run(config, links) {
	    //var link1s = genLinks(config.links);
	    var nodes = {};

	    var sources = links.map(function(link) { return link.source });
	    var targets = links.map(function(link) { return link.target });
	    var nodes = []
	        .concat(sources)
	        .concat(targets)
	        .distinct()
	        .reduce(function(res, name) {res[name] = { name: name}; return res}, {});
	    
	    function relations(links) {
		    // asign a type per value to encode opacity
		    // type prema broju poruka?
		    // koje i koliko grupa (type-ove) treba formirati?
		    // dodati i property da li je main ili nije
		    function type(value) {
		    	if (value <= 33) {
		            return "twofive";
		        } 
		    	if (value <= 66 && value > 33) {
		            return "fivezero";
		        } 
		    	if (value <= 85 && value > 66) {
		            return "sevenfive";
		        } 
		    	if (value <= 100 && value > 85) {
		            return "onezerozero";
		        }
		    	return "";
		    }

	    	var v = d3.scale.linear().range([0, 100]);
	    	v.domain([0, d3.max(links, function(d) {
	    		return d.count;
	    	})]);
	    	
	    	return links.map(function(link) {
	    		return {
	    			source: nodes[link.source],
	    			target: nodes[link.target],
	    			value: link.count,
	    			type: type(v(link.count))
	    		}
	    	});
	    }
	    
	    var width = config.width,
	        height = config.height;

	    var d3nodes = d3.values(nodes);
	    var n = d3nodes.length;
	    d3nodes.forEach(function(d, i) {
	        d.x = d.y = width / n * i;
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
	        .call(d3.behavior.zoom().scaleExtent([1, 8]).on("zoom", zoom));
	    
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
	    	return dofocus(d.name);
	    })
	    
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
	    
    	function tick() {
			path.attr("d", function(d) {
				var dx = d.target.x - d.source.x,
				dy = d.target.y - d.source.y,
				dr = Math.sqrt(dx * dx + dy * dy) * 2;
				return "M" + d.source.x + "," + d.source.y + "A" + dr + "," + dr + " 0 0,1 " + d.target.x + "," + d.target.y;
			});
			
			node.attr("transform", function(d) {
				return "translate(" + d.x + "," + d.y + ")";
			});
		}
	    
	}
	
	return {
		load: function (config) {
			getSocialInteractions(config);
		}
	}
});