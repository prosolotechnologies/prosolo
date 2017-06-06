package org.prosolo.services.nodes.data;

import java.util.Date;

import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.services.nodes.data.instructor.InstructorData;

public class StudentData {

	private UserData user;
	private InstructorData instructor;
	private int progress;
	private boolean assigned;
	private long assessmentId;
	private boolean enrolled;
	private Date dateEnrolled;
	private Date dateCompleted;
	
	public StudentData() {

	}
	
	public StudentData(User user) {
		this.user = new UserData(user);
	}
	
	public String getFormattedEnrollDate() {
		String date = DateUtil.formatDate(dateEnrolled, "MMM dd, yyyy");
		return date != null ? date : "-";
	}
	
	public String getFormattedCompletionDate() {
		String date = DateUtil.formatDate(dateCompleted, "MMM dd, yyyy");
		return date != null ? date : "-";
	}

//	public UserData(User user, Map<String, Object> instructor, int progress, String profileType, String profileTitle) {
//		this(user);
//		if(instructor != null) {
//			this.instructor = new CourseInstructorData();
//			this.instructor.setName((String) instructor.get("firstName") + (instructor.get("lastName") != null ? " " + 
//					(String) instructor.get("lastName") : ""));
//			this.instructor.setInstructorId((long) instructor.get("instructorId"));
//		}
//		this.courseProgress = progress;
//	}

	public InstructorData getInstructor() {
		return instructor;
	}

	public void setInstructor(InstructorData instructor) {
		this.instructor = instructor;
	}

	public UserData getUser() {
		return user;
	}

	public void setUser(UserData user) {
		this.user = user;
	}

	public boolean isAssigned() {
		return assigned;
	}

	public void setAssigned(boolean assigned) {
		this.assigned = assigned;
	}

	public long getAssessmentId() {
		return assessmentId;
	}

	public void setAssessmentId(long assessmentId) {
		this.assessmentId = assessmentId;
	}

	public boolean isEnrolled() {
		return enrolled;
	}

	public void setEnrolled(boolean enrolled) {
		this.enrolled = enrolled;
	}

	public int getProgress() {
		return progress;
	}

	public void setProgress(int progress) {
		this.progress = progress;
	}

	public Date getDateEnrolled() {
		return dateEnrolled;
	}

	public void setDateEnrolled(Date dateEnrolled) {
		this.dateEnrolled = dateEnrolled;
	}

	public Date getDateCompleted() {
		return dateCompleted;
	}

	public void setDateCompleted(Date dateCompleted) {
		this.dateCompleted = dateCompleted;
	}
	
}
