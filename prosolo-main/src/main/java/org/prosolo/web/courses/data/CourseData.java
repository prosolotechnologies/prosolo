/**
 * 
 */
package org.prosolo.web.courses.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.competences.Competence;
import org.prosolo.common.domainmodel.course.Course;
import org.prosolo.common.domainmodel.course.CourseCompetence;
import org.prosolo.common.domainmodel.course.CourseEnrollment;
import org.prosolo.common.domainmodel.course.CreatorType;
import org.prosolo.common.domainmodel.feeds.FeedSource;
//import org.prosolo.util.nodes.CreatedAscComparator;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.common.web.activitywall.data.UserData;
import org.prosolo.util.nodes.AnnotationUtil;
import org.prosolo.web.courses.util.CourseDataConverter;

/**
 * @author "Nikola Milikic"
 * 
 */
public class CourseData implements Comparable<CourseData>, Serializable {

	private static final long serialVersionUID = -160871336144679322L;

	private long id;
	private Date dateCreated;
	private long enrollmentId;
	private long targetGoalId;
	private String title;
	private String description;
	private UserData maker;
	private boolean enrolled;
	private boolean editable;
	private boolean active;
	private boolean inFutureCourses;
	private boolean inCompletedCourses;
	private boolean inWithdrawnCourses;
	private boolean studentsCanAddNewCompetences;
	private boolean published;
	private Date dateStarted;
	private String dateStartedString;
	private Date dateFinished;
	private String dateFinishedString;
	private int progress;
	
	private List<CourseCompetenceData> originalCompetences; // competences defined in a Course
	private List<CourseCompetenceData> addedCompetences;
	private List<Tag> tags;
	private String tagsString;
	private CreatorType creatorType;
	private List<Long> memberIds;
	private List<String> blogs;
	
	private String hashtagsString = "";

//	private Course basedOnCourse;
//	private Course course;
	private long basedOnCourse;

	public CourseData() {
		this.originalCompetences = new ArrayList<CourseCompetenceData>();
		this.addedCompetences = new ArrayList<CourseCompetenceData>();
		this.tags = new ArrayList<Tag>();
		this.blogs = new ArrayList<>();
	}

	public CourseData(Course course) {
		this();
		this.id = course.getId();
		this.dateCreated = course.getDateCreated();
		this.title = course.getTitle();
		this.description = course.getDescription();
		this.maker = new UserData(course.getMaker());
		if (course.getBasedOn() != null) {
			this.basedOnCourse = course.getBasedOn().getId();
		}
		this.originalCompetences = CourseDataConverter.convertToCompetenceCourseData(course.getCompetences(), true);
		this.tags = new ArrayList<Tag>(course.getTags());
		this.tagsString = AnnotationUtil.getAnnotationsAsSortedCSV(this.tags);
		this.creatorType = course.getCreatorType();
		this.studentsCanAddNewCompetences = course.isStudentsCanAddNewCompetences();
		this.published = course.isPublished();
		
		this.hashtagsString = AnnotationUtil.getAnnotationsAsSortedCSV(course.getHashtags());
		
		for (FeedSource blog : course.getBlogs()) {
			this.blogs.add(blog.getLink());
		}
	}
	
	public CourseData(CourseEnrollment enrollment) {
		this(enrollment.getCourse());
		this.setEnrollment(enrollment);
		this.setDateStarted(enrollment.getDateStarted());
		this.setDateFinished(enrollment.getDateFinished());
	}
	
	public List<CourseCompetence> cloneCourseCompetences(Collection<CourseCompetence> originalCourseComps) {
		List<CourseCompetence> courseCompetences = new ArrayList<CourseCompetence>();
		
		for (CourseCompetence courseComp : originalCourseComps) {
			courseCompetences.add(new CourseCompetence(courseComp));
		}
		
		return courseCompetences;
	}
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	public String getDateCreatedPretty() {
		return DateUtil.getPrettyDate(dateCreated);
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public UserData getMaker() {
		return maker;
	}

	public void setMaker(UserData maker) {
		this.maker = maker;
	}

	public boolean isEditable() {
		return editable;
	}

	public void setEditable(boolean editable) {
		this.editable = editable;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public Date getDateStarted() {
		return dateStarted;
	}

	public void setDateStarted(Date dateStarted) {
		this.dateStarted = dateStarted;
		
		if (dateStarted != null) {
			this.dateStartedString = DateUtil.getPrettyDate(dateStarted);
		} else {
			this.dateStartedString = "";
		}
	}
	
	public String getDateStartedString() {
		return dateStartedString;
	}

	public void setDateStartedString(String dateStartedString) {
		this.dateStartedString = dateStartedString;
	}

	public Date getDateFinished() {
		return dateFinished;
	}

	public void setDateFinished(Date dateFinished) {
		this.dateFinished = dateFinished;
		
		if (dateFinished != null) {
			this.dateFinishedString = DateUtil.getPrettyDate(dateFinished);
		} else {
			this.dateFinishedString = "";
		}
	}
	
	public String getDateFinishedString() {
		return dateFinishedString;
	}

	public void setDateFinishedString(String dateFinishedString) {
		this.dateFinishedString = dateFinishedString;
	}

	public int getProgress() {
		return progress;
	}

	public void setProgress(int progress) {
		this.progress = progress;
	}

	public List<CourseCompetenceData> getOriginalCompetences() {
		return originalCompetences;
	}

	public void setOriginalCompetences(List<CourseCompetenceData> originalCompetences) {
		this.originalCompetences = originalCompetences;
	}

	public List<CourseCompetenceData> getAddedCompetences() {
		return addedCompetences;
	}

	public void setAddedCompetences(List<CourseCompetenceData> addedCompetences) {
		this.addedCompetences = addedCompetences;
	}

	public boolean removeOriginalCourseCompetence(long competenceId) {
		Iterator<CourseCompetenceData> iterator = originalCompetences.iterator();
		
		while (iterator.hasNext()) {
			CourseCompetenceData courseCompetenceData = iterator.next();
			
			if (courseCompetenceData.getCompetenceId() == competenceId) {
				iterator.remove();
				return true;
			}
		}
		return false;
	}
	
	public boolean removeAddedCourseCompetence(long competenceId) {
		Iterator<CourseCompetenceData> iterator = addedCompetences.iterator();
		
		while (iterator.hasNext()) {
			CourseCompetenceData courseCompetenceData = iterator.next();
		
			if (courseCompetenceData.getCompetenceId() == competenceId) {
				iterator.remove();
				return true;
			}
		}
		return false;
	}
	
	public boolean removeAddedCompetence(Competence competence) {
		if (competence != null) {
			Iterator<CourseCompetenceData> iterator = addedCompetences.iterator();
			
			while (iterator.hasNext()) {
				CourseCompetenceData courseComp = (CourseCompetenceData) iterator.next();
				
				if (courseComp.getCompetenceId() == competence.getId()) {
					iterator.remove();
					return true;
				}
			}
		}
		return false;
	}

	public long getBasedOnCourse() {
		return basedOnCourse;
	}

	public void setBasedOnCourse(long basedOnCourse) {
		this.basedOnCourse = basedOnCourse;
	}

	public List<Tag> getTags() {
		return tags;
	}

	public void setTags(List<Tag> tags) {
		this.tags = tags;
	}
	
	public String getTagsString() {
		return tagsString;
	}

	public void setTagsString(String tagsString) {
		this.tagsString = tagsString;
	}
	
	public CreatorType getCreatorType() {
		return creatorType;
	}

	public void setCreatorType(CreatorType creatorType) {
		this.creatorType = creatorType;
	}
	
	public long getEnrollmentId() {
		return enrollmentId;
	}

	public void setEnrollmentId(long enrollmentId) {
		this.enrollmentId = enrollmentId;
	}

	public long getTargetGoalId() {
		return targetGoalId;
	}

	public void setTargetGoalId(long targetGoalId) {
		this.targetGoalId = targetGoalId;
	}
	
	public boolean isEnrolled() {
		return enrolled;
	}

	public void setEnrolled(boolean enrolled) {
		this.enrolled = enrolled;
	}
	
	public boolean isInFutureCourses() {
		return inFutureCourses;
	}

	public void setInFutureCourses(boolean inFutureCourses) {
		this.inFutureCourses = inFutureCourses;
	}

	public boolean isInCompletedCourses() {
		return inCompletedCourses;
	}

	public void setInCompletedCourses(boolean inCompletedCourses) {
		this.inCompletedCourses = inCompletedCourses;
	}
	
	public boolean isInWithdrawnCourses() {
		return inWithdrawnCourses;
	}

	public void setInWithdrawnCourses(boolean inWithdrawnCourses) {
		this.inWithdrawnCourses = inWithdrawnCourses;
	}

	@Override
	public int compareTo(CourseData o) {
		if (this.dateCreated != null && o.dateCreated != null) {
			if (this.dateCreated.before(o.dateCreated)) {
				return -1;
			} else {
				return 1;
			}
		}
		return this.getTitle().compareToIgnoreCase(o.getTitle());
	}

	@Override
	public boolean equals(Object courseData) {
		if (courseData != null) {
			CourseData objData = (CourseData) courseData;
			return id == objData.getId();
		} 
		return false;
	}
	
	public void setEnrollment(CourseEnrollment enrollment) {
		this.enrolled = true;
		
		if (enrollment != null) {
			this.enrollmentId = enrollment.getId();
			
			if (enrollment.getTargetGoal() != null) {
				this.targetGoalId = enrollment.getTargetGoal().getId();
				this.progress = enrollment.getTargetGoal().getProgress();
			}
		}
	}
	
	public List<Long> getMemberIds() {
		return memberIds;
	}

	public void setMemberIds(List<Long> memberIds) {
		this.memberIds = memberIds;
	}
	
	public boolean isStudentsCanAddNewCompetences() {
		return studentsCanAddNewCompetences;
	}

	public void setStudentsCanAddNewCompetences(boolean studentsCanAddNewCompetences) {
		this.studentsCanAddNewCompetences = studentsCanAddNewCompetences;
	}
	
	public boolean isPublished() {
		return published;
	}

	public void setPublished(boolean published) {
		this.published = published;
	}

	public String getAllCompetenceIds() {
		if ((originalCompetences.size() + addedCompetences.size()) > 0) {
			long[] ids = new long[originalCompetences.size() + addedCompetences.size()];
			
			for (int i = 0; i < ids.length; i++) {
				if (i < originalCompetences.size()) {
					ids[i] = originalCompetences.get(i).getCompetenceId();
				} else {
					ids[i] = addedCompetences.get(i-originalCompetences.size()).getCompetenceId();
				}
			}
			return Arrays.toString(ids);
		}
		return null;
	}

	public String getHashtagsString() {
		return hashtagsString;
	}

	public void setHashtagsString(String hashtagsString) {
		this.hashtagsString = hashtagsString;
	}
	
	public List<String> getBlogs() {
		return blogs;
	}

	public void setBlogs(List<String> blogs) {
		this.blogs = blogs;
	}
	
	public boolean addBlog(String blog) {
		int indexOfSlash = blog.lastIndexOf("/");
		
		if (indexOfSlash >= 0 && indexOfSlash == blog.length()-1) {
			blog = blog.substring(0, indexOfSlash);
		}
		
		if (blog != null) {
			if (!this.blogs.contains(blog)) {
				this.blogs.add(blog);
				return true;
			} else {
				return false;
			}
		}
		return false;
	}
	
	public boolean removeBlog(String blog) {
		if (blog != null) {
			return this.blogs.remove(blog);
		}
		return false;
	}
	
}
