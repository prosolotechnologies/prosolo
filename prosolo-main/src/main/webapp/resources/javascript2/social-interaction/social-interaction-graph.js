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

    function initializeDataForStudent(config, student, courseid) {
        $('.socialTab .selectedStudentName').html(student.name);
        $('.socialTab .selectedStudentAvatar').attr('src', student.avatar).attr('alt', student.name);

        $.ajax({
            url: config.host + "/social/interactions/interactionsbypeers/" + courseid + "/" + student.id,
            //data : {"students" : part},
            type: "GET",
            crossDomain: true,
            dataType: 'json'
        }).done(function (data) {
            for (var i = 0; i < data[0].interactions.length; i++) {
                data[0].interactions[i] = JSON.parse(data[0].interactions[i]);
            }
            var interactions = data[0].interactions;
            var peersinteractions = {};
            var peers = [];
            if (interactions.length > 0) {
                interactions.forEach(function (interaction) {
                    var intobject = {};
                    if (typeof (peersinteractions[interaction.peer]) !== "undefined") {
                        intobject = peersinteractions[interaction.peer];
                    } else peers.push(interaction.peer);
                    intobject[interaction.direction] = {
                        count: interaction.count,
                        percentage: Math.round(interaction.percentage * 100)
                    }
                    peersinteractions[interaction.peer] = intobject;
                });
                $.when(
                    getStudentsData(config, peers))
                    .then(function (studentsData) {
                        $('.socialTab .interactionsByPeers').empty();

                        var innerHtml = "<tr><th>Student</th><th>OUT</th><th>IN</th></tr>";

                        for (var peerId in peersinteractions) {
                            var interaction = peersinteractions[peerId];
                            var student = studentsData[peerId];

                            innerHtml = innerHtml + "<tr><td>" + student.name + "</td>" +
                                "<td>" + (typeof interaction.OUT == 'undefined' ? "-" : interaction.OUT.count + " (" + interaction.OUT.percentage + " %)") + "</td>" +
                                "<td>" + (typeof interaction.IN == 'undefined' ? "-" : interaction.IN.count + " (" + interaction.IN.percentage + " %)") + "</td></tr>";
                        }
                        ;
                        $('.socialTab .interactionsByPeers').append(innerHtml);
                    });
            }
        });

        $.ajax({
            url: config.host + "/social/interactions/interactionsbytype/" + courseid + "/" + student.id,
            //data : {"students" : part},
            type: "GET",
            crossDomain: true,
            dataType: 'json'
        }).done(function (data) {
            for (var i = 0; i < data[0].interactions.length; i++) {
                data[0].interactions[i] = JSON
                    .parse(data[0].interactions[i]);
            }
            var interactions = data[0].interactions;
            if (interactions.length > 0) {
                $('.socialTab .interactionsByType').empty();
                var innerHtml = "<tr><th>Type</th><th>OUT</th><th>IN</th></tr>";
                interactions.forEach(function (interaction) {
                    innerHtml = innerHtml +
                        "<tr><td>" + interaction.type + "</td>" +
                        "<td>" + interaction.fromusercount + " (" + Math.round(interaction.fromuserpercentage * 100) + " %)</td>" +
                        "<td>" + interaction.tousercount + " (" + Math.round(interaction.touserpercentage * 100) + " %)" + "</td></tr>";
                });
                $('.socialTab .interactionsByType').append(innerHtml);
            }
        });
    };

    function getStudentsData(config, peers) {
        return $.ajax({
            url: config.host + "/social/interactions/data",
            data: {"students": peers},
            type: "GET",
            crossDomain: true,
            dataType: "json"
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

        var width = document.getElementById(config.graphContainerId).offsetWidth,
            height = document.getElementById(config.graphContainerId).offsetHeight;

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

		var drag = force.drag()
		 .on("dragstart", dragstart);

		var zoom = d3.behavior.zoom()
            .translate([0, 0])
            .scale(1)
            .scaleExtent([1, 3])
            .on("zoom", zoomed);

        var svg = d3.select('#'+config.graphContainerId)
            .append("svg")
            .attr("height", height)
            .attr("viewBox", "0 0 " + width + " " + height)
            .append("g")
            .call(zoom);

        svg.on('mousedown.zoom',null);

        svg.append("rect")
            .attr("class", "background")
            .attr("width", width)
            .attr("height", height)
            .on("click", reset);

        // var svgdefs = svg.append("svg:defs");
        // // build the arrow.
        // svgdefs.selectAll("marker")
        //     .data(["end"]) // Different link/path types can be defined here
        //     .enter().append("svg:marker") // This section adds in the arrows
        //     .attr("id", String)
        //     .attr("viewBox", "0 -5 10 10")
        //     .attr("refX", 16)
        //     .attr("refY", 0)
        //     .attr("markerWidth", 13)
        //     .attr("markerHeight", 13)
        //     .attr("orient", "auto")
        //     .attr("markerUnits", "userSpaceOnUse")
        //     .append("svg:path")
        //     .append("g")
        //     .attr("d", "M0,-5L10,0L0,5");
        //
        // svgdefs.append("svg:clipPath")
        //     .attr("id", "circle-clip")
        //     .append("svg:circle")
        //     .append("g")
        //     .attr("r", "10")
        //     .attr("cx", "0")
        //     .attr("cy", "0");

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
                svg.selectAll(".selected").classed('selected',false);
                d3.select(this).select("circle").classed('selected',true);
                var student = {
                    id: d.name,
                    cluster: d.cluster,
                    name: studentData[d.name] ? studentData[d.name].name : "",
                    avatar: studentData[d.name] ? studentData[d.name].avatar : ""
                };
                initializeDataForStudent(config, student, config.courseId);

            })
            .attr("class","focus")
            .call(force.drag);

        node.append("svg:defs").attr("id", "mdef")
            .append("svg:pattern")
                .attr("id", function(d,i) { return "image"+i; })
                .attr("x", "0")
                .attr("y", "0")
                .attr("height", "20")
                .attr("width", "20")
            .append("svg:image")
                .attr("x", "0")
                .attr("y", "0")
                .attr("height", "20")
                .attr("width", "20")
                .attr("xlink:href", function(d) {
                    return studentData[d.name] ? studentData[d.name].avatar : "";
                });

        node.append("svg:circle")
                .attr("r", 10)
                .style("fill", function(d,i) { return "url(#image"+i+")"; })
                .attr("class", function(d) {
                    return (d.name == config.studentId ? "selected focus " : "") + d.clusterClass;
                });

        node.append("svg:text")
            .attr("dx","-2em")
            .attr("dy","-2em")
            .text(function(d){return studentData[d.name] ? studentData[d.name].name : ""; });


        node.append("svg:title").text(function(d) { return studentData[d.name] ? studentData[d.name].name : ""; });

        function zoomed() {
            if (d3.event.scale >= 1) {
                d3.select(this).selectAll("circle").each(function(d) {
                    var patternId = d3.select(this.parentNode).selectAll("pattern").attr('id');
                    d3.select(this).style("fill", "url(#"+patternId+")");
                });
            } else {
                d3.select(this).selectAll("circle").style("fill", "#ccc");
            };
            svg.attr("transform", "translate(" + d3.event.translate + ")scale(" + d3.event.scale + ")");
        }

        function reset() {
            svg.transition()
                .duration(750)
                .call(zoom.translate([0, 0]).scale(1).event);
        }

        function tick(e) {
            var radius = 6;
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

            node.attr("cx", function(d) { return d.x = Math.max(radius, Math.min(width - radius, d.x)); })
                .attr("cy", function(d) { return d.y = Math.max(radius, Math.min(height - radius, d.y)); });

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
            $.when(
                readClusterInteractions(config),
                readOuterInteractions(config))
                .then(function(ci, oi) {
                    parse(ci[0]);
                    parse(oi[0]);
                    if ((ci[0].length + oi[0].length) == 0) {
                        $("#interactionGraph").hide();
                        $("#noDataMessage").text(config.systemNotAvailableMessage);
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
                $("#interactionGraph").hide();
                $("#noDataMessage").text(config.systemNotAvailableMessage);
            });

            var student = {
                id: config.studentId,
                cluster: 0,
                name: config.studentName,
                avatar: config.studentAvatar
            };
            initializeDataForStudent(config, student, config.courseId);
        },
    };
})();