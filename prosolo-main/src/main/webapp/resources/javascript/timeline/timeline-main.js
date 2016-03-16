$(function () {	
    var root = document.getElementById("timeline-graph");
    timelineGraph.load({
    	element : root,
    	apiHost : $(root).attr('data-api'),
		studentId : $(root).attr('student-data-id')
    });
    
    $("input[type=checkbox][id^=timeline]").click(function(){
        var events = []
        $("input[type=checkbox][id^=timeline]:checked").each(function() {
        	  events.push($(this).attr("event-type"))
        })
        timelineGraph.recalculateMilestones(events);
    });
    
});
