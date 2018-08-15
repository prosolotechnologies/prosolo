package org.prosolo.bigdata.session.impl;

import org.prosolo.bigdata.events.pojo.LogEvent;


/**
 * @author Nikola Maric
 *
 */
public class LearningEventMatchCounter {
	
	private int state = 0;
	private int numberOfStates;
	private LearningEventMatcher matcher;
	private LearningEventsMatchSummary result;
	
	public LearningEventMatchCounter(LearningEventMatcher matcher) {
		super();
		this.matcher = matcher;
		//number of possible states is equal to number of rules we need to match + initial
		numberOfStates = matcher.getPatternList().size();
		result = new LearningEventsMatchSummary(matcher.getId(), matcher.getDescription(), 
				matcher.getProcess(), matcher.isMilestoneEvent(),matcher.getType(),matcher.getName());
	}

	public void processEvent(LogEvent event,final long epochDay) {
		//get the matcher based on current state (if we had matches previously, 
		//we will get next matcher, and so on)
		//TODO if there can be more than one pattern in the pattern list, only first would be processed
		boolean match = matcher.getPatternList().get(state).match(event); 
		if(match) {
			//we had a match, advance state
			state += 1;
			if(state == numberOfStates) {
				//last state reached, restart from beginning
				state = 0;
				result.hit(event,epochDay);
			}
		}
		//no matter what state we had before, object given does not have properties required to
		//advance state, reset the state (only successive matches should increase hit counter)
		else {
			state = 0;
		}
	}

	public LearningEventsMatchSummary getResult() {
		return result;
	}
    
}
