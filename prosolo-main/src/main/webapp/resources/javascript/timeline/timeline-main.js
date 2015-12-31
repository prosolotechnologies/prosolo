require([ 'timeline/timeline-graph'], function(graph) {	
    var root = document.getElementById("timeline-graph");
    graph.load({
    	element : root
    });
    
    $("input[type=checkbox][id^=timeline]").click(function(){
        var events = []
        $("input[type=checkbox][id^=timeline]:checked").each(function() {
        	  events.push($(this).attr("event-type"))
        })
        graph.recalculateMilestones(events);
    });
    
});
