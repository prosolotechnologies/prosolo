package org.prosolo.bigdata.common.dal.pojo;

public class OuterInteractionsCount {

	private Long student;

	private Long cluster;

	private String interactions;
	
	private String direction;

	public OuterInteractionsCount(Long student, Long cluster, String interactions, String direction) {
		this.student = student;
		this.cluster = cluster;
		this.interactions = interactions;
		this.direction = direction;
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
	
	public String getDirection() {
		return direction;
	}
	
	public void setDirection(String direction) {
		this.direction = direction;
	}

}
