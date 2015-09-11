/**
 * 
 */
package org.prosolo.web.dialogs.analytics.data;

import java.io.Serializable;

/**
 * @author "Nikola Milikic"
 * 
 */
public class GoalAnalyticsData implements Serializable {

	private static final long serialVersionUID = -3152554989085800159L;

	private long completionTime;

	public long getCompletionTime() {
		return completionTime;
	}

	public void setCompletionTime(long completionTime) {
		this.completionTime = completionTime;
	}

}
