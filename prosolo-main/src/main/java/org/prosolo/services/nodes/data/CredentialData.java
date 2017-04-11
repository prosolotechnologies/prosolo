package org.prosolo.services.nodes.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.credential.CredentialType;
import org.prosolo.common.util.date.DateUtil;
import org.prosolo.services.common.observable.StandardObservable;
import org.prosolo.services.nodes.util.TimeUtil;

/** For all fields that can be updated changes can be tracked */
public class CredentialData extends StandardObservable implements Serializable {

	private static final long serialVersionUID = -8784334832131740545L;
	
	//private static Logger logger = Logger.getLogger(CredentialData.class);
	
	/*
	 * this is special version field that should not be changed. it should be copied from 
	 * a database record and never be changed again.
	 */
	private long version = -1;
	private long id;
	private String title;
	private String description;
	private Set<Tag> tags;
	private String tagsString;
	private Set<Tag> hashtags;
	private String hashtagsString = "";
	private boolean mandatoryFlow;
	private long duration;
	private String durationString;
	private ResourceCreator creator;
	private List<CompetenceData1> competences;
	private boolean automaticallyAssingStudents;
	private int defaultNumberOfStudentsPerInstructor;
	
	//target credential data
	private boolean enrolled;
	private long targetCredId;
	private int progress;
	private long nextCompetenceToLearnId;
	
	private boolean bookmarkedByCurrentUser;
	private long instructorId;
	private String instructorAvatarUrl;
	private String instructorFullName;
	private Date date;
	private boolean instructorPresent;
	
	private boolean archived;
	
	//for delivery
	private long deliveryOfId;
	private Date deliveryStart;
	private Date deliveryEnd;
	private CredentialType type;
	//is delivery active
	private CredentialDeliveryStatus deliveryStatus;
	private long numberOfStudents;
	private long numberOfInstructors;
	
	//for original
	private List<CredentialData> deliveries;
	
	public CredentialData(boolean listenChanges) {
		//this.status = PublishedStatus.UNPUBLISH;
		competences = new ArrayList<>();
		this.listenChanges = listenChanges;
	}

	/**
	 * This method needed to be overriden to deal with collection of competences because
	 * super method does not take into account collections
	 */
	@Override
	public boolean hasObjectChanged() {
		boolean changed = super.hasObjectChanged();
		if(!changed) {
			for(CompetenceData1 cd : getCompetences()) {
				if(cd.getObjectStatus() != ObjectStatus.UP_TO_DATE) {
					return true;
				}
			}
		}
		return changed;
	}
	
	public String getDeliveryStartString() {
		return getDateString(deliveryStart);
	}
	
	public String getDeliveryEndString() {
		return getDateString(deliveryEnd);
	}
	
	public String getDateString(Date date) {
		String str = DateUtil.parseDateWithShortMonthName(date);
		return str != null ? str : "-";
	}
	
	public boolean hasMoreCompetences(int index) {
		return index < competences.size() - 1;
	}
	
	public void calculateDurationString() {
		durationString = TimeUtil.getHoursAndMinutesInString(this.duration);
	}
	
	public boolean isCompleted() {
		return progress == 100;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		observeAttributeChange("title", this.title, title);
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		observeAttributeChange("description", this.description, description);
		this.description = description;
	}

	public String getTagsString() {
		return tagsString;
	}

	public void setTagsString(String tagsString) {
		observeAttributeChange("tagsString", this.tagsString, tagsString);
		this.tagsString = tagsString;
	}

	public String getHashtagsString() {
		return hashtagsString;
	}

	public void setHashtagsString(String hashtagsString) {
		observeAttributeChange("hashtagsString", this.hashtagsString, hashtagsString);
		this.hashtagsString = hashtagsString;
	}

	public boolean isMandatoryFlow() {
		return mandatoryFlow;
	}

	public void setMandatoryFlow(boolean mandatoryFlow) {
		observeAttributeChange("mandatoryFlow", this.mandatoryFlow, mandatoryFlow);
		this.mandatoryFlow = mandatoryFlow;
	}

	public long getId() {
		return id;
	}

	public void setId(long credId) {
		this.id = credId;
	}

	public List<CompetenceData1> getCompetences() {
		return competences;
	}

	public void setCompetences(List<CompetenceData1> competences) {
		this.competences = competences;
	}

	public ResourceCreator getCreator() {
		return creator;
	}

	public void setCreator(ResourceCreator creator) {
		this.creator = creator;
	}

	public boolean isEnrolled() {
		return enrolled;
	}

	public void setEnrolled(boolean enrolled) {
		this.enrolled = enrolled;
	}

	public long getTargetCredId() {
		return targetCredId;
	}

	public void setTargetCredId(long targetCredId) {
		this.targetCredId = targetCredId;
	}

	public int getProgress() {
		return progress;
	}

	public void setProgress(int progress) {
		this.progress = progress;
	}

	public String getDurationString() {
		return durationString;
	}
	
	public void setDurationString(String durationString) {
		this.durationString = durationString;
	}
	
	public Set<Tag> getTags() {
		return tags;
	}

	public void setTags(Set<Tag> tags) {
		this.tags = tags;
	}

	public Set<Tag> getHashtags() {
		return hashtags;
	}

	public void setHashtags(Set<Tag> hashtags) {
		this.hashtags = hashtags;
	}

	public boolean isAutomaticallyAssingStudents() {
		return automaticallyAssingStudents;
	}

	public void setAutomaticallyAssingStudents(boolean automaticallyAssingStudents) {
		observeAttributeChange("automaticallyAssingStudents", this.automaticallyAssingStudents, 
				automaticallyAssingStudents);
		this.automaticallyAssingStudents = automaticallyAssingStudents;
	}

	public int getDefaultNumberOfStudentsPerInstructor() {
		return defaultNumberOfStudentsPerInstructor;
	}

	public void setDefaultNumberOfStudentsPerInstructor(int defaultNumberOfStudentsPerInstructor) {
		this.defaultNumberOfStudentsPerInstructor = defaultNumberOfStudentsPerInstructor;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		observeAttributeChange("duration", this.duration, duration);
		this.duration = duration;
		calculateDurationString();
	}

	public boolean isBookmarkedByCurrentUser() {
		return bookmarkedByCurrentUser;
	}

	public void setBookmarkedByCurrentUser(boolean bookmarkedByCurrentUser) {
		this.bookmarkedByCurrentUser = bookmarkedByCurrentUser;
	}

	public long getNextCompetenceToLearnId() {
		return nextCompetenceToLearnId;
	}

	public void setNextCompetenceToLearnId(long nextCompetenceToLearnId) {
		this.nextCompetenceToLearnId = nextCompetenceToLearnId;
	}

	public long getInstructorId() {
		return instructorId;
	}

	public void setInstructorId(long instructorId) {
		this.instructorId = instructorId;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getInstructorAvatarUrl() {
		return instructorAvatarUrl;
	}

	public void setInstructorAvatarUrl(String instructorAvatarUrl) {
		this.instructorAvatarUrl = instructorAvatarUrl;
	}

	public String getInstructorFullName() {
		return instructorFullName;
	}

	public void setInstructorFullName(String instructorFullName) {
		this.instructorFullName = instructorFullName;
	}

	public boolean isInstructorPresent() {
		return instructorPresent;
	}

	public void setInstructorPresent(boolean instructorPresent) {
		this.instructorPresent = instructorPresent;
	}
	
	public boolean isArchived() {
		return archived;
	}

	public void setArchived(boolean archived) {
		this.archived = archived;
	}
	
	public long getVersion() {
		return version;
	}

	/**
	 * Setting version is only allowed if version is -1. Generally version should not 
	 * be changed except when data is being populated.
	 * 
	 * @param version
	 */
	public void setVersion(long version) {
		if(this.version == -1) {
			this.version = version;
		}
	}

	//change tracking get methods
	
	public long getDeliveryOfId() {
		return deliveryOfId;
	}

	public void setDeliveryOfId(long deliveryOfId) {
		this.deliveryOfId = deliveryOfId;
	}

	public Date getDeliveryStart() {
		return deliveryStart;
	}

	public void setDeliveryStart(Date deliveryStart) {
		observeAttributeChange("deliveryStart", this.deliveryStart, deliveryStart);
		this.deliveryStart = deliveryStart;
	}

	public Date getDeliveryEnd() {
		return deliveryEnd;
	}

	public void setDeliveryEnd(Date deliveryEnd) {
		observeAttributeChange("deliveryEnd", this.deliveryEnd, deliveryEnd);
		this.deliveryEnd = deliveryEnd;
	}

	public CredentialType getType() {
		return type;
	}

	public void setType(CredentialType type) {
		this.type = type;
	}

	public boolean isTitleChanged() {
		return changedAttributes.containsKey("title");
	}

	public boolean isDescriptionChanged() {
		return changedAttributes.containsKey("description");
	}

	public boolean isTagsStringChanged() {
		return changedAttributes.containsKey("tagsString");
	}

	public boolean isHashtagsStringChanged() {
		return changedAttributes.containsKey("hashtagsString");
	}
	
	public String getOldHashtags() {
		return (String) changedAttributes.get("hashtagsString");
	}

	public boolean isStatusChanged() {
		return changedAttributes.containsKey("status");
	}

	public boolean isMandatoryFlowChanged() {
		return changedAttributes.containsKey("mandatoryFlow");
	}
	
	public boolean isDurationChanged() {
		return changedAttributes.containsKey("duration");
	}
	
	public boolean isScheduledPublicDateChanged() {
		return changedAttributes.containsKey("scheduledPublicDate");
	}

	public List<CredentialData> getDeliveries() {
		return deliveries;
	}

	public void setDeliveries(List<CredentialData> deliveries) {
		this.deliveries = deliveries;
	}

	public CredentialDeliveryStatus getDeliveryStatus() {
		return deliveryStatus;
	}

	public void setDeliveryStatus(CredentialDeliveryStatus deliveryStatus) {
		this.deliveryStatus = deliveryStatus;
	}

	public long getNumberOfStudents() {
		return numberOfStudents;
	}

	public void setNumberOfStudents(long numberOfStudents) {
		this.numberOfStudents = numberOfStudents;
	}

	public long getNumberOfInstructors() {
		return numberOfInstructors;
	}

	public void setNumberOfInstructors(long numberOfInstructors) {
		this.numberOfInstructors = numberOfInstructors;
	}

}
