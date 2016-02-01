package org.prosolo.bigdata.session.impl;

import org.prosolo.bigdata.events.pojo.LogEvent;
import org.prosolo.bigdata.session.EventMatchCounter;


/**
 * @author Nikola Maric
 *
 */
public class LearningEventMatchCounter implements EventMatchCounter<LogEvent>{
	
	private int state = 0;
	private int numberOfStates;
	private LearningEventMatcher matcher;
	private int hitCounter;
	
	public LearningEventMatchCounter(LearningEventMatcher matcher) {
		super();
		this.matcher = matcher;
		//number of possible states is equal to number of rules we need to match + initial
		numberOfStates = matcher.getPatternList().size();
	}

	@Override
	public void processEvent(LogEvent event) {
		//get the matcher based on current state (if we had matches previously, 
		//we will get next matcher, and so on)
		boolean match = matcher.getPatternList().get(state).match(event);
		if(match) {
			//we had a match, advance state
			state += 1;
			if(state == numberOfStates) {
				//last state reached, restart from beginning
				state = 0;
				hitCounter++;
			}
		}
		//no matter what state we had before, object given does not have properties required to
		//advance state, reset the state (only successive matches should increase hit counter)
		else {
			state = 0;
		}
	}

	@Override
	public int getMatchCount() {
		return hitCounter;
	}
	
    
}
