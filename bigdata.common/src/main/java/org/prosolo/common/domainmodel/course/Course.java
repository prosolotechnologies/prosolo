/**
 * 
 */
package org.prosolo.common.domainmodel.course;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Type;
import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.feeds.FeedSource;
import org.prosolo.common.domainmodel.general.BaseEntity;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.course.Course;
import org.prosolo.common.domainmodel.course.CourseCompetence;
import org.prosolo.common.domainmodel.course.CreatorType;

/**
 * @author "Nikola Milikic"
 * 
 */
@Entity
public class Course extends BaseEntity {

	private static final long serialVersionUID = -4158905515450966577L;
	
	@JsonIgnore
	private User maker;
	private Set<Tag> tags;
	private Set<Tag> hashtags;
	private List<CourseCompetence> competences;
	private CreatorType creatorType;
	private boolean studentsCanAddNewCompetences;
	private boolean published;
	@JsonIgnore
	private Course basedOn;
	
	private List<FeedSource> blogs;
	private List<FeedSource> excludedFeedSources;

	private boolean manuallyAssignStudentsToInstructors;
	private int defaultNumberOfStudentsPerInstructor;
	
	public Course() {
		competences = new ArrayList<CourseCompetence>();
		tags = new HashSet<Tag>();
		hashtags = new HashSet<Tag>();
		blogs = new ArrayList<FeedSource>();
		excludedFeedSources = new ArrayList<FeedSource>();
	}
	
	@OneToOne(fetch = FetchType.LAZY)
	public User getMaker() {
		return maker;
	}

	public void setMaker(User maker) {
		if (null != maker) {
			this.maker = maker;
		}
	}

	@ManyToMany
	public Set<Tag> getTags() {
		return tags;
	}

	public void setTags(Set<Tag> tags) {
		this.tags = tags;
	}
	
	@ManyToMany
	public Set<Tag> getHashtags() {
		return hashtags;
	}

	public void setHashtags(Set<Tag> hashtags) {
		this.hashtags = hashtags;
	}

	@ManyToMany
	@Cascade({org.hibernate.annotations.CascadeType.SAVE_UPDATE, org.hibernate.annotations.CascadeType.MERGE})
	public List<CourseCompetence> getCompetences() {
		return competences;
	}

	public void setCompetences(List<CourseCompetence> competences) {
		this.competences = competences;
	}
	
	public boolean addCompetence(CourseCompetence competence) {
		if (competence != null) {
			if (!competences.contains(competence)) {
				return competences.add(competence);
			}
		}
		return false;
	}
	
	@Enumerated(EnumType.STRING)
	public CreatorType getCreatorType() {
		return creatorType;
	}

	public void setCreatorType(CreatorType creatorType) {
		this.creatorType = creatorType;
	}

	@OneToOne
 	@JoinTable(name = "course_basedon_course")
	public Course getBasedOn() {
		return basedOn;
	}

	public void setBasedOn(Course basedOn) {
		this.basedOn = basedOn;
	}

	@Column(nullable = true)
	@Type(type = "true_false")
	public boolean isStudentsCanAddNewCompetences() {
		return studentsCanAddNewCompetences;
	}

	public void setStudentsCanAddNewCompetences(boolean studentsCanAddNewCompetences) {
		this.studentsCanAddNewCompetences = studentsCanAddNewCompetences;
	}

	@Type(type = "true_false")
	@Column(columnDefinition = "char(1) DEFAULT 'F'")
	public boolean isPublished() {
		return published;
	}

	public void setPublished(boolean published) {
		this.published = published;
	}
	
	@ManyToMany
	public List<FeedSource> getBlogs() {
		return blogs;
	}

	public void setBlogs(List<FeedSource> blogs) {
		this.blogs = blogs;
	}

	public void addBlog(FeedSource feedSource) {
		this.getBlogs().add(feedSource);
	}
	
	@ManyToMany
	public List<FeedSource> getExcludedFeedSources() {
		return excludedFeedSources;
	}

	public void setExcludedFeedSources(List<FeedSource> excludedFeedSources) {
		this.excludedFeedSources = excludedFeedSources;
	}
	
	public void addExcludedFeedSource(FeedSource feedSource) {
		this.getExcludedFeedSources().add(feedSource);
	}

	@Type(type="true_false")
	@Column(columnDefinition = "char(1) DEFAULT 'F'")
	public boolean isManuallyAssignStudentsToInstructors() {
		return manuallyAssignStudentsToInstructors;
	}

	public void setManuallyAssignStudentsToInstructors(boolean manuallyAssignStudentsToInstructors) {
		this.manuallyAssignStudentsToInstructors = manuallyAssignStudentsToInstructors;
	}

	public int getDefaultNumberOfStudentsPerInstructor() {
		return defaultNumberOfStudentsPerInstructor;
	}

	public void setDefaultNumberOfStudentsPerInstructor(int defaultNumberOfStudentsPerInstructor) {
		this.defaultNumberOfStudentsPerInstructor = defaultNumberOfStudentsPerInstructor;
	}

	@Override
	public String toString() {
		return "Course [maker=" + maker + ", tags=" + tags + ", hashtags=" + hashtags + ", competences=" + competences + ", creatorType="
				+ creatorType + ", studentsCanAddNewCompetences=" + studentsCanAddNewCompetences + ", published=" + published + ", basedOn="
				+ basedOn + ", getTitle()=" + getTitle() + ", getId()=" + getId() + ", getDateCreated()=" + getDateCreated() + "]";
	}

}
