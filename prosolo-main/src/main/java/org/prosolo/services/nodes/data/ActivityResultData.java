package org.prosolo.services.nodes.data;

import java.io.Serializable;
import java.util.Date;

import org.prosolo.common.util.date.DateUtil;
import org.prosolo.services.interaction.data.CommentsData;

public class ActivityResultData implements Serializable {

	private static final long serialVersionUID = 635098582303412076L;
	
	private String assignmentTitle; 
	private String result;
	private Date resultPostDate;
	private ActivityResultType resultType;
	private CommentsData resultComments;
	private UserData user;
	private long targetActivityId;
	
	public ActivityResultData() {
		resultType = ActivityResultType.NONE;
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
		this.resultType = resultType;
	}
	public CommentsData getResultComments() {
		return resultComments;
	}
	public void setResultComments(CommentsData resultComments) {
		this.resultComments = resultComments;
	}
	public UserData getUser() {
		return user;
	}
	public void setUser(UserData user) {
		this.user = user;
	}
	public String getPrettyResultPostDate() {
		if(resultPostDate == null) {
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
	
}
