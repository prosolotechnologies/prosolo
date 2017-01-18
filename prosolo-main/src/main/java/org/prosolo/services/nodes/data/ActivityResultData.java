package org.prosolo.services.nodes.data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.prosolo.common.util.date.DateUtil;
import org.prosolo.services.common.observable.StandardObservable;
import org.prosolo.services.interaction.data.ResultCommentInfo;
import org.prosolo.services.interaction.data.CommentsData;

public class ActivityResultData extends StandardObservable implements Serializable {

	private static final long serialVersionUID = 635098582303412076L;

	private String assignmentTitle;
	private String result;
	private Date resultPostDate;
	private ActivityResultType resultType;
	private CommentsData resultComments;
	private List<ResultCommentInfo> otherResultsComments;
	private UserData user;
	private long targetActivityId;
	// for instructor to post messages and grades;
	private ActivityAssessmentData assessment;

	public ActivityResultData(boolean listenChanges) {
		resultType = ActivityResultType.NONE;
		assessment = new ActivityAssessmentData();
		this.listenChanges = listenChanges;
	}

	public String getAssignmentTitle() {
		return assignmentTitle;
	}

	public void setAssignmentTitle(String assignmentTitle) {
		this.assignmentTitle = assignmentTitle;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public Date getResultPostDate() {
		return resultPostDate;
	}

	public void setResultPostDate(Date resultPostDate) {
		this.resultPostDate = resultPostDate;
	}

	public ActivityResultType getResultType() {
		return resultType;
	}

	public void setResultType(ActivityResultType resultType) {
		observeAttributeChange("resultType", this.resultType, resultType);
		this.resultType = resultType;
	}

	public CommentsData getResultComments() {
		return resultComments;
	}

	public void setResultComments(CommentsData resultComments) {
		this.resultComments = resultComments;
	}

	public List<ResultCommentInfo> getOtherResultsComments() {
		return otherResultsComments;
	}

	public void setOtherResultsComments(List<ResultCommentInfo> otherResultsComments) {
		this.otherResultsComments = otherResultsComments;
	}

	public UserData getUser() {
		return user;
	}

	public void setUser(UserData user) {
		this.user = user;
	}

	public String getPrettyResultPostDate() {
		if (resultPostDate == null) {
			return "";
		}
		return DateUtil.getTimeAgoFromNow(resultPostDate);
	}

	public long getTargetActivityId() {
		return targetActivityId;
	}

	public void setTargetActivityId(long targetActivityId) {
		this.targetActivityId = targetActivityId;
	}

	public ActivityAssessmentData getAssessment() {
		return assessment;
	}

	public void setAssessment(ActivityAssessmentData assessment) {
		this.assessment = assessment;
	}

	public boolean isResultTypeChanged() {
		return changedAttributes.containsKey("resultType");
	}

}
