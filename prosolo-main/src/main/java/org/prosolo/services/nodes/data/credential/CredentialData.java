package org.prosolo.services.nodes.data.credential;

import org.prosolo.common.domainmodel.annotation.Tag;
import org.prosolo.common.domainmodel.assessment.AssessmentType;
import org.prosolo.common.domainmodel.credential.Credential1;
import org.prosolo.common.domainmodel.credential.CredentialType;
import org.prosolo.common.util.Pair;
import org.prosolo.services.assessment.data.AssessmentTypeConfig;
import org.prosolo.services.assessment.data.LearningResourceAssessmentSettings;
import org.prosolo.services.common.observable.StandardObservable;
import org.prosolo.services.nodes.data.UserData;
import org.prosolo.services.nodes.data.competence.CompetenceData1;
import org.prosolo.services.nodes.data.LearningResourceLearningStage;
import org.prosolo.services.nodes.data.ObjectStatus;
import org.prosolo.services.nodes.data.ResourceCreator;
import org.prosolo.services.nodes.data.organization.CredentialCategoryData;
import org.prosolo.services.nodes.data.organization.LearningStageData;
import org.prosolo.services.nodes.util.TimeUtil;

import java.io.Serializable;
import java.util.*;

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
	private long organizationId;
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
	private UserData student;
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
	private String deliveryOfTitle;
	private long deliveryStartTime;
	private long deliveryEndTime;
	private CredentialType type;
	//is delivery active
	private CredentialDeliveryStatus deliveryStatus;
	private long numberOfStudents;
	private long numberOfInstructors;
	
	//for original
	private List<CredentialData> deliveries;

	//learning in stages
	private boolean learningStageEnabled;
	private LearningStageData learningStage;
	private long firstLearningStageCredentialId;
	private boolean learningStagesInitialized;
	private List<LearningResourceLearningStage> learningStages;

	//assessment
	private LearningResourceAssessmentSettings assessmentSettings;
	private List<AssessmentTypeConfig> assessmentTypes;

	//category
	private CredentialCategoryData category;

	private int numberOfAssessments;

	//profile visibility settings
	private boolean credentialAssessmentsDisplayed;
	private boolean competenceAssessmentsDisplayed;
	private boolean evidenceDisplayed;
	
	public CredentialData(boolean listenChanges) {
		//this.status = PublishedStatus.UNPUBLISH;
		competences = new ArrayList<>();
		learningStages = new ArrayList<>();
		assessmentSettings = new LearningResourceAssessmentSettings();
		assessmentTypes = new ArrayList<>();
		this.listenChanges = listenChanges;
	}

	public CredentialData(Credential1 credential){
		this.id = credential.getId();
		this.title = credential.getTitle();
		assessmentSettings = new LearningResourceAssessmentSettings();
		assessmentTypes = new ArrayList<>();
	}

	/**
	 * This method needed to be overriden to deal with collection of competences because
	 * super method does not take into account collections
	 */
	@Override
	public boolean hasObjectChanged() {
		boolean changed = super.hasObjectChanged();
		if(!changed) {
			for (CompetenceData1 cd : getCompetences()) {
				if(cd.getObjectStatus() != ObjectStatus.UP_TO_DATE) {
					return true;
				}
			}

			if (getAssessmentSettings().hasObjectChanged()) {
				return true;
			}

			for (AssessmentTypeConfig atc : getAssessmentTypes()) {
				if (atc.hasObjectChanged()) {
					return true;
				}
			}
		}

		return changed;
	}

	@Override
	public void startObservingChanges() {
		super.startObservingChanges();
		getAssessmentSettings().startObservingChanges();
		for (AssessmentTypeConfig atc : getAssessmentTypes()) {
			atc.startObservingChanges();
		}
	}

	public boolean isPeerAssessmentEnabled() {
		return isAssessmentTypeEnabled(AssessmentType.PEER_ASSESSMENT);
	}

	public boolean isSelfAssessmentEnabled() {
		return isAssessmentTypeEnabled(AssessmentType.SELF_ASSESSMENT);
	}

	public Pair<Integer, Integer> getGradeSummary(AssessmentType type) {
		AssessmentTypeConfig aType = getAssessmentTypeConfig(type);
		return aType == null ? null : aType.getGradeSummary();
	}

	private boolean isAssessmentTypeEnabled(AssessmentType type) {
		AssessmentTypeConfig aType = getAssessmentTypeConfig(type);
		return aType != null && aType.isEnabled();
	}

	private AssessmentTypeConfig getAssessmentTypeConfig(AssessmentType type) {
		if (assessmentTypes == null) {
			return null;
		}
		return assessmentTypes.stream().filter(t -> t.getType() == type).findFirst().get();
	}

	public boolean isFirstStageCredential() {
		return getId() == getFirstLearningStageCredentialId();
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

	public CredentialType getType() {
		return type;
	}

	public void setType(CredentialType type) {
		this.type = type;
	}

	public boolean isLearningStageEnabledChanged() {
		return changedAttributes.containsKey("learningStageEnabled");
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
	
	public boolean isDeliveryStartChanged() {
		return changedAttributes.containsKey("deliveryStartTime");
	}

	public boolean isTitleChanged() {
		return changedAttributes.containsKey("title");
	}

	public long getDeliveryStartBeforeUpdate() {
		Long delStartTime = (Long) changedAttributes.get("deliveryStartTime");
		//if not null return this value, if it is null it means it is not changed so original value can be returned
		return delStartTime != null ? delStartTime : deliveryStartTime;
	}
	
	public boolean isDeliveryEndChanged() {
		return changedAttributes.containsKey("deliveryEndTime");
	}

	public long getDeliveryEndBeforeUpdate() {
		Long delEndTime = (Long) changedAttributes.get("deliveryEndTime");
		//if not null return this value, if it is null it means it is not changed so original value can be returned
		return delEndTime != null ? delEndTime : deliveryEndTime;
	}

	public LearningStageData getLearningStageBeforeUpdate() {
		return (LearningStageData) changedAttributes.get("learningStage");
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

	public long getDeliveryStartTime() {
		return deliveryStartTime;
	}

	public void setDeliveryStartTime(long deliveryStartTime) {
		observeAttributeChange("deliveryStartTime", this.deliveryStartTime, deliveryStartTime);
		this.deliveryStartTime = deliveryStartTime;
	}

	public long getDeliveryEndTime() {
		return deliveryEndTime;
	}

	public void setDeliveryEndTime(long deliveryEndTime) {
		observeAttributeChange("deliveryEndTime", this.deliveryEndTime, deliveryEndTime);
		this.deliveryEndTime = deliveryEndTime;
	}

	public String getDeliveryOfTitle() {
		return deliveryOfTitle;
	}

	public void setDeliveryOfTitle(String deliveryOfTitle) {
		this.deliveryOfTitle = deliveryOfTitle;
	}

	public boolean isLearningStageEnabled() {
		return learningStageEnabled;
	}

	public void setLearningStageEnabled(boolean learningStageEnabled) {
		observeAttributeChange("learningStageEnabled", this.learningStageEnabled, learningStageEnabled);
		this.learningStageEnabled = learningStageEnabled;
	}

	public LearningStageData getLearningStage() {
		return learningStage;
	}

	public void setLearningStage(LearningStageData learningStage) {
		observeAttributeChange("learningStage", this.learningStage, learningStage);
		this.learningStage = learningStage;
	}

	public List<LearningResourceLearningStage> getLearningStages() {
		return learningStages;
	}

	public void addLearningStage(LearningResourceLearningStage ls) {
		this.learningStages.add(ls);
	}

	public void addLearningStages(Collection<LearningResourceLearningStage> learningStages) {
		this.learningStages.addAll(learningStages);
	}

	public long getOrganizationId() {
		return organizationId;
	}

	public void setOrganizationId(long organizationId) {
		this.organizationId = organizationId;
	}

	public long getFirstLearningStageCredentialId() {
		return firstLearningStageCredentialId;
	}

	public void setFirstLearningStageCredentialId(long firstLearningStageCredentialId) {
		this.firstLearningStageCredentialId = firstLearningStageCredentialId;
	}

	public boolean isLearningStagesInitialized() {
		return learningStagesInitialized;
	}

	public void setLearningStagesInitialized(boolean learningStagesInitialized) {
		this.learningStagesInitialized = learningStagesInitialized;
	}

	public LearningResourceAssessmentSettings getAssessmentSettings() {
		return assessmentSettings;
	}

	public List<AssessmentTypeConfig> getAssessmentTypes() {
		return assessmentTypes;
	}

	public void setAssessmentTypes(List<AssessmentTypeConfig> assessmentTypes) {
		this.assessmentTypes = assessmentTypes;
	}

	public CredentialCategoryData getCategory() {
		return category;
	}

	public void setCategory(CredentialCategoryData category) {
		observeAttributeChange("category", this.category, category);
		this.category = category;
	}

	public int getNumberOfAssessments() {
		return numberOfAssessments;
	}

	public void setNumberOfAssessments(int numberOfAssessments) {
		this.numberOfAssessments = numberOfAssessments;
	}

	public UserData getStudent() {
		return student;
	}

	public void setStudent(UserData student) {
		this.student = student;
	}

	public boolean isCredentialAssessmentsDisplayed() {
		return credentialAssessmentsDisplayed;
	}

	public void setCredentialAssessmentsDisplayed(boolean credentialAssessmentsDisplayed) {
		this.credentialAssessmentsDisplayed = credentialAssessmentsDisplayed;
	}

	public boolean isCompetenceAssessmentsDisplayed() {
		return competenceAssessmentsDisplayed;
	}

	public void setCompetenceAssessmentsDisplayed(boolean competenceAssessmentsDisplayed) {
		this.competenceAssessmentsDisplayed = competenceAssessmentsDisplayed;
	}

	public boolean isEvidenceDisplayed() {
		return evidenceDisplayed;
	}

	public void setEvidenceDisplayed(boolean evidenceDisplayed) {
		this.evidenceDisplayed = evidenceDisplayed;
	}
}
