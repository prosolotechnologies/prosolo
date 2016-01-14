$(function () {	
    var root = document.getElementById("timeline-graph");
    timelineGraph.load({
    	element : root
    });
    
    $("input[type=checkbox][id^=timeline]").click(function(){
        var events = []
        $("input[type=checkbox][id^=timeline]:checked").each(function() {
        	  events.push($(this).attr("event-type"))
        })
        timelineGraph.recalculateMilestones(events);
    });
    
});
