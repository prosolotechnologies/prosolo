 dimple.plot.custom = {
        stacked: !1,
        grouped: !1,
        supportedAxes: ["x", "y", "z", "c"],
        draw: function(a, b, c) {
            var d, e, f = b._positionData,
                g = null,
                h = ["dimple-series-" + a.series.indexOf(b), "dimple-bubble"];
            null !== a._tooltipGroup && void 0 !== a._tooltipGroup && a._tooltipGroup.remove(), g = null === b.shapes || void 0 === b.shapes ? a._group.selectAll("." + h.join(".")).data(f) : b.shapes.data(f, function(a) {
                return a.key
            }), g.enter().append("circle")
            			.style("fill", "#E55B68")
            			 .style("stroke", "transparent")  
            			.attr('fill-opacity', 0.2)
            			.attr("id", function(a) {
                return a.key
            }).attr("class", function(a) {
                var b = [];
                return b = b.concat(a.aggField), b = b.concat(a.xField), b = b.concat(a.yField), b = b.concat(a.zField), h.join(" ") + " " + dimple._createClass(b)
            }).attr("cx", function(c) {
                return b.x._hasCategories() ? dimple._helpers.cx(c, a, b) : b.x._previousOrigin
            }).attr("cy", function(c) {
                return b.y._hasCategories() ? dimple._helpers.cy(c, a, b) : b.y._previousOrigin
            })
            .attr("r", 0).attr("opacity", function(c) {
            
              //  return dimple._helpers.opacity(c, a, b)
            })
            .on("mouseover", function(c) {
                dimple._showPointTooltip(c, this, a, b)
            }).on("mouseleave", function(c) {
                dimple._removeTooltip(c, this, a, b)
            }).call(function() {
                a.noFormats || this.attr("fill", function(c) {
                    return dimple._helpers.fill(c, a, b)
                }).attr("stroke", function(c) {
                    return dimple._helpers.stroke(c, a, b)
                })
            }), d = a._handleTransition(g, c, a, b).attr("cx", function(c) {
                return dimple._helpers.cx(c, a, b)
            }).attr("cy", function(c) {
                return dimple._helpers.cy(c, a, b)
            }).attr("r", function(c) {
            	//shape circle radius
            	return 14
                //return dimple._helpers.r(c, a, b)
            }).call(function() {
                a.noFormats || this.attr("fill", function(c) {
                    return dimple._helpers.fill(c, a, b)
                }).attr("stroke", function(c) {
                    return dimple._helpers.stroke(c, a, b)
                })
            }), e = a._handleTransition(g.exit(), c, a, b).attr("r", 0).attr("cx", function(c) {
                return b.x._hasCategories() ? dimple._helpers.cx(c, a, b) : b.x._origin
            }).attr("cy", function(c) {
                return b.y._hasCategories() ? dimple._helpers.cy(c, a, b) : b.y._origin
            }), dimple._postDrawHandling(b, d, e, c), b.shapes = g
        }
    }