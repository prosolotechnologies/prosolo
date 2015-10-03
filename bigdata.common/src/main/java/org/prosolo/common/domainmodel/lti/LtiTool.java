package org.prosolo.common.domainmodel.lti;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.prosolo.common.domainmodel.user.User;

import antlr.Tool;


@Entity
public class LtiTool extends BaseLtiEntity {

	private static final long serialVersionUID = 1200930197977531182L;
	
	private String code;
	private String toolKey;
	private String name;
	private String description;
	private String launchUrl;
	private long learningGoalId = -1;
	private long competenceId = -1;
	private long activityId = -1;
	private LtiToolType toolType;
	private String resourceName;
	private User createdBy;
	private LtiToolSet toolSet;
	private String customCss;
	private boolean enabled = true;

	public LtiTool(){

	}
	
	

	public LtiTool(long id, boolean enabled, boolean delted,  String customCss, long activityId, 
			long competenceId, long learningGoalId, long toolSetId, long consumerId,
			String keyLtiOne, String secretLtiOne, String keyLtiTwo, String secretLtiTwo) {
		
		this.setId(id);
		this.learningGoalId = learningGoalId;
		this.competenceId = competenceId;
		this.activityId = activityId;
		this.customCss = customCss;
		this.enabled = enabled;
		LtiConsumer cons = new LtiConsumer(consumerId, keyLtiOne, secretLtiOne, keyLtiTwo, secretLtiTwo);
		LtiToolSet ts = new LtiToolSet(toolSetId, cons);
		this.toolSet = ts;
		
	}



	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getToolKey() {
		return toolKey;
	}

	public void setToolKey(String toolKey) {
		this.toolKey = toolKey;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column (length = 2000)
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getLaunchUrl() {
		return launchUrl;
	}

	public void setLaunchUrl(String launchUrl) {
		this.launchUrl = launchUrl;
	}

	public long getLearningGoalId() {
		return learningGoalId;
	}

	public void setLearningGoalId(long learningGoalId) {
		this.learningGoalId = learningGoalId;
	}

	public long getCompetenceId() {
		return competenceId;
	}

	public void setCompetenceId(long competenceId) {
		this.competenceId = competenceId;
	}

	public long getActivityId() {
		return activityId;
	}

	public void setActivityId(long activityId) {
		this.activityId = activityId;
	}

	@Enumerated(EnumType.STRING)
	public LtiToolType getToolType() {
		return toolType;
	}

	public void setToolType(LtiToolType toolType) {
		this.toolType = toolType;
	}

	public String getResourceName() {
		return resourceName;
	}

	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	public User getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(User createdBy) {
		this.createdBy = createdBy;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	public LtiToolSet getToolSet() {
		return toolSet;
	}

	public void setToolSet(LtiToolSet toolSet) {
		this.toolSet = toolSet;
	}

	@Column(length = 10000)
	public String getCustomCss() {
		return customCss;
	}

	public void setCustomCss(String customCss) {
		this.customCss = customCss;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}


}