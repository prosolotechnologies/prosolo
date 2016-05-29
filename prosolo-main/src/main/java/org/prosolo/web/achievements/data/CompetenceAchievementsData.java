package org.prosolo.web.achievements.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.prosolo.web.data.IData;

/**
 * @author "Musa Paljos"
 * 
 */

public class CompetenceAchievementsData implements Serializable, IData {

	private static final long serialVersionUID = 2744828516870425737L;

	private List<TargetCompetenceData> targetCompetenceDataList;

	public CompetenceAchievementsData() {
		targetCompetenceDataList = new ArrayList();
	}

	public List<TargetCompetenceData> getTargetCompetenceDataList() {
		return targetCompetenceDataList;
	}

	public void setTargetCompetenceDataList(List<TargetCompetenceData> targetCompetenceDataList) {
		this.targetCompetenceDataList = targetCompetenceDataList;
	}

	public TargetCompetenceData getTargetCompetenceDataByid(long id) {
		for (TargetCompetenceData data : targetCompetenceDataList) {
			if (data.getId() == id) {
				return data;
			}
		}
		return null;
	}

}
