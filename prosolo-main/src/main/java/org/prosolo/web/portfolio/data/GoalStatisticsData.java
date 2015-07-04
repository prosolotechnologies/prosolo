/**
 * 
 */
package org.prosolo.web.portfolio.data;

import java.io.Serializable;

/**
 * @author "Nikola Milikic"
 * 
 */
public class GoalStatisticsData implements Serializable {

	private static final long serialVersionUID = 6134136369905829509L;

	private String averageTime;
	private int completedNo;
	private int totalNo;

	public String getAverageTime() {
		return averageTime;
	}

	public void setAverageTime(String averageTime) {
		this.averageTime = averageTime;
	}

	public int getCompletedNo() {
		return completedNo;
	}

	public void setCompletedNo(int completedNo) {
		this.completedNo = completedNo;
	}

	public int getTotalNo() {
		return totalNo;
	}

	public void setTotalNo(int totalNo) {
		this.totalNo = totalNo;
	}
	
	public String getProgressPercentage() {
		if (totalNo == 0) {
			return "0";
		} else {
			double c = ((double) completedNo) / totalNo;
			return String.valueOf(Math.round(c * 100));
		}
	}

}
