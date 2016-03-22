package org.prosolo.web.courses.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.course.Course;
import org.prosolo.common.domainmodel.course.CreatorType;
import org.prosolo.services.nodes.data.PublishedStatus;
import org.prosolo.util.nodes.AnnotationUtil;

public class BasicCourseData {

	private long id;
	private Date dateCreated;
	private long targetGoalId;
	private String title;
	private String description;
	
	private boolean published;
	
	private List<Tag> tags;
	private String tagsString;
	private CreatorType creatorType;
	
	private String hashtagsString = "";
	
	private PublishedStatus courseStatus;
	
	public BasicCourseData() {
		this.tags = new ArrayList<Tag>();
	}

	public BasicCourseData(Course course) {
		this();
		this.id = course.getId();
		this.dateCreated = course.getDateCreated();
		this.title = course.getTitle();
		this.description = course.getDescription();
		this.tags = new ArrayList<Tag>(course.getTags());
		this.tagsString = AnnotationUtil.getAnnotationsAsSortedCSV(this.tags);
		this.creatorType = course.getCreatorType();
		this.published = course.isPublished();
		setCourseStatus();
		this.hashtagsString = AnnotationUtil.getAnnotationsAsSortedCSV(course.getHashtags());
	}
	
	public static BasicCourseData copyBasicCourseData(BasicCourseData data) {
		BasicCourseData course = new BasicCourseData();
		course.setId(data.getId());
		course.setDateCreated(data.getDateCreated());
		course.setTitle(data.getTitle());
		course.setDescription(data.getDescription());
		course.setTags(new ArrayList<>(data.getTags()));
		course.setTagsString(data.getTagsString());
		course.setCreatorType(data.getCreatorType());
		course.setPublished(data.isPublished());
		course.setCourseStatus();
		course.setHashtagsString(data.getHashtagsString());
		return course;
	}
	
	//setting course status based on published flag
	public void setCourseStatus() {
		this.courseStatus = this.published ? PublishedStatus.PUBLISHED : PublishedStatus.DRAFT;
	}
	
	//setting published flag based on course status
	public void setPublished() {
		this.published = courseStatus == PublishedStatus.PUBLISHED ? true : false;
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

	public long getTargetGoalId() {
		return targetGoalId;
	}

	public void setTargetGoalId(long targetGoalId) {
		this.targetGoalId = targetGoalId;
	}

	@Override
	public boolean equals(Object courseData) {
		if (courseData != null) {
			CourseData objData = (CourseData) courseData;
			return id == objData.getId();
		} 
		return false;
	}
	
	public boolean isPublished() {
		return published;
	}

	public void setPublished(boolean published) {
		this.published = published;
	}

	public String getHashtagsString() {
		return hashtagsString;
	}

	public void setHashtagsString(String hashtagsString) {
		this.hashtagsString = hashtagsString;
	}
	
	public PublishedStatus getCourseStatus() {
		return courseStatus;
	}

	public void setCourseStatus(PublishedStatus courseStatus) {
		this.courseStatus = courseStatus;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
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

}
