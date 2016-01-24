package org.prosolo.bigdata.common.dal.pojo;

public class SocialIneractionsCount {

	private Long student;

	private Long cluster;

	private String interactions;

	public SocialIneractionsCount(Long student, Long cluster, String interactions) {
		this.student = student;
		this.cluster = cluster;
		this.interactions = interactions;
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

	public String getInteractions() {
		return interactions;
	}

	public void setInteractions(String interactions) {
		this.interactions = interactions;
	}

}
