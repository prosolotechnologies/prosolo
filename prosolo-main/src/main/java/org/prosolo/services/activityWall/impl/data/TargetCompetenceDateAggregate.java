/**
 * 
 */
package org.prosolo.services.activityWall.impl.data;

import java.util.Date;

import org.prosolo.common.domainmodel.competences.TargetCompetence;

/**
 * @author "Nikola Milikic"
 * 
 *         Sep 6, 2014
 */
public class TargetCompetenceDateAggregate {

	private TargetCompetence targetCompetence;
	private Date date;

	public TargetCompetenceDateAggregate(TargetCompetence targetCompetence,	Date date) {
		this.targetCompetence = targetCompetence;
		this.date = date;
	}

	public TargetCompetence getTargetCompetence() {
		return targetCompetence;
	}

	public void setTargetCompetence(TargetCompetence targetCompetence) {
		this.targetCompetence = targetCompetence;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

}
