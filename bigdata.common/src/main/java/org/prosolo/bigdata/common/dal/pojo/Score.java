package org.prosolo.bigdata.common.dal.pojo;

import java.io.Serializable;

/**
@author Zoran Jeremic Jun 6, 2015
 *
 */

public class Score implements Serializable{
	private Long id;
	private Double score;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Double getScore() {
		return score;
	}
	public void setScore(Double score) {
		this.score = score;
	}
}

