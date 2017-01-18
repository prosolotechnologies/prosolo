package org.prosolo.services.interaction.data;

import java.util.Date;

/**
 * This class is used for storing info about comments a student has made on
 * other students' response. It is created to be used on the Activity Response
 * page.
 * 
 * @author Nikola Milikic
 *
 */
public class ResultCommentInfo {

	private int noOfComments;
	private String resultCreator;
	private Date firstCommentDate;
	private long targetActivityId; // id of target activity whose result is commented on

	public ResultCommentInfo(int noOfComments, String resultCreator, Date firstCommentDate, long targetActivityId) {
		this.noOfComments = noOfComments;
		this.resultCreator = resultCreator;
		this.firstCommentDate = firstCommentDate;
		this.targetActivityId = targetActivityId;
	}

	public int getNoOfComments() {
		return noOfComments;
	}

	public void setNoOfComments(int noOfComments) {
		this.noOfComments = noOfComments;
	}

	public String getResultCreator() {
		return resultCreator;
	}

	public void setResultCreator(String resultCreator) {
		this.resultCreator = resultCreator;
	}

	public Date getFirstCommentDate() {
		return firstCommentDate;
	}

	public void setFirstCommentDate(Date firstCommentDate) {
		this.firstCommentDate = firstCommentDate;
	}

	public long getTargetActivityId() {
		return targetActivityId;
	}

	public void setTargetActivityId(long targetActivityId) {
		this.targetActivityId = targetActivityId;
	}

}
