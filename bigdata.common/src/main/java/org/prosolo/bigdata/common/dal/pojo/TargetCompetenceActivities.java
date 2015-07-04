package org.prosolo.bigdata.common.dal.pojo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
@author Zoran Jeremic May 3, 2015
 *
 */

public class TargetCompetenceActivities  implements Serializable{
	private long targetCompetenceId;
	private long competenceId;
	private List<Long> activities;
	
	public TargetCompetenceActivities(){
		activities=new ArrayList<Long>();
	}
	public TargetCompetenceActivities(long tCompId, long compId, List<Long> activities){
		super();
		setTargetCompetenceId(tCompId);
		setCompetenceId(compId);
		setActivities(activities);
	}
	
	public long getTargetCompetenceId() {
		return targetCompetenceId;
	}

	public void setTargetCompetenceId(long targetCompetenceId) {
		this.targetCompetenceId = targetCompetenceId;
	}

	public long getCompetenceId() {
		return competenceId;
	}

	public void setCompetenceId(long competenceId) {
		this.competenceId = competenceId;
	}

	public List<Long> getActivities() {
		return activities;
	}

	public void setActivities(List<Long> activities) {
		this.activities = activities;
	}


}

