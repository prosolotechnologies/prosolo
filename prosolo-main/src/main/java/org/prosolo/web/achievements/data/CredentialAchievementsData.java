package org.prosolo.web.achievements.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.prosolo.web.data.IData;

/**
 * @author "Musa Paljos"
 * 
 */
public class CredentialAchievementsData implements Serializable, IData {

	private static final long serialVersionUID = 2744828596870425737L;

	private List<TargetCredentialData> targetCredentialDataList;

	public CredentialAchievementsData() {
		targetCredentialDataList = new ArrayList<>();
	}

	public List<TargetCredentialData> getTargetCredentialDataList() {
		return targetCredentialDataList;
	}

	public void setTargetCredentialDataList(List<TargetCredentialData> targetCredentialDataList) {
		this.targetCredentialDataList = targetCredentialDataList;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public TargetCredentialData getTargetCredentialDataByid(long id) {
		for (TargetCredentialData data : targetCredentialDataList) {
			if (data.getId() == id) {
				return data;
			}
		}
		return null;
	}

}
