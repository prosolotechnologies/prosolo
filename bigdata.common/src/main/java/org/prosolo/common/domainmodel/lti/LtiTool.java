package org.prosolo.common.domainmodel.lti;

import org.prosolo.common.domainmodel.organization.Organization;
import org.prosolo.common.domainmodel.organization.Unit;
import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.domainmodel.user.UserGroup;

import javax.persistence.*;


@Entity
public class LtiTool extends BaseLtiEntity {

	private static final long serialVersionUID = 1200930197977531182L;
	
	private String code;
	private String toolKey;
	private String name;
	private String description;
	private String launchUrl;
	private long credentialId = -1;
	private long competenceId = -1;
	private long activityId = -1;
	private ResourceType toolType;
	private String resourceName;
	private User createdBy;
	private LtiToolSet toolSet;
	private String customCss;
	private boolean enabled = true;
	private Organization organization;
	private Unit unit;
	private UserGroup userGroup;

	public LtiTool(){

	}
	
	public LtiTool(long id, boolean enabled, boolean delted,  String customCss, ResourceType toolType, long activityId, 
			long competenceId, long credentialId, long toolSetId, long consumerId,
			String keyLtiOne, String secretLtiOne, String keyLtiTwo, String secretLtiTwo, String launchUrl) {
		
		setId(id);
		this.toolType = toolType;
		this.credentialId = credentialId;
		this.competenceId = competenceId;
		this.activityId = activityId;
		this.customCss = customCss;
		this.enabled = enabled;
		LtiConsumer cons = new LtiConsumer(consumerId, keyLtiOne, secretLtiOne, keyLtiTwo, secretLtiTwo);
		LtiToolSet ts = new LtiToolSet(toolSetId, cons);
		this.toolSet = ts;
		this.launchUrl = launchUrl;
		
	}
	
	public LtiTool(long id, String name, String description, String launchUrl){
		setId(id);
		this.name = name;
		this.description = description;
		this.launchUrl = launchUrl;
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

	public long getCredentialId() {
		return credentialId;
	}

	public void setCredentialId(long credentialId) {
		this.credentialId = credentialId;
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
	@Column(nullable = false)
	public ResourceType getToolType() {
		return toolType;
	}

	public void setToolType(ResourceType toolType) {
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

	@Transient
	public String getFullLaunchURL(){
		return launchUrl+"?id="+getId();
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	public Unit getUnit() {
		return unit;
	}

	public void setUnit(Unit unit) {
		this.unit = unit;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	public UserGroup getUserGroup() {
		return userGroup;
	}

	public void setUserGroup(UserGroup userGroup) {
		this.userGroup = userGroup;
	}
}