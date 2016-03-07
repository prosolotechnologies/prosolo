/**
 * 
 */
package org.prosolo.web.communications.evaluation.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.prosolo.common.domainmodel.user.User;
import org.prosolo.common.web.activitywall.data.UserData;
import org.prosolo.services.nodes.data.activity.attachmentPreview.NodeData;

/**
 * @author "Nikola Milikic"
 * 
 */
public class EvaluationItemData implements Serializable {

	private static final long serialVersionUID = -6155099884634555343L;

	private long id;
	private UserData user;
	private String name;
	private String title;
	private String message;
	private NodeData resource;
	private String date;
	private String dateSubmitted;
	private User creator;
	private boolean draft;
	private boolean accepted;
	private List<SelectedBadge> badges;

	private long evaluationSubmissionId;

	public EvaluationItemData() {
		badges = new ArrayList<SelectedBadge>();
	}
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public User getCreator() {
		return creator;
	}

	public void setCreator(User creator) {
		this.creator = creator;
	}

	public UserData getUser() {
		return user;
	}

	public void setUser(UserData user) {
		this.user = user;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	public NodeData getResource() {
		return resource;
	}

	public void setResource(NodeData resource) {
		this.resource = resource;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}
	
	public String getDateSubmitted() {
		return dateSubmitted;
	}

	public void setDateSubmitted(String dateSubmitted) {
		this.dateSubmitted = dateSubmitted;
	}

	public List<SelectedBadge> getBadges() {
		return badges;
	}

	public void setBadges(List<SelectedBadge> badges) {
		this.badges = badges;
	}

	public void addBadge(SelectedBadge badge) {
		if (badge != null) {
			getBadges().add(badge);
		}
	}

	public long getEvaluationSubmissionId() {
		return evaluationSubmissionId;
	}

	public void setEvaluationSubmissionId(long evaluationSubmissionId) {
		this.evaluationSubmissionId = evaluationSubmissionId;
	}

	public boolean isDraft() {
		return draft;
	}

	public void setDraft(boolean draft) {
		this.draft = draft;
	}

	public boolean isAccepted() {
		return accepted;
	}

	public void setAccepted(boolean accepted) {
		this.accepted = accepted;
	}
	
}
