/*
jQuery.ganttView v.0.8.8
Copyright (c) 2010 JC Grubbs - jc.grubbs@devmynd.com
MIT License Applies


Fork by Nikola Milikic - nikola.milikic@gmail.com
In this fork, the code has been changed so that the plugin does not accept values in date format, but values of relative number of days. An example of the data that can be supplied:

[
	{
		id: 1, name: "Feature 1", series: [
			{ name: "Planned", start: 1, duration: 3 },
			{ name: "Actual", start: 5, duration: 5, color: "#f0f0f0" }
		]
	}, 
	{
		id: 2, name: "Feature 2", series: [
			{ name: "Planned", start: 1, duration: 2 },
			{ name: "Actual", start: 2, duration: 16, color: "#f0f0f0" },
			{ name: "Projected", start: 7, duration: 99, color: "#e0e0e0" }
		]
	}
]
*/

/*
Options
-----------------
data: object,
fixedDuration: number,
cellWidth: number,
cellHeight: number,
slideWidth: number,
dataUrl: string,
legendContainer: string,
defaultColor: string,
behavior: {
	clickable: boolean,
	draggable: boolean,
	resizable: boolean,
	onClick: function,
	onDrag: function,
	onResize: function
}
*/

(function (jQuery) {
	
    jQuery.fn.ganttView = function () {
    	
    	var args = Array.prototype.slice.call(arguments);
    	
    	if (args.length == 1 && typeof(args[0]) == "object") {
        	build.call(this, args[0]);
    	}
    	
    	if (args.length == 2 && typeof(args[0]) == "string") {
    		handleMethod.call(this, args[0], args[1]);
    	}
    };
    
    function build(options) {
    	
    	var els = this;
        var defaults = {
            cellWidth: 21,
            cellHeight: 31,
            slideWidth: 400,
            vHeaderWidth: 100,
            fixedDuration: null,
            legendContainer: null,
            defaultColor: "#E5ECF9",
            behavior: {
            	clickable: false,
            	draggable: false,
            	resizable: false
            }
        };
        
        var opts = jQuery.extend(true, defaults, options);

		if (opts.data) {
			build();
		} else if (opts.dataUrl) {
			jQuery.getJSON(opts.dataUrl, function (data) { opts.data = data; build(); });
		}

		function build() {
			var minDays = Math.floor((opts.slideWidth / opts.cellWidth) + 1);
			opts.duration = DataUtils.getTotalDuration(opts.data);
			
			opts.start = 0;
			
			if (opts.duration < minDays) {
				opts.duration = minDays;
			}
			
	        els.each(function () {
	            var container = jQuery(this);
	            var div = jQuery("<div>", { "class": "ganttview" });
	            container.append(div);
	            
	            new Chart(div, opts).render();
				
				
				var w = jQuery("div.ganttview-vtheader", container).outerWidth() +
					jQuery("div.ganttview-slide-container", container).outerWidth();
	            container.css("width", (w + 2) + "px");
	            
	            new Behavior(container, opts).apply();
	        });
	        
	        if (opts.legendContainer) {
	        	var colors = [];
	        	var colorName = [];
	        	
	        	for (var i = 0; i < data.length; i++) {
	                for (var j = 0; j < data[i].series.length; j++) {
	                    var c = data[i].series[j].color;
	                    
	                    if ($.inArray(c, colors) < 0) {
	                    	colors.push(c);
	                    	colorName.push(
	                    			{
	                    				'name': data[i].series[j].name,
	                    				'color': c
                					}
	                    	);
	                    }
	                }
	            }
	        	
	        	new Legend(opts.legendContainer, colorName).render();
	        }
		}
    }

	function handleMethod(method, value) {
		
		if (method == "setSlideWidth") {
			var div = $("div.ganttview", this);
			div.each(function () {
				var vtWidth = $("div.ganttview-vtheader", div).outerWidth();
				$(div).width(vtWidth + value + 1);
				$("div.ganttview-slide-container", this).width(value);
			});
		}
	}

	var Chart = function(div, opts) {
		
		function render() {
			addVtHeader(div, opts.data, opts.cellHeight);

            var slideDiv = jQuery("<div>", {
                "class": "ganttview-slide-container",
                "css": { "width": opts.slideWidth + "px" }
            });
            
            div.append(slideDiv);
            
            var numbers = [];
            var maxNumberOfDays = opts.fixedDuration ? opts.fixedDuration : (opts.duration - 1);
            
            for(var i=0; i < maxNumberOfDays; i++){
            	numbers[i]=i+1;
            }
            addHzHeader(slideDiv, numbers, opts.cellWidth);
            addGrid(slideDiv, opts.data, numbers, opts.cellWidth);
            addBlockContainers(slideDiv, opts.data);
           	addBlocks(slideDiv, opts.data, opts.cellWidth);
            applyLastClass(div.parent());
		}

        function addVtHeader(div, data, cellHeight) {
            var headerDiv = jQuery("<div>", { "class": "ganttview-vtheader" });
            for (var i = 0; i < data.length; i++) {
                var itemDiv = jQuery("<div>", { "class": "ganttview-vtheader-item", "dataid": data[i].compid });
                
                if (data[i].predefined) {
                	itemDiv.attr('predefined', true);
                }
                
                itemDiv.append(jQuery("<div>", {
                    "class": "ganttview-vtheader-item-name",
                    "css": { "height": (data[i].series.length * cellHeight) + "px" }
                }).append(data[i].name));
                
                // if legend is not extracted
                if (!opts.legendContainer) {
                	var seriesDiv = jQuery("<div>", { "class": "ganttview-vtheader-series" });
                
	                for (var j = 0; j < data[i].series.length; j++) {
	                    seriesDiv.append(jQuery("<div>", { "class": "ganttview-vtheader-series-name" })
							.append(data[i].series[j].name));
	                }
	                itemDiv.append(seriesDiv);
                }
                headerDiv.append(itemDiv);
            }
            var title = jQuery("<div>", { "class": "ganttview-vtheader-title" });
            title.text('Duration in days');
            headerDiv.prepend(title);
            div.append(headerDiv);
        }

        function addHzHeader(div, numbers, cellWidth) {
            var headerDiv = jQuery("<div>", { "class": "ganttview-hzheader" });
            var daysDiv = jQuery("<div>", { "class": "ganttview-hzheader-days" });
            var totalW = numbers.length * cellWidth;

			for (var d in numbers) {
				daysDiv.append(jQuery("<div>", { "class": "ganttview-hzheader-day" })
					.append(numbers[d]));
			}
            daysDiv.css("width", totalW + "px");
            headerDiv.append(daysDiv);
            div.append(headerDiv);
        }

        function addGrid(div, data, numbers, cellWidth) {
            var gridDiv = jQuery("<div>", { "class": "ganttview-grid" });
            var rowDiv = jQuery("<div>", { "class": "ganttview-grid-row" });
			for (var d in numbers) {
				var cellDiv = jQuery("<div>", { "class": "ganttview-grid-row-cell" });
						rowDiv.append(cellDiv);
			}
            var w = jQuery("div.ganttview-grid-row-cell", rowDiv).length * cellWidth;
            rowDiv.css("width", w + "px");
            gridDiv.css("width", w + "px");
            for (var i = 0; i < data.length; i++) {
                for (var j = 0; j < data[i].series.length; j++) {
                    gridDiv.append(rowDiv.clone());
                }
            }
            div.append(gridDiv);
        }

        function addBlockContainers(div, data) {
            var blocksDiv = jQuery("<div>", { "class": "ganttview-blocks" });
            for (var i = 0; i < data.length; i++) {
                for (var j = 0; j < data[i].series.length; j++) {
                    blocksDiv.append(jQuery("<div>", { "class": "ganttview-block-container" }));
                }
            }
            div.append(blocksDiv);
        }

        function addBlocks(div, data, cellWidth) {
            var rows = jQuery("div.ganttview-blocks div.ganttview-block-container", div);
            var rowIdx = 0;
            for (var i = 0; i < data.length; i++) {
                for (var j = 0; j < data[i].series.length; j++) {
                    var series = data[i].series[j];
                    var size = series.duration;
					var offset = series.start  - 1;
					var block = jQuery("<div>", {
                        "class": "ganttview-block",
                        "title": series.name + ", " + size + " days",
                        "css": {
                            "width": ((size * cellWidth) - 9) + "px",
                            "margin-left": ((offset * cellWidth) + 3) + "px"
                        }
                    });
					if (series.fixed) {
						block.attr("fixed", true);
					}
					
                    addBlockData(block, data[i], series);
                    if (data[i].series[j].color) {
                        block.css("background-color", data[i].series[j].color);
                    } else {
                    	block.css("background-color", opts.defaultColor);
                    }
                    block.append(jQuery("<div>", { "class": "ganttview-block-text" }).text(size));
                    jQuery(rows[rowIdx]).append(block);
                    rowIdx = rowIdx + 1;
                }
            }
        }
        
        function addBlockData(block, data, series) {
        	// This allows custom attributes to be added to the series data objects
        	// and makes them available to the 'data' argument of click, resize, and drag handlers
        	var blockData = { id: data.id, name: data.name };
        	jQuery.extend(blockData, series);
        	block.data("block-data", blockData);
        }

        function applyLastClass(div) {
            jQuery("div.ganttview-grid-row div.ganttview-grid-row-cell:last-child", div).addClass("last");
            jQuery("div.ganttview-hzheader-days div.ganttview-hzheader-day:last-child", div).addClass("last");
            jQuery("div.ganttview-hzheader-months div.ganttview-hzheader-month:last-child", div).addClass("last");
        }
		
		return {
			render: render
		};
	};

	var Behavior = function (div, opts) {
		
		function apply() {
			
			if (opts.behavior.clickable) { 
            	bindBlockClick(div, opts.behavior.onClick); 
        	}
        	
            if (opts.behavior.resizable) { 
            	bindBlockResize(div, opts.cellWidth, opts.start, opts.behavior.onResize); 
        	}
            
            if (opts.behavior.draggable) { 
            	bindBlockDrag(div, opts.cellWidth, opts.start, opts.behavior.onDrag); 
        	}
		}

        function bindBlockClick(div, callback) {
            jQuery("div.ganttview-block:not([fixed='true'])", div).on("click", function () {
                if (callback) { callback(jQuery(this).data("block-data")); }
            });
        }
        
        function bindBlockResize(div, cellWidth, startDate, callback) {
        	jQuery("div.ganttview-block:not([fixed='true'])", div).resizable({
        		grid: cellWidth, 
        		handles: "e,w",
        		stop: function () {
        			var block = jQuery(this);
        			updateDataAndPosition(div, block, cellWidth, startDate);
        			if (callback) { callback(block.data("block-data")); }
        		}
        	});
        }
        
        function bindBlockDrag(div, cellWidth, startDate, callback) {
        	jQuery("div.ganttview-block:not([fixed='true'])", div).draggable({
        		axis: "x", 
        		grid: [cellWidth, cellWidth],
        		stop: function () {
        			var block = jQuery(this);
        			updateDataAndPosition(div, block, cellWidth, startDate);
        			if (callback) { callback(block.data("block-data")); }
        		}
        	});
        }
        
        function updateDataAndPosition(div, block, cellWidth, startDate) {
        	var container = jQuery("div.ganttview-slide-container", div);
        	var scroll = container.scrollLeft();
			var offset = block.offset().left - container.offset().left - 1 + scroll;
			
			// Set new start date
			//var daysFromStart = ;
			var newStart = Math.round(offset / cellWidth);
			block.data("block-data").start = newStart;

			// Set new end date
        	var width = block.outerWidth();
			var numberOfDays = Math.round(width / cellWidth) - 1;
			block.data("block-data").duration = numberOfDays;
			jQuery("div.ganttview-block-text", block).text(numberOfDays + 1);
			
			// Remove top and left properties to avoid incorrect block positioning,
        	// set position to relative to keep blocks relative to scrollbar when scrolling
			block.css("top", "").css("left", "")
				.css("position", "relative").css("margin-left", offset + "px");
        }
        
        return {
        	apply: apply	
        };
	};
	
	var Legend = function (div, colors) {
		function render() {
			var fragments = [];
			
			for (var i = 0; i < colors.length; ++i) {
                var entry = colors[i];

                fragments.push(
                    '<span class="item '+ (i == colors.length-1 ? 'last' : '') +'">'+
                    	'<span class="colorOuter">' +
                    		'<span class="colorInner" style="background: '+ entry.color +';"></span>' +
                    	'</span>' +
                    	'<span class="legendLabel">'+ entry.name +'</span>' +
                    '</span>'
                );
            }
			
			var content = '<span class="label">Legend:</span>' + fragments.join("");
			
			$(div).html(content);
		}
		
		return {
			render: render
		};
	};

    var ArrayUtils = {
	
        contains: function (arr, obj) {
            var has = false;
            for (var i = 0; i < arr.length; i++) { if (arr[i] == obj) { has = true; } }
            return has;
        }
    };
    
     var DataUtils = {
		getTotalDuration: function (data) {
			var totalDuration = 0;
			
			for(var i=0; i<data.length; i++) {
				var d = data[i].series;
				
				for(var j=0; j<d.length; j++) {
					var serieDuration = d[j].start + d[j].duration;
					
					if (serieDuration > totalDuration) {
						totalDuration = serieDuration;
					}
				}
			}
			
			return totalDuration;
		}
    };

})(jQuery);