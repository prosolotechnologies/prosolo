package org.prosolo.bigdata.common.dal.pojo;

import java.util.ArrayList;
import java.util.List;

public class SocialInteractionsCount {

	private Long student;

	private Long cluster;

	private List<String> interactions;

	public SocialInteractionsCount(Long student, Long cluster, List<String> interactions) {
		this.student = student;
		this.cluster = cluster;
		this.interactions = interactions != null ? interactions : new ArrayList<String>();
	}

	public Long getStudent() {
		return student;
	}

	public void setStudent(Long student) {
		this.student = student;
	}

	public Long getCluster() {
		return cluster;
	}

	public void setCluster(Long cluster) {
		this.cluster = cluster;
	}

	public List<String> getInteractions() {
		return interactions;
	}

	public void setInteractions(List<String> interactions) {
		this.interactions = interactions;
	}

}
